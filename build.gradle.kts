import me.proton.core.util.gradle.*
import setup.setupPublishing

/**
 * Registered tasks:
 * * `detekt`
 * * `dokka`
 * * `multiModuleDetekt` [setupDetekt]
 * * `publishAll` [setupPublishing]
 */

buildscript {
    initVersions()

    repositories(repos)
    dependencies(classpathDependencies)
}

allprojects {
    repositories(repos)
}

val sourcesModuleFilter = { module: Project -> !module.name.contains("test", ignoreCase = true) }

setupKotlin(
    "-XXLanguage:+NewInference",
    "-Xuse-experimental=kotlin.Experimental",
    "-XXLanguage:+InlineClasses"
)
setupDetekt()
setupDokka()
setupPublishing()

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}
