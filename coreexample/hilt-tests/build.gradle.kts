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

import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*

plugins {
    protonAndroidTest
    protonDagger
}

protonDagger {
    workManagerHiltIntegration = true
}

publishOption.shouldBePublishedAsLib = false

android {
    namespace = "me.proton.android.core.coreexample.hilttests"

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    defaultConfig {
        targetProjectPath = ":coreexample"
        testInstrumentationRunner = "me.proton.core.test.android.ProtonHiltTestRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    flavorDimensions.add("env")
    productFlavors {
        register("dev") { dimension = "env" }
        register("localProperties") { dimension = "env" }
        register("mock") { dimension = "env" }
        register("prod") { dimension = "env" }
    }

    testOptions {
        animationsDisabled = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

dependencies {
    androidTestUtil(`androidx-test-orchestrator`)

    coreLibraryDesugaring(`desugar-jdk-libs`)

    implementation(
        `android-work-runtime`,
        `hilt-android-testing`,
        `kotlin-test`,
        `kotlin-test-junit`,
        `mockk-android`,
        mockWebServer,

        project(Module.androidInstrumentedTest),
        project(Module.quark),

        project(Module.account),
        project(Module.accountManager),
        project(Module.accountRecovery),
        project(Module.androidUtilDagger),
        project(Module.auth),
        project(Module.authTest),
        project(Module.challenge),
        project(Module.contact),
        project(Module.country),
        project(Module.crypto),
        project(Module.cryptoValidator),
        project(Module.eventManager),
        project(Module.featureFlag),
        project(Module.gopenpgp),
        project(Module.humanVerification),
        project(Module.kotlinUtil),
        project(Module.key),
        project(Module.label),
        project(Module.mailMessage),
        project(Module.mailSettings),
        project(Module.network),
        project(Module.notification),
        project(Module.observabilityDagger),
        project(Module.payment),
        project(Module.sentryUtil),
        project(Module.paymentIap),
        project(Module.plan),
        project(Module.plan),
        project(Module.push),
        project(Module.report),
        project(Module.user),
        project(Module.userSettings),
        project(Module.keyTransparency)
    )
}
