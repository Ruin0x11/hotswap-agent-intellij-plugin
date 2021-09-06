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

import com.hotswap.agent.plugin.services.DownloadManager
import com.hotswap.agent.plugin.util.Constants.Companion.DCEVM_HOW_TO_INSTALL_URL
import com.hotswap.agent.plugin.util.Constants.Companion.DCEVM_RELEASES_URL
import com.hotswap.agent.plugin.util.DCEVMUtil
import com.hotswap.agent.plugin.util.HotSwapAgentPathUtil
import com.intellij.execution.RunManager
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.ui.DocumentAdapter
import com.intellij.util.execution.ParametersListUtil
import java.awt.CardLayout
import java.awt.Color
import java.io.File
import java.util.*
import javax.swing.JComponent
import javax.swing.event.DocumentEvent

/**
 * @author Dmitry Zhuravlev
 *         Date:  09.03.2017
 */
class HotSwapAgentPluginSettingsConfigurable(project: Project) : Configurable {
    companion object {
        val bundle = ResourceBundle.getBundle("HotSwapAgentIntellijPluginBundle")!!
        private const val DCEVM_NOT_DETERMINED = "<not determined>"
    }

    private var stateChanged: Boolean = false
    private val form = HotSwapAgentPluginSettingsForm()
    private val downloadManager = DownloadManager.getInstance(project)
    private val projectRootManager = ProjectRootManager.getInstance(project)
    private val stateProvider = HotSwapAgentPluginSettingsProvider.getInstance(project)
    private val runManager = RunManager.getInstance(project)

    override fun isModified() = stateChanged

    override fun getDisplayName() = bundle.getString("settings.hotswap.plugin.name")


    override fun apply() {
        stateProvider.currentState.agentPath = form.agentInstallPathField.text
        stateProvider.currentState.enableAgentForAllConfiguration = form.applyAgentToAllConfigurationsBox.isSelected
        stateProvider.currentState.selectedRunConfigurations = form.configurationTableProvider.getSelectedConfigurationNames()
        stateProvider.currentState.disabledPlugins = form.disabledPluginsField.text.parse()
        showUpdateButton()
        stateChanged = false
    }

    override fun createComponent(): JComponent? {
        setupFormComponents()
        return form.rootPanel
    }

    override fun reset() {
        form.agentInstallPathField.text = stateProvider.currentState.agentPath
        form.applyAgentToAllConfigurationsBox.isSelected = stateProvider.currentState.enableAgentForAllConfiguration
        form.disabledPluginsField.text = stateProvider.currentState.disabledPlugins.joinString()
        stateChanged = false
    }

    override fun getHelpTopic() = null

    private fun setupFormComponents() {
        projectRootManager.projectSdk?.let { sdk ->
            form.dcevmVersionLabel.text = DCEVMUtil.determineDCEVMVersion(sdk) ?: DCEVM_NOT_DETERMINED
        }
        form.agentInstallPathField.addBrowseFolderListener(null, null, null, FileChooserDescriptor(false, false, true, true, false, false))
        form.agentInstallPathField.textField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(event: DocumentEvent) {
                stateChanged = form.agentInstallPathField.textField.text != stateProvider.currentState.agentPath
            }
        })
        form.disabledPluginsField.document.addDocumentListener(object: DocumentAdapter(){
            override fun textChanged(event: DocumentEvent) {
                stateChanged = form.disabledPluginsField.text != stateProvider.currentState.disabledPlugins.joinString()
            }
        })
        form.applyAgentToAllConfigurationsBox.addItemListener {
            stateChanged = form.applyAgentToAllConfigurationsBox.isSelected != stateProvider.currentState.enableAgentForAllConfiguration
            form.configurationTableProvider.tableView.isEnabled = !form.applyAgentToAllConfigurationsBox.isSelected
        }
        form.updateButton.addActionListener {
            with(downloadManager) {
                downloadAgentJarSynchronously(artifactToDownload = getLatestAgentDescriptorOrDefault(), canBeCanceled = false) { downloadedAgentPath ->
                    form.agentInstallPathField.textField.text = downloadedAgentPath
                }
            }
        }
        form.dcevmDownloadSuggestionLabel.apply {
            setHtmlText("""
                   DCEVM installation not found for JDK specified for the current project.
                   You should <a>download</a> and""")
            foreground = Color.red
            setHyperlinkTarget(DCEVM_RELEASES_URL)
            isVisible = form.dcevmVersionLabel.text == DCEVM_NOT_DETERMINED
        }
        form.dcevmHowToInstallLabel.apply {
            setHtmlText("""<a>install</a> it.""")
            foreground = Color.red
            setHyperlinkTarget(DCEVM_HOW_TO_INSTALL_URL)
            isVisible = form.dcevmVersionLabel.text == DCEVM_NOT_DETERMINED
        }
        form.configurationTableProvider.apply {
            addModelChangeListener {
                stateChanged = stateProvider.currentState.selectedRunConfigurations != form.configurationTableProvider.getSelectedConfigurationNames()
            }
            setItems(runManager.allConfigurationsList.toTableItems())
            setSelected(stateProvider.currentState.selectedRunConfigurations)
        }
    }

    private fun showUpdateButton() {
        val currentVersion = HotSwapAgentPathUtil.determineAgentVersionFromPath(stateProvider.currentState.agentPath)
        val show = currentVersion != null && File(stateProvider.currentState.agentPath).exists() && downloadManager.isLatestAgentVersionAvailable(currentVersion)
        if (show) {
            (form.updateButtonPanel.layout as CardLayout).show(form.updateButtonPanel, "cardWithUpdateButton")
        } else {
            (form.updateButtonPanel.layout as CardLayout).show(form.updateButtonPanel, "emptyCard")
        }
    }

    private fun String.parse() = ParametersListUtil.COLON_LINE_PARSER.`fun`(this).map(String::trim).toMutableSet()

    private fun Set<String>.joinString() = ParametersListUtil.COLON_LINE_JOINER.`fun`(this.toList())
}