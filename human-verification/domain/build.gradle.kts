import studio.forface.easygradle.dsl.*
import util.libVersion

plugins {
    `kotlin-library`
}

libVersion = Version(0, 1, 0)

dependencies {

    implementation(

        project(Module.kotlinUtil),
        project(Module.domain),
        project(Module.networkDomain),

        // Kotlin
        `kotlin-jdk8`,
        `coroutines-core`,

        // Android
        `dagger`
    )

    testImplementation(project(Module.kotlinTest))
}

