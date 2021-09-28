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

import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*

internal fun initVersions() {

    // region Kotlin
    `kotlin version` =                          "1.5.30"        // Released: Aug 23, 2021
    `coroutines version` =                      "1.5.2"         // Released: Sep 02, 2021
    `serialization version` =                   "1.2.2"         // Released: Jul 8, 2021
    // endregion

    // region Android
    `activity version` =                        "1.3.1"         // Released: Aug 04, 2021
    `android-annotation version` =              "1.2.0"         // Released: Mar 24, 2021
    `appcompat version` =                       "1.3.1"         // Released: Jul 21, 2021
    `android-arch version` =                    "2.1.0"         // Released: Sep 06, 2019
    `constraint-layout version` =               "2.1.0"         // Released: Jul 22, 2021
    `espresso version` =                        "3.4.0"         // Released: Jul 04, 2021
    `fragment version` =                        "1.3.6"         // Released: Sep 01, 2021
    `ktx version` =                             "1.6.0"         // Released: Jun 30, 2021
    `lifecycle version` =                       "2.4.0-alpha03" // Released: Aug 04, 2021
    `material version` =                        "1.4.0"         // Released: Jul 02, 2021
    `android-paging version` =                  "3.0.1"         // Released: Jul 21, 2021
    `android-work version` =                    "2.6.0"         // Released: Sep 01, 2021
    `android-room version` =                    "2.3.0"         // Released: Apr 21, 2021

    `android-test version` =                    "1.4.0"         // Released: Jun 30, 2021
    `robolectric version` =                     "4.6.1"         // Released: Jul 04, 2021
    // endregion

    // region Others
    `dagger version` =                          "2.38.1"        // Released: Jul 27, 2021
    `hilt-android version` =                    "2.38.1"        // Released: Jul 27, 2021
    `hilt-androidx version` =                   "1.0.0"         // Released: May 05, 2021
    `mockK version` =                           "1.11.0"
    `retrofit version` =                        "2.9.0"         // Released: May 20, 2020
    `retrofit-kotlin-serialization version` =   "0.8.0"         // Released: Oct 09, 2020
    `timber version` =                          "5.0.1"         // Released: Jun 28, 2018
    `viewStateStore version` =                  "1.4-beta-4"    // Released: Oct 03, 2019
    // endregion
}

// region Android
const val `android-tools version` =         "30.0.2"        // Updated: Jun, 2020
const val `androidUi version` =             "0.1.0-dev08"   // Released: Apr 03, 2020
// endregion

// region Other
const val `apacheCommon-codec version` =    "1.15"
const val `bcrypt version` =                "0.9.0"         // Released: Oct 29, 2019
const val `gotev-cookieStore version` =     "1.3.5"
const val `googleTink version` =            "1.6.1"         // Released: Oct 15, 2020
const val `miniDsn version` =               "1.0.0"         // Released: Jul 18, 2020
const val `okHttp version` =                "4.9.1"
const val `okHttp-url-connection version` = "4.9.1"
const val `trustKit version` =              "1.1.5"
const val `store4 version` =                "4.0.2-KT15"    // Released: May 17, 2021
const val `lifecycle-extensions version` =  "2.2.0"         // Released: Jan 00, 2020
const val `lottie version` =                "4.1.0"
const val `javax-inject version` =          "1"

// endregion

// region Tests
const val `falcon version` =                    "2.2.0"         // Released: Sep 24, 2018
const val `uiautomator version` =               "2.2.0"         // Released: Oct 25, 2018
const val `preference version` =                "1.1.1"         // Released: Apr 15, 2020
const val `json-simple version` =               "1.1.1"         // Released: Mar 21, 2012
const val `turbine version` =                   "0.6.1"
const val `junit-ktx version` =                 "1.1.3"
// endregion
