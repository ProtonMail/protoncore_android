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

package me.proton.core.util.gradle

import org.gradle.api.Project

/**
 * @return [List] of [Project] where the first item is the Root Project and the last on is the receiver one
 * @author Davide Farella
 */
@OptIn(ExperimentalStdlibApi::class)
fun Project.hierarchy() = buildList {
    var current: Project? = this@hierarchy
    while (current != null) {
        add(0, current)
        current = current.parent
    }
}
