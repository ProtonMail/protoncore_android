import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import util.libVersion

plugins {
    `kotlin-library`
    `kotlin-serialization`
}

libVersion = Version(0, 1, 2)

dependencies {

    implementation(
        project(Module.kotlinUtil),

        `kotlin-jdk7`,
        `coroutines-core`,
        `serialization`
    )

    testImplementation(project(Module.kotlinTest))
}
