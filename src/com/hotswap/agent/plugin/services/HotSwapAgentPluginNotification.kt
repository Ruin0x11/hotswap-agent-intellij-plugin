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

import com.hotswap.agent.plugin.util.Constants.Companion.DCEVM_RELEASES_URL
import com.intellij.ide.BrowserUtil
import com.intellij.notification.*
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import javax.swing.event.HyperlinkEvent

/**
 * @author Dmitry Zhuravlev
 *         Date:  10.03.2017
 */
class HotSwapAgentPluginNotification(private val project: Project?) {
    companion object {
        private val NOTIFICATION_GROUP = NotificationGroupManager.getInstance().getNotificationGroup("HotSwapAgent Notification Group")

        private const val DOWNLOAD_AGENT_EVENT_DESCRIPTION = "download_agent"
        private const val DOWNLOAD_DCEVM_EVENT_DESCRIPTION = "download_dcevm"

        fun getInstance(project: Project? = null) = when (project) {
            null -> ServiceManager.getService<HotSwapAgentPluginNotification>(HotSwapAgentPluginNotification::class.java)!!
            else -> ServiceManager.getService<HotSwapAgentPluginNotification>(project, HotSwapAgentPluginNotification::class.java)!!
        }
    }

    fun showNotificationAboutNewAgentVersion(downloadAction: () -> Unit) {
        val message = """<a href=$DOWNLOAD_AGENT_EVENT_DESCRIPTION>Download and apply</a> new version of HotSwapAgent."""
        HotSwapAgentPluginNotification.getInstance(project).showBalloon(
                "New HotSwapAgent version available",
                message, NotificationType.INFORMATION, object : NotificationListener.Adapter() {
            override fun hyperlinkActivated(notification: Notification, e: HyperlinkEvent) {
                notification.expire()
                if (DOWNLOAD_AGENT_EVENT_DESCRIPTION == e.description) {
                    downloadAction()
                }
            }
        })
    }

    fun showNotificationAboutMissingDCEVM() {
        val message = """HotSwap will not work. <a href=$DOWNLOAD_DCEVM_EVENT_DESCRIPTION>Download</a> and install DCEVM."""
        HotSwapAgentPluginNotification.getInstance(project).showBalloon(
                "DCEVM installation not found",
                message, NotificationType.WARNING, object : NotificationListener.Adapter() {
            override fun hyperlinkActivated(notification: Notification, e: HyperlinkEvent) {
                notification.expire()
                if (DOWNLOAD_DCEVM_EVENT_DESCRIPTION == e.description) {
                    BrowserUtil.browse(DCEVM_RELEASES_URL)
                }
            }
        })
    }

    private fun showBalloon(title: String,
                            message: String,
                            type: NotificationType,
                            listener: NotificationListener? = null) {
        NOTIFICATION_GROUP.createNotification(title, message, type, listener).notify(project)
    }
}