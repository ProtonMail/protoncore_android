import org.gradle.api.artifacts.dsl.DependencyHandler
import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*

val DependencyHandler.`apacheCommon-codec` get() = dependency("commons-codec", module = "commons-codec") version `apacheCommon-codec version`
val DependencyHandler.`okHttp-logging` get() = squareup("okhttp3", module = "logging-interceptor") version `okHttp version`
val DependencyHandler.`lint-core` get() = lint()
val DependencyHandler.`lint-api` get() = lint("api")
val DependencyHandler.`lint-checks` get() = lint("checks")
val DependencyHandler.`lint-tests` get() = lint("tests")

// Gradle
val DependencyHandler.`easyGradle-android` get() = forface("easygradle", "dsl") version `easyGradle version`

// Plugins
val DependencyHandler.`detekt-gradle-plugin` get() = detekt("gradle-plugin")
val DependencyHandler.`dokka-gradle-plugin` get() = jetbrains("dokka", moduleSuffix = "gradle-plugin") version `dokka-plugin version`

// region accessors
fun DependencyHandler.lint(moduleSuffix: String? = null, version: String = `android-tools version`) =
    android("tools.lint", "lint", moduleSuffix, version)
// endregion

// region hilt
val DependencyHandler.`hilt-lifecycle-viewmodel` get() = androidx("hilt", moduleSuffix = "lifecycle-viewmodel") version "1.0.0-alpha01"
// endregion
