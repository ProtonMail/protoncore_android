import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import util.libVersion

plugins {
    `kotlin-library`
    `kotlin-serialization`
}

//libVersion = Version(0, 1, 0)

dependencies {

    implementation(

        project(Module.kotlinUtil),
        `coroutines-core`,
        // project(Module.domain)

        // Kotlin
        `kotlin-jdk7`,
        `serialization`
    )

    testImplementation(project(Module.kotlinTest))
}

