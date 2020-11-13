import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*

plugins {
    id("com.android.library")
    kotlin("android")
}

libVersion = Version(0, 1, 3)

android()

dependencies {
    api(
        project(Module.accountManagerPresentation),
        project(Module.accountManagerDomain),
        project(Module.accountManagerData)
    )
}
