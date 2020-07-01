import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import util.libVersion

plugins {
    `kotlin-library`
}

//libVersion = Version(0, 1, 0)

dependencies {

    implementation(

        project(Module.kotlinUtil),
        // project(Module.domain)

        // Kotlin
        `kotlin-jdk7`
    )

    testImplementation(project(Module.kotlinTest))
}

