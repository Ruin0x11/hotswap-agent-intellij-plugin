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
package com.hotswap.agent.plugin.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

/**
 * @author Dmitry Zhuravlev
 *         Date:  09.03.2017
 */
@State(
        name = "HotSwapAgentPluginSettingsProvider",
        storages = [(Storage("hotswap_agent.xml"))]
)
class HotSwapAgentPluginSettingsProvider : PersistentStateComponent<HotSwapAgentPluginSettingsProvider.State> {
    companion object{
        fun getInstance(project: Project): HotSwapAgentPluginSettingsProvider {
            return ServiceManager.getService(project, HotSwapAgentPluginSettingsProvider::class.java)
        }
    }
    class State {
        var agentPath = ""
        var enableAgentForAllConfiguration = false
        var selectedRunConfigurations = mutableSetOf<String>()
        var disabledPlugins = mutableSetOf<String>()
    }

    var currentState = State()

    override fun getState() = currentState

    override fun loadState(state: State) {
        currentState.agentPath = state.agentPath
        currentState.enableAgentForAllConfiguration = state.enableAgentForAllConfiguration
        currentState.selectedRunConfigurations = state.selectedRunConfigurations
        currentState.disabledPlugins = state.disabledPlugins
    }
}