import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*

plugins {
    id("com.android.library")
    kotlin("android")
}

libVersion = Version(1, 0, 2)

android(minSdk = 23)

dependencies {
    api(
        // project(Module.accountPresentation),
        project(Module.accountDomain),
        project(Module.accountData)
    )
}

