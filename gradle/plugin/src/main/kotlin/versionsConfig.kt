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
    `kotlin version` =                          "1.4.10"        // Released: Sep 09, 2020
    `coroutines version` =                      "1.4.0-M1"      // Released: Oct 13, 2020
    `serialization version` =                   "1.0.0"         // Released: Oct 08, 2020
    // endregion

    // region Android
    `activity version` =                        "1.2.0-alpha03" // Released: Apr 05, 2020
    `android-annotation version` =              "1.1.0"         // Released: Jun 05, 2019
    `appcompat version` =                       "1.1.0"         // Released: Sep 06, 2019
    `android-arch version` =                    "2.1.0"         // Released: Sep 06, 2019
    `constraint-layout version` =               "2.0.0-beta4"   // Released: Jun 17, 2019
    `espresso version` =                        "3.2.0"         // Released: May 30, 2019
    `fragment version` =                        "1.3.0-alpha04" // Released: Apr 05, 2020
    `ktx version` =                             "1.2.0-beta01"  // Released: Oct 11, 2019
    `lifecycle version` =                       "2.2.0"         // Released: Oct 11, 2019
    `material version` =                        "1.2.1"         // Released: Sep 03, 2020
    `android-paging version` =                  "3.0.0-alpha02" // Released: Jun 24, 2020
    `android-work version` =                    "2.2.0"         // Released: Aug 16, 2019
    `android-room version` =                    "2.2.5"         // Released: Mar 18, 2020

    `android-test version` =                    "1.2.0"         // Released: May 31, 2019
    `robolectric version` =                     "4.4"           // Released: Aug 24, 2020
    // endregion

    // region Others
    `assistedInject version` =                  "0.5.2"         // Released: Nov 22, 2019
    `dagger version` =                          "2.28"          // Released: May 04, 2018
    `hilt-android version` =                    "2.28-alpha"    // Released: Jun 10, 2020
    `hilt-androidx version` =                   "1.0.0-alpha01" // Released: Jun 12, 2020
    `mockK version` =                           "1.10.2"        // Released: Sep 27, 2020
    `retrofit version` =                        "2.9.0"         // Released: May 20, 2020
    `retrofit-kotlin-serialization version` =   "0.8.0"         // Released: Oct 09, 2020
    `timber version` =                          "4.7.1"         // Released:
    `viewStateStore version` =                  "1.4-beta-4"    // Released: Oct 03, 2019
    // endregion
}

// region Android
const val `android-tools version` =         "26.6.3"        // Updated: Apr 17, 2020
const val `androidUi version` =             "0.1.0-dev08"   // Released: Apr 03, 2020
// endregion

// region Other
const val `apacheCommon-codec version` =    "1.14"          // Released: Dec 31, 2019
const val `bcrypt version` =                "0.9.0"         // Released: Oct, 2019
const val `googleTink version` =            "1.4.0"         // Released:
const val `miniDsn version` =               "1.0.0"         // Released: Jul 18, 2020
const val `okHttp version` =                "4.8.0"         // Released: Jul 11, 2020
const val `trustKit version` =              "1.1.3"         // Released: Apr 30, 2020
const val `security-crypto version` =       "1.1.0-alpha02" // Released: Aug 05, 2020

// endregion
