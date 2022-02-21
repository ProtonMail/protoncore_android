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

@file:Suppress("TopLevelPropertyNaming", "ObjectPropertyName", "NoMultipleSpaces")

import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import org.gradle.api.artifacts.VersionCatalog


internal fun initVersions(libs: VersionCatalog) {

    // region Kotlin
    `kotlin version` =                          libs.findVersion("kotlin").get().toString()
    `coroutines version` =                      libs.findVersion("kotlinCoroutines").get().toString()
    `serialization version` =                   libs.findVersion("kotlinSerialization").get().toString()
    // endregion

    // region Android
    `activity version` =                        "1.3.1"         // Released: Aug 04, 2021
    `android-annotation version` =              "1.2.0"         // Released: Mar 24, 2021
    `appcompat version` =                       "1.3.1"         // Released: Jul 21, 2021
    `android-arch version` =                    "2.1.0"         // Released: Sep 06, 2019
    `constraint-layout version` =               "2.1.0"         // Released: Jul 22, 2021
    `espresso version` =                        "3.4.0"         // Released: Jul 04, 2021
    `fragment version` =                        "1.3.6"         // Released: Sep 01, 2021
    `ktx version` =                             "1.7.0"
    `lifecycle version` =                       "2.4.0"
    `material version` =                        "1.5.0"
    `android-paging version` =                  "3.1.0"
    `android-work version` =                    "2.7.1"
    `android-room version` =                    "2.4.1"

    `android-test version` =                    "1.4.0"         // Released: Jun 30, 2021
    `robolectric version` =                     "4.7.3"
    // endregion

    // region Others
    `dagger version` =                          libs.findVersion("daggerHiltAndroid").get().toString()
    `hilt-android version` =                    libs.findVersion("daggerHiltAndroid").get().toString()
    `hilt-androidx version` =                   "1.0.0"         // Released: May 05, 2021
    `mockK version` =                           "1.12.2"
    `retrofit version` =                        "2.9.0"         // Released: May 20, 2020
    `retrofit-kotlin-serialization version` =   "0.8.0"         // Released: Oct 09, 2020
    `timber version` =                          "5.0.1"         // Released: Jun 28, 2018
    // endregion
}

// region Android
public const val `android-tools version`: String =             "30.0.2"        // Updated: Jun, 2020
public const val `compose version`: String =                   "1.2.0-alpha01"
public const val `datastore version`: String =                 "1.0.0"
public const val `hilt-navigation-compose version`: String =   "1.0.0-rc01"
public const val `material3 version`: String =                 "1.0.0-alpha03"
public const val `navigation version`: String =                "2.4.0-rc01"
public const val `startup-runtime version`: String =           "1.1.0"         // Released: Aug 04, 2021
// endregion

// region Other
public const val `apacheCommon-codec version`: String =    "1.15"
public const val `bcrypt version`: String =                "0.9.0"             // Released: Oct 29, 2019
public const val `googleTink version`: String =            "1.6.1"             // Released: Oct 15, 2020
public const val `miniDns version`: String =               "1.0.0"             // Released: Jul 18, 2020
public const val `okHttp version`: String =                "4.9.1"
public const val `trustKit version`: String =              "1.1.5"
public const val `store4 version`: String =                "4.0.4-KT15"        // Released: Dec 10, 2021
public const val `lifecycle-extensions version`: String =  "2.2.0"             // Released: Jan 00, 2020
public const val `lottie version`: String =                "4.1.0"
public const val `javax-inject version`: String =          "1"
public const val `ez-vcard_version`: String =              "0.11.3"

// endregion

// region Tests
public const val `uiautomator version`: String =               "2.2.0"         // Released: Oct 25, 2018
public const val `preference version`: String =                "1.1.1"         // Released: Apr 15, 2020
public const val `json-simple version`: String =               "1.1.1"         // Released: Mar 21, 2012
public const val `turbine version`: String =                   "0.7.0"
public const val `junit-ktx version`: String =                 "1.1.3"
// endregion
