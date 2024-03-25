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


plugins {
    protonAndroidApp
    protonDagger
    id("me.proton.core.gradle-plugins.environment-config")
    kotlin("plugin.serialization")
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
        preference
    )
}
