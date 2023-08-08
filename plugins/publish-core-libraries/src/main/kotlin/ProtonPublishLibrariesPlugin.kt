/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
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

const val CORE_RELEASE_BRANCH_PREFIX = "release/libs/"

/**
 * Setup Publishing for whole Project.
 *
 * Setup sub-projects by generating KDoc, generating aar, updating readme, sign and publish new versions to Maven.
 */
abstract class ProtonPublishLibrariesPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val groupName = "me.proton.core"
        val versionName = target.computeVersionNameFromBranchName(CORE_RELEASE_BRANCH_PREFIX)
        target.setupPublishingTasks(groupName, versionName)
        target.setupNotifyNewReleaseTask(versionName)
        target.setupTagReleaseTask("$CORE_RELEASE_BRANCH_PREFIX$versionName")
        target.subprojects {
            setupSubProjectPublishing(groupName = groupName, versionName = versionName)
        }
    }
}
