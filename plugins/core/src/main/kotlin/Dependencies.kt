/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
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

// region AndroidX
public val DependencyHandler.`androidx-browser`: Any
    get() = androidx("browser", "browser") version `androidx-browser version`
public val DependencyHandler.`androidx-core`: Any
    get() = androidx("core") version `androidx-core version`
public val DependencyHandler.`core-splashscreen`: Any
    get() = androidx("core", moduleSuffix = "splashscreen") version `core-splashscreen version`
// end region

// region Compose
public val DependencyHandler.`activity-compose`: Any
    get() = androidx("activity", moduleSuffix = "compose") version `activity version`
public val DependencyHandler.`navigation-compose`: Any
    get() = androidx("navigation", moduleSuffix = "compose") version `navigation version`
public val DependencyHandler.`hilt-navigation-compose`: Any
    get() = androidx("hilt", moduleSuffix = "navigation-compose") version `hilt-navigation-compose version`
public val DependencyHandler.`lifecycle-viewModel-compose`: Any
    get() = androidx("lifecycle", moduleSuffix = "viewmodel-compose") version `lifecycle version`
public val DependencyHandler.`lifecycle-runtime-compose`: Any
    get() = androidx("lifecycle", moduleSuffix = "runtime-compose") version `lifecycle version`
public val DependencyHandler.`androidx-tv-foundation`: Any
    get() = androidx("tv", moduleSuffix = "foundation") version `tv-foundation version`
public val DependencyHandler.`androidx-tv-material`: Any
    get() = androidx("tv", moduleSuffix = "material") version `tv-material version`
public fun DependencyHandler.compose(
    module: String,
    moduleSuffix: String? = null,
    version: String = `compose version`
): Any = androidx("compose.$module", module, moduleSuffix, version = version)

public val DependencyHandler.`compose-animation`: Any
    get() = compose("animation", version = `composeAnimation version`)
public val DependencyHandler.`compose-animation-core`: Any
    get() = compose("animation", moduleSuffix = "core", version = `composeAnimation version`)
public val DependencyHandler.`compose-compiler`: Any
    get() = compose("compiler") version `compose compiler version`
public val DependencyHandler.`compose-foundation`: Any
    get() = compose("foundation", version = `composeFoundation version`)
public val DependencyHandler.`compose-foundation-layout`: Any
    get() = compose("foundation", "layout", version = `composeFoundation version`)
public val DependencyHandler.`compose-material`: Any
    get() = compose("material", version = `composeMaterial version`)
public val DependencyHandler.`compose-material-icons-core`: Any
    get() = compose("material", moduleSuffix = "icons-core", version = `composeMaterial version`)
public val DependencyHandler.`compose-material3`: Any
    get() = compose("material3", version = `material3 version`)
public val DependencyHandler.`compose-runtime`: Any
    get() = compose("runtime", version = `composeRuntime version`)
public val DependencyHandler.`compose-ui`: Any
    get() = compose("ui", version = `composeUi version`)
public val DependencyHandler.`compose-ui-graphics`: Any
    get() = compose("ui", moduleSuffix = "graphics", version = `composeUi version`)
public val DependencyHandler.`compose-ui-tooling`: Any
    get() = compose("ui", "tooling", version = `composeUi version`)
public val DependencyHandler.`compose-ui-tooling-preview`: Any
    get() = compose("ui", "tooling-preview", version = `composeUi version`)
public val DependencyHandler.`compose-ui-test`: Any
    get() = compose("ui", "test", version = `composeUi version`)
public val DependencyHandler.`compose-ui-text`: Any
    get() = compose("ui", "text", version = `composeUi version`)
public val DependencyHandler.`compose-ui-test-junit`: Any
    get() = compose("ui", "test-junit4", version = `composeUi version`)
public val DependencyHandler.`compose-ui-test-manifest`: Any
    get() = compose("ui", "test-manifest", version = `composeUi version`)
public val DependencyHandler.`compose-ui-unit`: Any
    get() = compose("ui", "unit", version = `composeUi version`)
// endregion

// Network
public val DependencyHandler.miniDns: Any
    get() = dependency("org.minidns", module = "minidns-core") version `miniDns version`
public val DependencyHandler.`retrofit-scalars-converter`: Any
    get() = squareup("retrofit2", "converter-scalars") version `retrofit version`
public val DependencyHandler.okhttp: Any
    get() = squareup("okhttp3", "okhttp") version `okHttp version`
public val DependencyHandler.mockWebServer: Any
    get() = squareup("okhttp3", "mockwebserver") version `okHttp version`

// Other
public val DependencyHandler.`activity-noktx`: Any
    get() = androidx("activity") version `activity version`
public val DependencyHandler.`androidx-collection`: Any
    get() = androidx("collection") version `androidx-collection version`
public val DependencyHandler.`androidx-navigation-common`: Any
    get() = androidx("navigation", moduleSuffix = "common") version `navigation version`
public val DependencyHandler.`apacheCommon-codec`: Any
    get() = dependency("commons-codec", module = "commons-codec") version `apacheCommon-codec version`
public val DependencyHandler.`appcompat-resources`: Any
    get() = androidx("appcompat", moduleSuffix = "resources") version `appcompat version`
public val DependencyHandler.bcrypt: Any
    get() = dependency("at.favre.lib", module = "bcrypt") version `bcrypt version`
public val DependencyHandler.coil: Any
    get() = dependency("io.coil-kt", module = "coil") version `coil version`
