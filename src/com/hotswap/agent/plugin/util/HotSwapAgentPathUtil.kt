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
package com.hotswap.agent.plugin.util

import com.hotswap.agent.plugin.services.DownloadManager
import com.intellij.openapi.application.PluginPathManager
import com.intellij.util.PathUtil
import java.io.File

/**
 * @author Dmitry Zhuravlev
 *         Date:  10.03.2017
 */
class HotSwapAgentPathUtil {
    companion object {
        private const val AGENT_VERSION_PATTERN = "(\\d+.\\d+)"
        private const val AGENT_JAR_NAME_PATTERN = "(hotswap-agent-$AGENT_VERSION_PATTERN.jar)"

        fun determineAgentVersionFromPath(path: String) = Regex(AGENT_JAR_NAME_PATTERN).find(path)?.value?.let { agentJarName ->
            Regex(AGENT_VERSION_PATTERN).find(agentJarName)?.value
        }

        fun getAgentJarPath(agentVersion: String): String {
            val ourJar = File(PathUtil.getJarPathForClass(DownloadManager::class.java))
            return if (ourJar.isDirectory) {//development mode
                PluginPathManager.getPluginHomePath("hotswapagent") + File.separator + "hotswap-agent-$agentVersion.jar"
            } else
                ourJar.parentFile.path + File.separator + "agent" + File.separator + "hotswap-agent-$agentVersion.jar"
        }
    }
}

