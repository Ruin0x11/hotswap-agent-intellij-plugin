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

/**
 * @author Dmitry Zhuravlev
 *         Date:  10.03.2017
 */
class Constants {
    companion object {
        const val MIN_AGENT_VERSION = "1.0"
        const val AGENT_LATEST_RELEASE_API_URL = "https://api.github.com/repos/HotswapProjects/HotswapAgent/releases/latest"
        const val AGENT_DOWNLOAD_URL = "https://github.com/HotswapProjects/HotswapAgent/releases/download"
        const val DCEVM_RELEASES_URL = "https://github.com/dcevm/dcevm/releases"
        const val DCEVM_HOW_TO_INSTALL_URL = "https://github.com/dmitry-zhuravlev/hotswap-agent-intellij-plugin#dcevm-installation"
    }
}