public val DependencyHandler.coilSvg: Any
    get() = dependency("io.coil-kt", module = "coil-svg") version `coil version`
public val DependencyHandler.coordinatorlayout: Any
    get() = dependency("androidx.coordinatorlayout", module = "coordinatorlayout") version `coordinatorlayout version`
public val DependencyHandler.datastore: Any
    get() = dependency("androidx.datastore", module = "datastore") version `datastore version`
public val DependencyHandler.datastorePreferences: Any
    get() = dependency("androidx.datastore", module = "datastore-preferences") version `datastore version`
public val DependencyHandler.drawerLayout: Any
    get() = androidx("drawerlayout") version `drawerLayout version`
public val DependencyHandler.googlePlayBilling: Any
    get() = dependency("com.android.billingclient", module = "billing-ktx") version `googlePlayBilling version`
public val DependencyHandler.googleTink: Any
    get() = google("crypto.tink", module = "tink-android") version `googleTink version`
public val DependencyHandler.guavaListenableFuture: Any
    get() = dependency("com.google.guava", module = "listenablefuture") version `guavaListenableFuture version`
public val DependencyHandler.leakCanary: Any
    get() = dependency("com.squareup.leakcanary", module = "leakcanary-android") version `leakCanary version`
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
public val DependencyHandler.cache4k: Any
    get() = dependency("io.github.reactivecircus.cache4k", module = "cache4k") version `cache4k version`
public val DependencyHandler.`lifecycle-common`: Any
    get() = androidxLifecycle("common") version `lifecycle version`
public val DependencyHandler.`lifecycle-extensions`: Any
    get() = androidxLifecycle("extensions") version `lifecycle-extensions version`
public val DependencyHandler.`lifecycle-livedata-core`: Any
    get() = androidxLifecycle("livedata-core") version `lifecycle version`
public val DependencyHandler.`lifecycle-process`: Any
    get() = androidxLifecycle("process") version `lifecycle version`
public val DependencyHandler.`lifecycle-savedState`: Any
    get() = androidxLifecycle("viewmodel-savedstate") version `lifecycle version`
public val DependencyHandler.lottie: Any
    get() = dependency("com.airbnb.android", module = "lottie") version `lottie version`
public val DependencyHandler.timber: Any
    get() = dependency("com.jakewharton.timber", module = "timber") version `timber version`
public val DependencyHandler.sentry: Any
    get() = dependency("io.sentry", module = "sentry") version `sentry version`
public val DependencyHandler.`sentry-android-core`: Any
    get() = dependency("io.sentry", module = "sentry-android-core") version `sentry version`
public val DependencyHandler.`sentry-timber`: Any
    get() = dependency("io.sentry", module = "sentry-android-timber") version `sentry version`
public val DependencyHandler.`javax-inject`: Any
    get() = dependency("javax.inject", module = "javax.inject") version `javax-inject version`
public val DependencyHandler.`ez-vcard`: Any
    get() = dependency("com.googlecode.ez-vcard", module = "ez-vcard") version `ez-vcard_version`
public val DependencyHandler.recyclerview: Any
    get() = androidx("recyclerview") version `recyclerview version`
public val DependencyHandler.`startup-runtime`: Any
    get() = androidx("startup", moduleSuffix = "runtime") version `startup-runtime version`
public val DependencyHandler.`serialization-core`: Any
    get() = serialization("core")
public val DependencyHandler.`desugar-jdk-libs`: Any
    get() = dependency("com.android.tools", module = "desugar_jdk_libs") version `desugar_jdk_libs version`
public val DependencyHandler.`swagger-annotations`: Any
    get() = dependency("io.swagger.core.v3", module = "swagger-annotations") version `swagger-annotations version`

// region accessors
public fun DependencyHandler.lint(moduleSuffix: String? = null, version: String = `android-tools version`): Any =
    dependency("android.tools.lint", "lint", moduleSuffix, version)
// endregion

// region tests
public val DependencyHandler.`android-test-core-ktx`: Any
    get() = dependency("androidx.test", module = "core-ktx") version `android-test version`
public val DependencyHandler.`androidx-test-monitor`: Any
    get() = androidx("test", module = "monitor") version `androidx-test-monitor version`
public val DependencyHandler.`androidx-test-orchestrator`: Any
    get() = androidx("test", module = "orchestrator") version `androidx-test-orchestrator version`
public val DependencyHandler.`espresso-contrib`: Any
    get() = androidx("test.espresso", module = "espresso-contrib") version `espresso version`
public val DependencyHandler.`espresso-idling-resource`: Any
    get() = androidx("test.espresso", module = "espresso-idling-resource") version `espresso version`
public val DependencyHandler.`espresso-intents`: Any
    get() = androidx("test.espresso", module = "espresso-intents") version `espresso version`
public val DependencyHandler.`espresso-web`: Any
    get() = androidx("test.espresso", module = "espresso-web") version `espresso version`
public val DependencyHandler.hamcrest: Any
    get() = dependency("org.hamcrest", module = "hamcrest") version `hamcrest version`
public val DependencyHandler.jsonsimple: Any
    get() = dependency("com.googlecode.json-simple", module = "json-simple") version `json-simple version`
public val DependencyHandler.junit: Any
    get() = dependency("junit", module = "junit") version `junit version`
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
public val DependencyHandler.fusion: Any
    get() = dependency("me.proton.test", module = "fusion") version `fusion version`
// endregion
