/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Setup Publishing for whole Project.
 *
 * Setup sub-projects by generating KDoc, generating aar, updating readme, sign and publish new versions to Maven.
 */
abstract class ProtonPublishLibrariesPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val groupName = "me.proton.core"
        val versionName = target.computeVersionName()
        target.setupPublishingTasks(groupName, versionName)
        target.setupNotifyNewReleaseTask(versionName)
        target.setupTagReleaseTask(versionName)
        target.subprojects {
            setupSubProjectPublishing(groupName = groupName, versionName = versionName)
        }
    }
}
