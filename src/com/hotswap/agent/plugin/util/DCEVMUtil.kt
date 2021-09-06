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

import com.github.dcevm.installer.ConfigurationInfo
import com.github.dcevm.installer.Installation
import com.intellij.openapi.projectRoots.Sdk
import java.nio.file.Paths

/**
 * @author Dmitry Zhuravlev
 *         Date:  13.03.2017
 */
class DCEVMUtil {
    companion object {
        fun isDCEVMInstalledLikeAltJvm(projectSdk: Sdk): Boolean {
            val jdkPathString = projectSdk.javaSdk?.homePath ?: return false
            val jdkPath = Paths.get(jdkPathString) ?: return false
            return Installation(ConfigurationInfo.current(), jdkPath).isDCEInstalledAltjvm
        }

        fun isDCEVMPresent(projectSdk: Sdk): Boolean {
            val jdkPathString = projectSdk.javaSdk?.homePath ?: return false
            val jdkPath = Paths.get(jdkPathString) ?: return false
            val installation = Installation(ConfigurationInfo.current(), jdkPath)
            return installation.isDCEInstalled || installation.isDCEInstalledAltjvm
        }

        fun determineDCEVMVersion(projectSdk: Sdk): String? {
            val jdkPathString = projectSdk.javaSdk?.homePath ?: return null
            val jdkPath = Paths.get(jdkPathString) ?: return null
            val installation = Installation(ConfigurationInfo.current(), jdkPath)
            return when {
                installation.isDCEInstalled -> installation.versionDcevm
                installation.isDCEInstalledAltjvm -> installation.versionDcevmAltjvm
                else -> null
            }
        }

        private val Sdk.javaSdk get() = this
    }
}