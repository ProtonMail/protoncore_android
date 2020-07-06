import studio.forface.easygradle.dsl.*
import util.libVersion

plugins {
    `kotlin-dsl`
    `kotlin-library`
}

libVersion = Version(0, 1, 8)

dependencies {

    implementation(
        `kotlin-jdk7`,

        `kotlin-gradle-plugin`,
        `detekt-gradle-plugin`,
        `dokka-gradle-plugin`
    )
    api(`easyGradle-android`)

    testImplementation(project(Module.kotlinTest))
}
