/*
 *  Copyright (c) 2017 Dmitry Zhuravlev, Sergei Stepanov
 *  Copyright (c) 2021 Ruin0x11
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.hotswap.agent.plugin.services

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.hotswap.agent.plugin.util.Constants
import com.hotswap.agent.plugin.util.Constants.Companion.AGENT_DOWNLOAD_URL
import com.hotswap.agent.plugin.util.Constants.Companion.AGENT_LATEST_RELEASE_API_URL
import com.hotswap.agent.plugin.util.HotSwapAgentPathUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.platform.templates.github.DownloadUtil
import com.intellij.util.io.HttpRequests
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * @author Dmitry Zhuravlev
 *         Date:  10.03.2017
 */
class DownloadManager {
    companion object {
        internal val log = Logger.getInstance(DownloadManager::class.java)
        fun getInstance(project: Project? = null) = when (project) {
            null -> ServiceManager.getService<DownloadManager>(ProjectManager.getInstance().defaultProject, DownloadManager::class.java)!!
            else -> ServiceManager.getService<DownloadManager>(project, DownloadManager::class.java)!!
        }
    }

    fun downloadAgentJarSynchronously(project: Project? = ProjectManager.getInstance().defaultProject, artifactToDownload: ArtifactDescriptor, canBeCanceled: Boolean, onSuccess: (String) -> Unit) {
        val progressText = "Downloading HotSwapAgent $artifactToDownload"
        ProgressManager.getInstance().run(
                object : Task.Modal(project, "Downloading", canBeCanceled) {
                    override fun run(progress: ProgressIndicator) {
                        try {
                            onSuccess(doDownload(artifactToDownload, progress, progressText))
                        } catch(e: IOException) {
                            throw DownloadManagerException(e)
                        }
                    }
                }
        )
    }

    fun downloadAgentJarAsynchronously(project: Project, artifactToDownload: ArtifactDescriptor, onSuccess: (String) -> Unit) {
        val progressText = "Downloading HotSwapAgent ${artifactToDownload.version}"
        ApplicationManager.getApplication().invokeLater {
            val downloadTask = object : Task.Backgroundable(project, "Downloading") {
                override fun run(progress: ProgressIndicator) {
                    try {
                        onSuccess(doDownload(artifactToDownload, progress, progressText))
                    } catch(e: IOException) {
                        throw DownloadManagerException(e)
                    }
                }
            }
            val progress = BackgroundableProcessIndicator(downloadTask).apply {
                text = progressText
            }
            ProgressManager.getInstance().runProcessWithProgressAsynchronously(downloadTask, progress)
        }
    }

    fun isLatestAgentVersionAvailable(currentVersion: String) = getLatestAgentDescriptorOrDefault().version > currentVersion

    fun getLatestAgentDescriptorOrDefault(default: ArtifactDescriptor = ArtifactDescriptor(Constants.MIN_AGENT_VERSION, Constants.MIN_AGENT_VERSION)): ArtifactDescriptor {
        return try {
            determineLatestAgentVersionRequest().get(20, TimeUnit.SECONDS)
        } catch(ex: Exception) {
            default
        }
    }

    private fun determineLatestAgentVersionRequest(): Future<ArtifactDescriptor> {
        val callable = Callable<ArtifactDescriptor> {
            var result = ArtifactDescriptor(Constants.MIN_AGENT_VERSION, Constants.MIN_AGENT_VERSION)
            try {
                result = HttpRequests.request(AGENT_LATEST_RELEASE_API_URL)
                        .productNameAsUserAgent().connect { request ->
                            var version: String = Constants.MIN_AGENT_VERSION
                            @Suppress("UNCHECKED_CAST")
                            val reader = BufferedReader(InputStreamReader(request.inputStream))
                            val jo = JsonParser().parse(reader) as JsonObject
                            val versionName = jo["name"]?.asString
                            val tagName = jo["tag_name"]?.asString ?: Constants.MIN_AGENT_VERSION

                            when {
                                versionName.isNullOrBlank() -> version = tagName
                                versionName != null -> version = versionName

                            }
                            ArtifactDescriptor(tagName, version)
                        }
            } catch (ex: IOException) {
                DownloadManager.log.warn(
                        "Couldn't load the release URL: $AGENT_LATEST_RELEASE_API_URL")
            }
            result
        }
        return Executors.newFixedThreadPool(1).submit(callable)
    }


    private fun doDownload(artifactDescriptor: ArtifactDescriptor, progress: ProgressIndicator?, progressText: String?): String {
        val downloadUrl = with(artifactDescriptor) { "$AGENT_DOWNLOAD_URL/$tagName/hotswap-agent-$version.jar" }
        val agentJarPath = HotSwapAgentPathUtil.getAgentJarPath(artifactDescriptor.version)
        val file = File(agentJarPath)
        if (progress != null && progressText != null) {
            progress.text = progressText
        }

        DownloadUtil.downloadContentToFile(progress, downloadUrl, file)

        if (!file.exists()) {
            log.debug(file.toString() + " downloaded")
        }
        return agentJarPath
    }
}

class ArtifactDescriptor(val tagName:String, val version: String)

class DownloadManagerException(e: Throwable) : Exception(e)