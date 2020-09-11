import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import util.libVersion

plugins {
    `android-library`
}

libVersion = Version(0, 1, 4)

android()

dependencies {

    api(
        project(Module.humanVerificationDomain),
        project(Module.humanVerificationData),
        project(Module.humanVerificationPresentation)
    )
}
