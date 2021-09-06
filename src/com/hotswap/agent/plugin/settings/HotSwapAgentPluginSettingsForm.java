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
package com.hotswap.agent.plugin.settings;

import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.ui.table.TableView;
import com.intellij.util.execution.ParametersListUtil;

import javax.swing.*;

/**
 * @author Dmitry Zhuravlev
 *         Date:  09.03.2017
 */
public class HotSwapAgentPluginSettingsForm {
    public JButton updateButton;
    public TextFieldWithBrowseButton agentInstallPathField;
    public JBCheckBox applyAgentToAllConfigurationsBox;
    public JPanel rootPanel;
    public JPanel updateButtonPanel;
    public JBLabel dcevmVersionLabel;
    public HyperlinkLabel dcevmDownloadSuggestionLabel;
    public HyperlinkLabel dcevmHowToInstallLabel;
    public HotSwapAgentEnabledConfigurationTableViewProvider configurationTableProvider;

    private TableView configurationsTableView;
    public JTextField disabledPluginsField;

    private void createUIComponents() {
        configurationTableProvider = new HotSwapAgentEnabledConfigurationTableViewProvider();
        configurationsTableView = configurationTableProvider.getTableView();
        disabledPluginsField = new ExpandableTextField(ParametersListUtil.COLON_LINE_PARSER, ParametersListUtil.COLON_LINE_JOINER);
    }
}
