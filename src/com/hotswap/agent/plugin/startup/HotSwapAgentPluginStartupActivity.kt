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
package com.hotswap.agent.plugin.startup

import com.hotswap.agent.plugin.services.DownloadManager
import com.hotswap.agent.plugin.services.DownloadManagerException
import com.hotswap.agent.plugin.services.HotSwapAgentPluginNotification
import com.hotswap.agent.plugin.settings.HotSwapAgentPluginSettingsProvider
import com.hotswap.agent.plugin.util.HotSwapAgentPathUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import java.io.File

/**
 * @author Dmitry Zhuravlev
 *         Date:  10.03.2017
 */
class HotSwapAgentPluginStartupActivity : StartupActivity {
    companion object {
        internal val log = Logger.getInstance(HotSwapAgentPluginStartupActivity::class.java)
    }

    override fun runActivity(project: Project) {
        val stateProvider = HotSwapAgentPluginSettingsProvider.getInstance(project)
        checkForNewAgentVersion(project, stateProvider)
        downloadLatestAgentSilentlyIfNeeded(project, stateProvider)
    }

    private fun downloadLatestAgentSilentlyIfNeeded(project: Project, stateProvider: HotSwapAgentPluginSettingsProvider) = with(DownloadManager.getInstance(project)) {
        if (!File(stateProvider.currentState.agentPath).exists()) {
            val latestArtifact = getLatestAgentDescriptorOrDefault()
            val defaultAgentJarPath = HotSwapAgentPathUtil.getAgentJarPath(latestArtifact.version)
            if (File(defaultAgentJarPath).exists()) {
                stateProvider.currentState.agentPath = defaultAgentJarPath
                return
            }
            try {
                downloadAgentJarAsynchronously(project, latestArtifact) { downloadedAgentPath ->
                    stateProvider.currentState.agentPath = downloadedAgentPath
                }
            } catch(e: DownloadManagerException) {
                log.error("Cannot download agent jar: ", e)
            }
        }
    }

    private fun checkForNewAgentVersion(project: Project, stateProvider: HotSwapAgentPluginSettingsProvider) = with(DownloadManager.getInstance(project)) {
        if (File(stateProvider.currentState.agentPath).exists()) {
            val currentVersion = HotSwapAgentPathUtil.determineAgentVersionFromPath(stateProvider.currentState.agentPath) ?: return
            val latestArtifact = getLatestAgentDescriptorOrDefault()
            if (latestArtifact.version > currentVersion) {
                HotSwapAgentPluginNotification.getInstance(project).showNotificationAboutNewAgentVersion {
                    downloadAgentJarAsynchronously(project, latestArtifact) { downloadedAgentPath ->
                        stateProvider.currentState.agentPath = downloadedAgentPath
                    }
                }
            }
        }
    }

}