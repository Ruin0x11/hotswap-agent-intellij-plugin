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
package com.hotswap.agent.plugin.configuration.patcher

import com.hotswap.agent.plugin.services.HotSwapAgentPluginNotification
import com.hotswap.agent.plugin.settings.HotSwapAgentPluginSettingsProvider
import com.hotswap.agent.plugin.util.DCEVMUtil
import com.intellij.execution.Executor
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.runners.JavaProgramPatcher
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import java.io.File

/**
 * @author Dmitry Zhuravlev
 *         Date:  10.03.2017
 */
class HotSwapAgentPluginConfigurationPatcher : JavaProgramPatcher() {
    companion object {
        internal val log = Logger.getInstance(HotSwapAgentPluginConfigurationPatcher::class.java)
    }

    override fun patchJavaParameters(executor: Executor?, configuration: RunProfile?, javaParameters: JavaParameters?) {
        val project = (configuration as? RunConfiguration)?.project ?: return
        val stateProvider = HotSwapAgentPluginSettingsProvider.getInstance(project)
        val agentPath = stateProvider.currentState.agentPath
        val disablePluginsParam = stateProvider.currentState.disabledPlugins.toDisabledPluginsParam()
        if (!File(agentPath).exists()) return
        if (stateProvider.currentState.enableAgentForAllConfiguration) {
            applyForConfiguration(agentPath, disablePluginsParam, configuration, javaParameters, project)
        } else if (stateProvider.currentState.selectedRunConfigurations.contains(configuration.name ?: "")) {
            applyForConfiguration(agentPath, disablePluginsParam, configuration, javaParameters, project)
        }
    }

    private fun applyForConfiguration(agentPath: String, disablePluginsParam: String, configuration: RunProfile?, javaParameters: JavaParameters?, project: Project) {
        log.debug("Applying HotSwapAgent to configuration ${configuration?.name ?: ""}")
        ProjectRootManager.getInstance(project).projectSdk?.let { sdk ->
            if (DCEVMUtil.isDCEVMInstalledLikeAltJvm(sdk)) {
                javaParameters?.vmParametersList?.add("-XXaltjvm=dcevm")
            }
            if (!DCEVMUtil.isDCEVMPresent(sdk)) {
                HotSwapAgentPluginNotification.getInstance(project).showNotificationAboutMissingDCEVM()
            }
        }
        javaParameters?.vmParametersList?.add("-javaagent:$agentPath$disablePluginsParam")
    }

    private fun Set<String>.toDisabledPluginsParam() = if (this.isEmpty()) "" else
        "=" + this.joinToString(",") { "disablePlugin=$it" }

}
