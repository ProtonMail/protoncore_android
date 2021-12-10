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

import org.gradle.api.artifacts.dsl.DependencyHandler
import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*

// Network
val DependencyHandler.`miniDsn` get() = dependency("org.minidns", module = "minidns-hla") version `miniDsn version`
val DependencyHandler.`retrofit-scalars-converter` get() = squareup("retrofit2", "converter-scalars") version `retrofit version`
val DependencyHandler.`okhttp-url-connection` get() = squareup("okhttp3", "okhttp-urlconnection") version `okHttp-url-connection version`
val DependencyHandler.`mockWebServer` get() = squareup("okhttp3", "mockwebserver") version `okHttp version`
val DependencyHandler.`trustKit` get() = dependency("com.datatheorem.android.trustkit", module = "trustkit") version `trustKit version`

// Other
val DependencyHandler.`apacheCommon-codec` get() = dependency("commons-codec", module = "commons-codec") version `apacheCommon-codec version`
val DependencyHandler.`bcrypt` get() = dependency("at.favre.lib", module = "bcrypt") version `bcrypt version`
val DependencyHandler.`googleTink` get() = google("crypto.tink", module = "tink-android") version `googleTink version`
val DependencyHandler.`gotev-cookieStore` get() = dependency("net.gotev", module = "cookie-store") version `gotev-cookieStore version`
val DependencyHandler.`lint-core` get() = lint()
val DependencyHandler.`lint-api` get() = lint("api")
val DependencyHandler.`lint-checks` get() = lint("checks")
val DependencyHandler.`lint-tests` get() = lint("tests")
val DependencyHandler.`okHttp-logging` get() = squareup("okhttp3", module = "logging-interceptor") version `okHttp version`
val DependencyHandler.`store4` get() = dependency("com.dropbox.mobile.store", module = "store4") version `store4 version`
val DependencyHandler.`lifecycle-extensions` get() = androidxLifecycle("extensions") version `lifecycle-extensions version`
val DependencyHandler.`lottie` get() = dependency("com.airbnb.android", module = "lottie") version `lottie version`
val DependencyHandler.`javax-inject` get() = dependency("javax.inject", module = "javax.inject") version `javax-inject version`
val DependencyHandler.`ez-vcard` get() = dependency("com.googlecode.ez-vcard", module = "ez-vcard") version `ez-vcard_version`

// region accessors
fun DependencyHandler.lint(moduleSuffix: String? = null, version: String = `android-tools version`) =
    android("tools.lint", "lint", moduleSuffix, version)
// endregion

// region tests
val DependencyHandler.`espresso-contrib` get() = androidx("test.espresso", module = "espresso-contrib") version `espresso version`
val DependencyHandler.`espresso-intents` get() = androidx("test.espresso", module = "espresso-intents") version `espresso version`
val DependencyHandler.`espresso-web` get() = androidx("test.espresso", module = "espresso-web") version `espresso version`
val DependencyHandler.falcon get() = dependency("com.jraska", module = "falcon") version `falcon version`
val DependencyHandler.`orchestrator` get() = androidx("test", module = "orchestrator") version `android-test version`
val DependencyHandler.uiautomator get() = androidx("test.uiautomator", module = "uiautomator") version `uiautomator version`
val DependencyHandler.preference get() = androidx("preference", module = "preference") version `preference version`
val DependencyHandler.`jsonsimple` get() = dependency("com.googlecode.json-simple", module = "json-simple") version `json-simple version`
val DependencyHandler.`turbine` get() = dependency("app.cash.turbine", module = "turbine") version `turbine version`
val DependencyHandler.`junit-ktx` get() = dependency("androidx.test.ext", module = "junit-ktx") version `junit-ktx version`
// endregion
