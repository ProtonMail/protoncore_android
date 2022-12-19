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

import org.gradle.api.artifacts.VersionCatalog
import studio.forface.easygradle.dsl.android.`activity version`
import studio.forface.easygradle.dsl.android.`android-annotation version`
import studio.forface.easygradle.dsl.android.`android-arch version`
import studio.forface.easygradle.dsl.android.`android-paging version`
import studio.forface.easygradle.dsl.android.`android-room version`
import studio.forface.easygradle.dsl.android.`android-test version`
import studio.forface.easygradle.dsl.android.`android-work version`
import studio.forface.easygradle.dsl.android.`appcompat version`
import studio.forface.easygradle.dsl.android.`constraint-layout version`
import studio.forface.easygradle.dsl.android.`espresso version`
import studio.forface.easygradle.dsl.android.`fragment version`
import studio.forface.easygradle.dsl.android.`hilt-android version`
import studio.forface.easygradle.dsl.android.`hilt-androidx version`
import studio.forface.easygradle.dsl.android.`ktx version`
import studio.forface.easygradle.dsl.android.`lifecycle version`
import studio.forface.easygradle.dsl.android.`material version`
import studio.forface.easygradle.dsl.android.`retrofit version`
import studio.forface.easygradle.dsl.android.`retrofit-kotlin-serialization version`
import studio.forface.easygradle.dsl.android.`robolectric version`
import studio.forface.easygradle.dsl.android.`timber version`
import studio.forface.easygradle.dsl.`coroutines version`
import studio.forface.easygradle.dsl.`dagger version`
import studio.forface.easygradle.dsl.`kotlin version`
import studio.forface.easygradle.dsl.`mockK version`
import studio.forface.easygradle.dsl.`serialization version`


internal fun initVersions(libs: VersionCatalog) {

    // region Kotlin
    `kotlin version` =                          libs.findVersion("kotlin").get().toString()
    `coroutines version` =                      libs.findVersion("kotlinCoroutines").get().toString()
    `serialization version` =                   libs.findVersion("kotlinSerialization").get().toString()
    // endregion

    // region Android
    `activity version` =                        "1.6.1"
    `android-annotation version` =              "1.2.0"
    `appcompat version` =                       "1.5.1"
    `android-arch version` =                    "2.1.0"         // Released: Sep 06, 2019
    `constraint-layout version` =               "2.1.4"
    `espresso version` =                        "3.5.0"
    `fragment version` =                        "1.5.5"
    `ktx version` =                             "1.9.0"
    `lifecycle version` =                       "2.5.1"
    `material version` =                        "1.7.0"
    `android-paging version` =                  "3.1.0"
    `android-work version` =                    "2.7.1"
    `android-room version` =                    "2.4.3"

    `android-test version` =                    "1.5.0"
    `robolectric version` =                     "4.9.1"
    // endregion

    // region Others
    `dagger version` =                          libs.findVersion("daggerHiltAndroid").get().toString()
    `hilt-android version` =                    libs.findVersion("daggerHiltAndroid").get().toString()
    `hilt-androidx version` =                   "1.0.0"         // Released: May 05, 2021
    `mockK version` =                           "1.13.3"
    `retrofit version` =                        "2.9.0"         // Released: May 20, 2020
    `retrofit-kotlin-serialization version` =   "0.8.0"         // Released: Oct 09, 2020
    `timber version` =                          "5.0.1"         // Released: Jun 28, 2018
    // endregion
}

// region Android
public const val `android-tools version`: String =             "30.0.3"
public const val `core-splashscreen version`: String =         "1.0.0"
public const val `androidx-collection version`: String =       "1.2.0"
public const val `compose version`: String =                   "1.3.2"
public const val `composeFoundation version`: String =         "1.3.1"
public const val `composeMaterial version`: String =           "1.3.1"
public const val `coordinatorlayout version`: String =         "1.2.0"
public const val `googlePlayBilling version`: String =         "5.1.0"
public const val `datastore version`: String =                 "1.0.0"
public const val `drawerLayout version`: String =              "1.1.1"
public const val `hilt-navigation-compose version`: String =   "1.0.0"
public const val `material3 version`: String =                 "1.0.1"
public const val `navigation version`: String =                "2.5.3"
public const val `recyclerview version`: String =              "1.2.1"
public const val `startup-runtime version`: String =           "1.1.1"
// endregion

// region Other
public const val `apacheCommon-codec version`: String =    "1.15"
public const val `bcrypt version`: String =                "0.9.0"             // Released: Oct 29, 2019
public const val `googleTink version`: String =            "1.7.0"
public const val `guavaListenableFuture version`: String = "1.0"
public const val `leakCanary version`: String =            "2.10"
public const val `miniDns version`: String =               "1.0.4"
public const val `okHttp version`: String =                "4.10.0"
public const val `store4 version`: String =                "4.0.5"
public const val `lifecycle-extensions version`: String =  "2.2.0"             // Released: Jan 00, 2020
public const val `lottie version`: String =                "4.1.0"
public const val `javax-inject version`: String =          "1"
public const val `ez-vcard_version`: String =              "0.11.3"
public const val `desugar_jdk_libs version`: String =      "1.2.2"
// endregion

// region Tests
public const val `androidx-test-monitor version`: String =      "1.6.0"
public const val `androidx-test-orchestrator version`: String = "1.4.2"
public const val `hamcrest version`: String =                   "2.2"
public const val `uiautomator version`: String =                "2.3.0-alpha01"
public const val `preference version`: String =                 "1.1.1"         // Released: Apr 15, 2020
public const val `json-simple version`: String =                "1.1.1"         // Released: Mar 21, 2012
public const val `turbine version`: String =                    "0.12.1"
public const val `junit version`: String =                      "4.13.2"
public const val `junit-ktx version`: String =                  "1.1.4"
// endregion
