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
    kotlin("plugin.serialization")
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
            val API_HOST = "API_HOST"
            val HV3_HOST = "HV3_HOST"
            val QUARK_HOST = "QUARK_HOST"
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
            buildConfigField("String", buildConfigFieldKeys.API_HOST, "api.proton.black".toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.HV3_HOST, "verify.proton.black".toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.QUARK_HOST, "proton.black".toBuildConfigValue())
            buildConfigField("Boolean", buildConfigFieldKeys.USE_DEFAULT_PINS, false.toBuildConfigValue())
        }
        productFlavors.register("prod") {
            dimension = flavorDimensions.env
            buildConfigField("String", buildConfigFieldKeys.API_HOST, "api.protonmail.ch".toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.HV3_HOST, "verify.protonmail.com".toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.QUARK_HOST, "".toBuildConfigValue())
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
            val host: String = localProperties.getProperty("HOST") ?: "protonmail.ch"
            val apiHost = localProperties.getProperty(buildConfigFieldKeys.API_HOST) ?: "api.$host"
            val hv3Host = localProperties.getProperty(buildConfigFieldKeys.HV3_HOST) ?: "verify.$host"
            val quarkHost = localProperties.getProperty(buildConfigFieldKeys.QUARK_HOST) ?: host
            val useDefaultPins: String = localProperties.getProperty(buildConfigFieldKeys.USE_DEFAULT_PINS) ?: "false"

            buildConfigField(
                "Boolean",
                buildConfigFieldKeys.USE_DEFAULT_PINS,
                useDefaultPins.toBoolean().toBuildConfigValue()
            )
            buildConfigField("String", buildConfigFieldKeys.PROXY_TOKEN, proxyToken.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.API_HOST, apiHost.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.HV3_HOST, hv3Host.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.QUARK_HOST, quarkHost.toBuildConfigValue())
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

        // Feature
        // TODO: remove commented out modules if this is the result we want
//        project(Module.account),
        project(Module.accountDagger),
//        project(Module.accountManager),
        project(Module.accountManagerDagger),
//        project(Module.auth),
        project(Module.authDagger),
//        project(Module.contact),
        project(Module.contactHilt),
//        project(Module.crypto),
        project(Module.cryptoDagger),
//        project(Module.cryptoValidator),
        project(Module.cryptoValidatorDagger),
        project(Module.domain),
//        project(Module.eventManager),
        project(Module.eventManagerDagger),
//        project(Module.featureFlag),
        project(Module.featureFlagDagger),
        project(Module.gopenpgp),
//        project(Module.humanVerification),
        project(Module.humanVerificationDagger),
//        project(Module.key),
        project(Module.keyDagger),
//        project(Module.label),
        project(Module.labelDagger),
//        project(Module.user),
        project(Module.userDagger),
//        project(Module.mailMessage),
        project(Module.mailMessageDagger),
//        project(Module.mailSettings),
        project(Module.mailSettingsDagger),
//        project(Module.payment),
        project(Module.paymentDagger),
        project(Module.reportPresentation),
        project(Module.reportDagger),
//        project(Module.country),
        project(Module.countryDagger),
//        project(Module.plan),
        project(Module.planPresentation),
        project(Module.planDagger),
        project(Module.push),
        project(Module.userSettings),
        project(Module.userSettingsPresentation),
        project(Module.userSettingsDagger),
//        project(Module.challenge),
        project(Module.challengeDagger),

        `coroutines-android`,

        // Android
        `activity`,
        `appcompat`,
        `android-work-runtime`,
        `core-splashscreen`,
        `constraint-layout`,
        `fragment`,
        `lifecycle-extensions`,
        `lifecycle-viewModel`,
        `material`,

        // Other
        `serialization-json`,
        `room-ktx`,
        `retrofit`,
        `timber`,
        `ez-vcard`
    )

    kapt(
        `room-compiler`,
    )

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.9.1")

    // Test
    testImplementation(
        project(Module.androidTest),
        `hilt-android-testing`,
        miniDns,
        mockWebServer,
        squareup("okhttp3", "okhttp-tls") version `okHttp version`
    )

    kaptTest(
        `hilt-android-compiler`
    )

    androidTestImplementation(
        project(Module.androidInstrumentedTest),
        `hilt-android-testing`
    )

    // Lint - off temporary
    // lintChecks(project(Module.lint))
}
