import studio.forface.easygradle.dsl.*
import util.libVersion

plugins {
    `kotlin-library`
    `kotlin-serialization`
}

libVersion = Version(0, 2, 1)

dependencies {

    implementation(

        project(Module.kotlinUtil),

        // Kotlin
        `kotlin-jdk7`,
        `coroutines-core`,
        `serialization`
    )

    testImplementation(project(Module.kotlinTest))
}
