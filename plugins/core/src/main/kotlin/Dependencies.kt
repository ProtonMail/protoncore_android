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

@file:Suppress("TopLevelPropertyNaming", "ObjectPropertyName")

import org.gradle.api.artifacts.dsl.DependencyHandler
import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*

// region Compose
public val DependencyHandler.`activity-compose`: Any
    get() = androidx("activity", moduleSuffix = "compose") version `activity version`
public val DependencyHandler.`navigation-compose`: Any
    get() = androidx("navigation", moduleSuffix = "compose") version `navigation version`
public val DependencyHandler.`hilt-navigation-compose`: Any
    get() = androidx("hilt", moduleSuffix = "navigation-compose") version `hilt-navigation-compose version`
public val DependencyHandler.`lifecycle-compose`: Any
    get() = androidx("lifecycle", moduleSuffix = "compose") version `hilt-navigation-compose version`

public fun DependencyHandler.compose(
    module: String,
    moduleSuffix: String? = null,
    version: String = `compose version`
): Any = androidx("compose.$module", module, moduleSuffix, version = version)

public val DependencyHandler.`compose-animation`: Any
    get() = compose("animation")
public val DependencyHandler.`compose-compiler`: Any
    get() = compose("compiler")
public val DependencyHandler.`compose-foundation`: Any
    get() = compose("foundation")
public val DependencyHandler.`compose-foundation-layout`: Any
    get() = compose("foundation", "layout")
public val DependencyHandler.`compose-material`: Any
    get() = compose("material")
public val DependencyHandler.`compose-material3`: Any
    get() = compose("material3", version = `material3 version`)
public val DependencyHandler.`compose-runtime`: Any
    get() = compose("runtime")
public val DependencyHandler.`compose-ui`: Any
    get() = compose("ui")
public val DependencyHandler.`compose-ui-tooling`: Any
    get() = compose("ui", "tooling")
public val DependencyHandler.`compose-ui-test`: Any
    get() = compose("ui", "test")
public val DependencyHandler.`compose-ui-test-junit`: Any
    get() = compose("ui", "test-junit4")
// endregion

// Network
public val DependencyHandler.miniDns: Any
    get() = dependency("org.minidns", module = "minidns-hla") version `miniDns version`
public val DependencyHandler.`retrofit-scalars-converter`: Any
    get() = squareup("retrofit2", "converter-scalars") version `retrofit version`
public val DependencyHandler.`okhttp-url-connection`: Any
    get() = squareup("okhttp3", "okhttp-urlconnection") version `okHttp-url-connection version`
public val DependencyHandler.mockWebServer: Any
    get() = squareup("okhttp3", "mockwebserver") version `okHttp version`
public val DependencyHandler.trustKit: Any
    get() = dependency("com.datatheorem.android.trustkit", module = "trustkit") version `trustKit version`

// Other
public val DependencyHandler.`apacheCommon-codec`: Any
    get() = dependency("commons-codec", module = "commons-codec") version `apacheCommon-codec version`
public val DependencyHandler.bcrypt: Any
    get() = dependency("at.favre.lib", module = "bcrypt") version `bcrypt version`
public val DependencyHandler.googleTink: Any
    get() = google("crypto.tink", module = "tink-android") version `googleTink version`
public val DependencyHandler.`gotev-cookieStore`: Any
    get() = dependency("net.gotev", module = "cookie-store") version `gotev-cookieStore version`
public val DependencyHandler.`lint-core`: Any
    get() = lint()
public val DependencyHandler.`lint-api`: Any
    get() = lint("api")
public val DependencyHandler.`lint-checks`: Any
    get() = lint("checks")
public val DependencyHandler.`lint-tests`: Any
    get() = lint("tests")
public val DependencyHandler.`okHttp-logging`: Any
    get() = squareup("okhttp3", module = "logging-interceptor") version `okHttp version`
public val DependencyHandler.store4: Any
    get() = dependency("com.dropbox.mobile.store", module = "store4") version `store4 version`
public val DependencyHandler.`lifecycle-common`: Any
    get() = androidxLifecycle("common") version `lifecycle version`
public val DependencyHandler.`lifecycle-extensions`: Any
    get() = androidxLifecycle("extensions") version `lifecycle-extensions version`
public val DependencyHandler.`lifecycle-process`: Any
    get() = androidxLifecycle("process") version `lifecycle version`
public val DependencyHandler.lottie: Any
    get() = dependency("com.airbnb.android", module = "lottie") version `lottie version`
public val DependencyHandler.`javax-inject`: Any
    get() = dependency("javax.inject", module = "javax.inject") version `javax-inject version`
public val DependencyHandler.`ez-vcard`: Any
    get() = dependency("com.googlecode.ez-vcard", module = "ez-vcard") version `ez-vcard_version`
public val DependencyHandler.`startup-runtime`: Any
    get() = androidx("startup", moduleSuffix = "runtime") version `startup-runtime version`

// region accessors
public fun DependencyHandler.lint(moduleSuffix: String? = null, version: String = `android-tools version`) =
    dependency("android.tools.lint", "lint", moduleSuffix, version)
// endregion

// region tests
public val DependencyHandler.`android-test-core-ktx`: Any
    get() = dependency("androidx.test", module = "core-ktx") version `android-test version`
public val DependencyHandler.`espresso-contrib`: Any
    get() = androidx("test.espresso", module = "espresso-contrib") version `espresso version`
public val DependencyHandler.`espresso-intents`: Any
    get() = androidx("test.espresso", module = "espresso-intents") version `espresso version`
public val DependencyHandler.`espresso-web`: Any
    get() = androidx("test.espresso", module = "espresso-web") version `espresso version`
public val DependencyHandler.jsonsimple: Any
    get() = dependency("com.googlecode.json-simple", module = "json-simple") version `json-simple version`
public val DependencyHandler.`junit-ktx`: Any
    get() = dependency("androidx.test.ext", module = "junit-ktx") version `junit-ktx version`
public val DependencyHandler.orchestrator: Any
    get() = androidx("test", module = "orchestrator") version `android-test version`
public val DependencyHandler.preference: Any
    get() = androidx("preference", module = "preference") version `preference version`
public val DependencyHandler.turbine: Any
    get() = dependency("app.cash.turbine", module = "turbine") version `turbine version`
public val DependencyHandler.uiautomator: Any
    get() = androidx("test.uiautomator", module = "uiautomator") version `uiautomator version`
// endregion
