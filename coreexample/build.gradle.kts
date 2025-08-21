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

import com.android.build.api.dsl.VariantDimension
import com.android.build.gradle.TestedExtension
import configuration.extensions.protonEnvironment
import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import java.io.FileNotFoundException
import java.util.Properties

plugins {
    protonAndroidApp
    protonDagger
    alias(libs.plugins.compose.compiler)
    id("me.proton.core.gradle-plugins.environment-config")
    kotlin("plugin.serialization")
}

protonCoverage {
    disabled.set(true)
}

protonDagger {
    workManagerHiltIntegration = true
}

protonTestsOptions.unitTestFlavor = "dev"

val localProperties = Properties().apply {
    try {
        load(rootDir.resolve("local.properties").inputStream())
    } catch (e: FileNotFoundException) {
        logger.warn("No local.properties found")
    }
}

android {
    namespace = "me.proton.android.core.coreexample"

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
    signingConfigs {
        create("release") {
            storeFile = file("test-key-store.jks")
            storePassword = localProperties.getProperty("testStorePassword")
            keyAlias = localProperties.getProperty("testKeyAlias")
            keyPassword = localProperties.getProperty("testKeyPassword")
        }
    }
    buildTypes {
        debug {
            signingConfig =
                signingConfigs.getByName("release").takeIf { it.storeFile?.exists() == true } ?: signingConfig
        }
        release {
            postprocessing {
                isRemoveUnusedCode = true
                isRemoveUnusedResources = true
                isObfuscate = true
                isOptimizeCode = true
                proguardFile("proguard-rules.pro")
            }
            signingConfig = signingConfigs.getByName("release")
        }
    }
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
                arguments["room.incremental"] = "true"
            }
        }
        setAssetLinksResValue("proton.me")
        version = Version(1, 18, 10)
        versionName = version.toString()
        testInstrumentationRunner = "me.proton.core.test.android.ProtonHiltTestRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }
    setupFlavors(this)
    sourceSets.getByName("androidTest") {
        // Add schema for android tests
        assets.srcDirs("$projectDir/schemas")
    }.java.srcDirs("src/androidTest/kotlin", "src/androidTestLocalProperties/kotlin")
}

fun setupFlavors(testedExtension: TestedExtension) {
    val protonBlack = "proton.black"

    testedExtension.apply {
        val buildConfigFieldKeys = object {
            val CAN_USE_DOH = "USE_DOH"
            val KEY_TRANSPARENCY_ENV = "KEY_TRANSPARENCY_ENV"
            val SENTRY_DSN = "SENTRY_DSN"
            val ACCOUNT_SENTRY_DSN = "ACCOUNT_SENTRY_DSN"
            val DYNAMIC_DOMAIN = "DYNAMIC_DOMAIN"
            val LOKI_TENANT = "LOKI_TENANT"
        }
        val flavorDimensions = object {
            val env = "env"
        }

        val atlasHost: String = localProperties.getProperty("HOST") ?: protonBlack
        val lokiTenant = localProperties.getProperty(buildConfigFieldKeys.LOKI_TENANT)

        flavorDimensions(flavorDimensions.env)
        defaultConfig {
            buildConfigField("Boolean", buildConfigFieldKeys.CAN_USE_DOH, false.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.KEY_TRANSPARENCY_ENV, null.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.SENTRY_DSN, null.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.ACCOUNT_SENTRY_DSN, null.toBuildConfigValue())
            buildConfigField(
                "String",
                "LOKI_ENDPOINT",
                localProperties.getProperty("LOKI_ENDPOINT").toBuildConfigValue()
            )
            buildConfigField(
                "String",
                "LOKI_PRIVATE_KEY",
                localProperties.getProperty("LOKI_PRIVATE_KEY").toBuildConfigValue()
            )
            buildConfigField(
                "String",
                "LOKI_CERTIFICATE",
                localProperties.getProperty("LOKI_CERTIFICATE").toBuildConfigValue()
            )
        }

        productFlavors.register("dev") {
            dimension = flavorDimensions.env

            applicationIdSuffix = ".dev"
            protonEnvironment {
                host = protonBlack
            }
            buildConfigField("String", buildConfigFieldKeys.DYNAMIC_DOMAIN, protonBlack.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.KEY_TRANSPARENCY_ENV, "black".toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.SENTRY_DSN, null.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.ACCOUNT_SENTRY_DSN, null.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.LOKI_TENANT, lokiTenant.toBuildConfigValue())

            setAssetLinksResValue(protonBlack)
        }
        productFlavors.register("prod") {
            dimension = flavorDimensions.env

            protonEnvironment {
                apiPrefix = "mail-api"
            }

            buildConfigField("Boolean", buildConfigFieldKeys.CAN_USE_DOH, true.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.KEY_TRANSPARENCY_ENV, "production".toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.SENTRY_DSN, null.toBuildConfigValue())
            buildConfigField(
                "String",
                buildConfigFieldKeys.ACCOUNT_SENTRY_DSN,
                System.getenv("ACCOUNT_SENTRY_DSN").toBuildConfigValue()
            )
        }
        productFlavors.register("localProperties") {
            dimension = flavorDimensions.env
            applicationIdSuffix = ".local.properties"
            val keyTransparencyEnv: String? = localProperties.getProperty(buildConfigFieldKeys.KEY_TRANSPARENCY_ENV)
            val sentryDsn: String? = localProperties.getProperty(buildConfigFieldKeys.SENTRY_DSN)
            val accountSentryDsn: String? = localProperties.getProperty(buildConfigFieldKeys.ACCOUNT_SENTRY_DSN)

            protonEnvironment {
                useProxy = true
                host = atlasHost
            }

            buildConfigField(
                "String",
                buildConfigFieldKeys.KEY_TRANSPARENCY_ENV,
                keyTransparencyEnv.toBuildConfigValue()
            )
            buildConfigField("String", buildConfigFieldKeys.SENTRY_DSN, sentryDsn.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.ACCOUNT_SENTRY_DSN, accountSentryDsn.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.DYNAMIC_DOMAIN, atlasHost.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.LOKI_TENANT, lokiTenant.toBuildConfigValue())

            setAssetLinksResValue(atlasHost)
        }
        productFlavors.register("mock") {
            protonEnvironment {
                host = "mock"
            }

            buildConfigField("String", buildConfigFieldKeys.KEY_TRANSPARENCY_ENV, null.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.SENTRY_DSN, null.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.ACCOUNT_SENTRY_DSN, null.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.DYNAMIC_DOMAIN, atlasHost.toBuildConfigValue())
            buildConfigField("String", buildConfigFieldKeys.LOKI_TENANT, lokiTenant.toBuildConfigValue())


            dimension = flavorDimensions.env

            testOptions {
                animationsDisabled = true
                execution = "ANDROIDX_TEST_ORCHESTRATOR"
            }
        }
    }
}

