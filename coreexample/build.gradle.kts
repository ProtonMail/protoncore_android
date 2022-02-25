/*
 * Copyright (c) 2021 Proton Technologies AG
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

import com.android.build.gradle.TestedExtension
import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Properties

plugins {
    protonAndroidApp
    protonDagger
}

protonDagger {
    workManagerHiltIntegration = true
}

protonTestsOptions.unitTestFlavor = "dev"

android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
                arguments["room.incremental"] = "true"
            }
        }
        version = Version(1, 18, 4)
        versionName = version.toString()
    }
    setupFlavors(this)
    sourceSets.getByName("androidTest") {
        // Add schema for android tests
        assets.srcDirs("$projectDir/schemas")
    }
}

fun setupFlavors(testedExtension: TestedExtension) {
    testedExtension.apply {
        val buildConfigFieldKeys = object {
            val PROXY_TOKEN = "PROXY_TOKEN"
            val HOST = "HOST"
            val USE_DEFAULT_PINS = "USE_DEFAULT_PINS"
            val CAN_USE_DOH = "USE_DOH"
        }
        val flavorDimensions = object {
            val env = "env"
        }

        flavorDimensions(flavorDimensions.env)

        defaultConfig {
            buildConfigField("String", buildConfigFieldKeys.PROXY_TOKEN, null.toBuildConfigValue())
            buildConfigField("Boolean", buildConfigFieldKeys.USE_DEFAULT_PINS, true.toBuildConfigValue())
            buildConfigField("Boolean", buildConfigFieldKeys.CAN_USE_DOH, false.toBuildConfigValue())
        }

        productFlavors.register("dev") {
            dimension = flavorDimensions.env
            applicationIdSuffix = ".dev"
            buildConfigField("String", buildConfigFieldKeys.HOST, "proton.black".toBuildConfigValue())
            buildConfigField("Boolean", buildConfigFieldKeys.USE_DEFAULT_PINS, false.toBuildConfigValue())
        }
        productFlavors.register("prod") {
            dimension = flavorDimensions.env
            buildConfigField("String", buildConfigFieldKeys.HOST, "protonmail.ch".toBuildConfigValue())
            buildConfigField("Boolean", buildConfigFieldKeys.CAN_USE_DOH, true.toBuildConfigValue())
        }
        productFlavors.register("localProperties") {
            dimension = flavorDimensions.env
            applicationIdSuffix = ".local.properties"
            val localProperties = Properties().apply {
                try {
                    load(FileInputStream("local.properties"))
                } catch (e: FileNotFoundException) {
                    logger.warn("No local.properties found")
                }
            }
            val proxyToken: String? = localProperties.getProperty(buildConfigFieldKeys.PROXY_TOKEN)
            val host: String = localProperties.getProperty(buildConfigFieldKeys.HOST) ?: "proton.black"
            val useDefaultPins: String = localProperties.getProperty(buildConfigFieldKeys.USE_DEFAULT_PINS) ?: "false"

            buildConfigField(
                "Boolean",
                buildConfigFieldKeys.USE_DEFAULT_PINS,
                useDefaultPins.toBoolean().toBuildConfigValue()
            )
            buildConfigField("String", buildConfigFieldKeys.PROXY_TOKEN, proxyToken.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.HOST, host.toBuildConfigValue())
        }
    }
}

dependencies {

    implementation(

        project(Module.kotlinUtil),
        project(Module.presentation),
        project(Module.network),
        project(Module.networkDagger),
        project(Module.domain),
        project(Module.data),
        project(Module.dataRoom),

        // Features
        project(Module.account),
        project(Module.accountManager),
        project(Module.accountManagerDagger),
        project(Module.auth),
        project(Module.contact),
        project(Module.contactHilt),
        project(Module.crypto),
        project(Module.cryptoValidator),
        project(Module.domain),
        project(Module.eventManager),
        project(Module.featureFlag),
        project(Module.gopenpgp),
        project(Module.humanVerification),
        project(Module.key),
        project(Module.label),
        project(Module.user),
        project(Module.mailMessage),
        project(Module.mailSettings),
        project(Module.payment),
        project(Module.reportPresentation),
        project(Module.reportDagger),
        project(Module.country),
        project(Module.plan),
        project(Module.userSettings),

        `coroutines-android`,

        // Android
        `activity`,
        `appcompat`,
        `android-work-runtime`,
        `constraint-layout`,
        `fragment`,
        `lifecycle-extensions`,
        `lifecycle-viewModel`,
        `material`,

        // Other
        `room-ktx`,
        `retrofit`,
        `timber`,
        `ez-vcard`
    )

    kapt(
        `room-compiler`,
    )

    // Test
    testImplementation(project(Module.androidTest))
    androidTestImplementation(
        project(Module.androidInstrumentedTest),
        `hilt-android-testing`
    )

    // Lint - off temporary
    // lintChecks(project(Module.lint))
}
