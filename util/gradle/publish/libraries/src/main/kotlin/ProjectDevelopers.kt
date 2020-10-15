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

/**
 * The list of the developers working on the Projects
 * @author Davide Farella
 */
internal val EasyPublishExtension.applyDevelopers: EasyPublishExtension.DevelopersBuilder.() -> Unit get() = {
    developers {

        developer {
            id = "4face"
            name = "Davide Farella"
            email = "4face91@protonmail.com"
        }

        developer {
            name = "Mateusz Markowicz"
            email = "poniekad@protonmail.com"
            id = email
        }

        developer {
            name = "Algirdas Pundzius"
            email = "algirdas.pundzius@protonmail.com"
            id = email
        }

        developer {
            name = "Dino Kadrikj"
            email = "kadrikj@protonmail.com"
            id = email
        }
    }
}