dependencies {
    coreLibraryDesugaring(`desugar-jdk-libs`)

    api(
        project(Module.presentationCompose),
        `compose-runtime`,
        `compose-ui`,
    )

    implementation(

        project(Module.androidUtilDagger),
        project(Module.kotlinUtil),
        project(Module.presentation),
        project(Module.network),
        project(Module.domain),
        project(Module.dataRoom),
        project(Module.proguardRules),

        project(Module.account),
        project(Module.accountManager),
        project(Module.accountRecovery),
        project(Module.auth),
        project(Module.authFido),
        project(Module.challenge),
        project(Module.contact),
        project(Module.crypto),
        project(Module.country),
        project(Module.cryptoValidator),
        project(Module.cryptoAndroid),
        project(Module.cryptoCommon),
        project(Module.domain),
        project(Module.eventManager),
        project(Module.featureFlag),
        project(Module.gopenpgp),
        project(Module.humanVerification),
        project(Module.key),
        project(Module.label),
        project(Module.mailMessage),
        project(Module.mailSettings),
        project(Module.mailSendPreferences),
        project(Module.notification),
        project(Module.observability),
        project(Module.payment),
        project(Module.paymentIap),
        project(Module.plan),
        project(Module.push),
        project(Module.report),
        project(Module.telemetry),
        project(Module.user),
        project(Module.userRecovery),
        project(Module.userSettings),
        project(Module.strictModeUtil),
        project(Module.keyTransparency),
        project(Module.sentryUtil),
        project(Module.configurationData),
        project(Module.biometric),
        project(Module.deviceMigration),
        project(Module.passValidator),

        // Android
        activity,
        `android-ktx`,
        `androidx-collection`,
        appcompat,
        `android-work-runtime`,
        `core-splashscreen`,
        `constraint-layout`,
        `coroutines-android`,
        `coroutines-core`,
        fragment,
        `lifecycle-common`,
        `lifecycle-runtime`,
        `lifecycle-savedState`,
        `lifecycle-viewModel`,
        material,
        recyclerview,
        `startup-runtime`,

        // Other
        `serialization-json`,
        `room-ktx`,
        retrofit,
        timber,
        `sentry-android-core`,
        `ez-vcard`
    )

    // Configuration
    debugImplementation(
        project(Module.configurationDaggerContentResolver),
    )

    releaseImplementation(
        project(Module.configurationDaggerStatic),
    )

    kapt(
        `room-compiler`,
    )

    debugImplementation(
        leakCanary
    )

    // Test
    testImplementation(
        project(Module.androidTest),
        `hilt-android-testing`,
        junit,
        `kotlin-test`,
        miniDns,
        mockWebServer,
        robolectric,
        squareup("okhttp3", "okhttp-tls") version `okHttp version`
    )

    kaptTest(
        `hilt-android-compiler`
    )

    kaptAndroidTest(
        `hilt-android-compiler`
    )

    androidTestImplementation(
        project(Module.androidInstrumentedTest),
        project(Module.paymentIapTest),
        project(Module.planTest),
        project(Module.authTest),
        project(Module.quark),
        project(Module.testPerformance),
        project(Module.testRule),
        project(Module.humanVerificationTest),
        project(Module.reportTest),
        project(Module.accountRecoveryTest),
        `android-work-testing`,
        `android-test-runner`,
        `hilt-android-testing`,
        `kotlin-test-junit`,
        `lifecycle-process`,
        `mockk-android`,
        mockWebServer,
        uiautomator,
        fusion
    )

    androidTestUtil(`androidx-test-orchestrator`)

    // Lint - off temporary
    // lintChecks(project(Module.lint))
}

fun VariantDimension.setAssetLinksResValue(host: String) {
    resValue(
        type = "string", name = "asset_statements",
        value = """
            [{
              "relation": ["delegate_permission/common.handle_all_urls", "delegate_permission/common.get_login_creds"],
              "target": { "namespace": "web", "site": "https://$host" }
            }]
        """.trimIndent()
    )
}
