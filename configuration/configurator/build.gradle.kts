import configuration.extensions.protonEnvironment
import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*

/*
 * Copyright (c) 2022 Proton Technologies AG
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

import java.util.*

plugins {
    protonAndroidApp
    protonDagger
    id("me.proton.core.gradle-plugins.environment-config")
    kotlin("plugin.serialization")
}

val rootDirPath = rootDir.path
// Load properties from file
val privateProperties = Properties().apply {
    val propertiesFile = file("$rootDirPath/local.properties")
    if (propertiesFile.exists()) {
        load(propertiesFile.inputStream())
    }
}

// Function to get a property by key with a fallback to an environment variable
fun getProperty(key: String): String {
    // Try to get the property from the environment first, if it's not set, get from the properties file
    return System.getenv(key) ?: privateProperties.getProperty(key, "")
}

protonCoverage {
    disabled.set(true)
}

android {
    namespace = "me.proton.core.configuration.configurator"

    defaultConfig {
        protonEnvironment {
            host = "proton.black"
        }
        buildConfigField("String", "PROXY_URL", "https://proxy.proton.black".toBuildConfigValue())
        buildConfigField("String", "UNLEASH_API_TOKEN", getProperty("UNLEASH_API_TOKEN").toBuildConfigValue())
        buildConfigField("String", "UNLEASH_URL", getProperty("UNLEASH_URL").toBuildConfigValue())
    }

    buildFeatures.compose = true

    composeOptions {
        kotlinCompilerExtensionVersion = `compose compiler version`
    }
}

dependencies {
    api(
        project(Module.presentationCompose),
        `compose-runtime`,
        `compose-ui`,
    )

    implementation(
        project(Module.configurationData),
        project(Module.configurationDaggerContentResolver),
        project(Module.presentation),
        project(Module.networkData),
        project(Module.networkDagger),
        project(Module.quark),
        datastore,
        datastorePreferences,
        `hilt-navigation-compose`,
        `android-ktx`,
        `startup-runtime`,
        `lifecycle-viewModel-compose`,
        appcompat,
        `kotlin-reflect`,
        preference,
        `serialization-json`,
        `retrofit-kotlin-serialization`
    )
}
