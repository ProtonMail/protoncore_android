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

import studio.forface.easygradle.publish.EasyPublishExtension

internal val EasyPublishExtension.applyLicences: EasyPublishExtension.LicensesBuilder.() -> Unit
    get() = {
        licenses {
            license {
                name = "GNU GENERAL PUBLIC LICENSE, Version 3.0"
                url = "https://www.gnu.org/licenses/gpl-3.0.en.html"
            }
        }
    }