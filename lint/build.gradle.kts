import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*

plugins {
    `java-library`
    `kotlin`
}

val version = Version(0, 1, 0)
archivesBaseName = archiveNameFor(ProtonCore.lint, version)

dependencies {
    // Lint
    compileOnly(
        `lint-api`,
        `lint-checks`
    )
    testImplementation(
        `lint-core`,
        `lint-tests`
    )

    // Accessors
    compileOnly(
        `coroutines-core`,
        `serialization`
    )
}


val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Lint-Registry-v2"] = "ch.protonmail.libs.lint.ProtonIssueRegistry"
    }
}

tasks.getting(Test::class) {
    environment("LINT_PRINT_STACKTRACE", true)
}
