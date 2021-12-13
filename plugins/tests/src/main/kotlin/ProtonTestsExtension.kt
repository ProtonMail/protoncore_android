import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import javax.inject.Inject

/*
 * Copyright (c) 2021 Proton Technologies AG
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

open class ProtonTestsExtension @Inject constructor () {

    var unitTestFlavor: String? = null

    companion object {
        fun Project.setupProtonTestsExtension(): ProtonTestsExtension {
            return extensions.create("protonTestsOptions", ProtonTestsExtension::class.java)
        }
    }
}
