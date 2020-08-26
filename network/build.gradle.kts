import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import util.libVersion

plugins {
    `android-library`
}

libVersion = Version(0, 2, 3)

android()

dependencies {

    api(
        project(Module.networkDomain),
        project(Module.networkData)
    )
}
