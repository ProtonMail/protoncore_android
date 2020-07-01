import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import util.libVersion

plugins {
    `kotlin-library`
}

libVersion = Version(0, 1, 0)

dependencies {

    // Base dependencies
    implementation(
        `kotlin-jdk7`,
        `coroutines-core`
    )

    // Test dependencies
    api(
        `kotlin-test`,
        `kotlin-test-junit`,
        `coroutines-test`,

        `mockk`
    )
}
