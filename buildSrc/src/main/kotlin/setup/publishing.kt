package setup

import applyDevelopers
import org.gradle.api.Project
import org.gradle.kotlin.dsl.named
import org.jetbrains.dokka.gradle.DokkaTask
import studio.forface.easygradle.dsl.*
import util.archiveName
import util.humanReadableName
import util.libVersion
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Setup Publishing for whole Project.
 * It will setup publishing to sub-projects by generating KDoc, generating aar, updating readme and publish new versions
 * to Bintray
 *
 * @param filter filter [Project.subprojects] to attach Publishing to
 *
 *
 * @author Davide Farella
 */
fun Project.setupPublishing(filter: (Project) -> Boolean = { true }) {

    // Configure sub-projects
    for (sub in subprojects.filter(filter)) sub.setupModule()
}

private fun Project.setupModule() {
    afterEvaluate {

        val bintrayApiKey = System.getenv()["BINTRAY_PUBLISH_KEY"]
        if (libVersion != null && bintrayApiKey != null) {

            archivesBaseName = archiveName

            // Setup maven publish config
            publish {
                version = libVersion!!
                developers(applyDevelopers)

                apiKey = bintrayApiKey

                // Temporary solution since files are already published on Bintray.
                override = true
            }

            // Setup pre publish
            val releaseManager = ReleaseManager(this)
            val prePublish = tasks.create("prePublish") {
                doFirst {
                    with(releaseManager) {
                        moveArchives("releases")
                        generateKdocIfNeeded()
                        updateReadme()
                        printToNewReleasesFile()
                    }
                }
            }

            // setup publish
            tasks.register("publishAll") {
                dependsOn(prePublish)
                if (releaseManager.isNewVersion) {
                    dependsOn(tasks.getByName("uploadArchives"))
                }
            }
        }
    }
}

/**
 * This class will organize libraries release.
 *
 * It can:
 * * Move jar/aar archives into '/folderName/<libName>'
 * * Generate KDoc if new library is available
 * * Update readme with new version
 * * Print update to new_releases.tmp
 *
 * @param forceRefresh Generally all the processes are executed only of [Project.libVersion] is different from the one
 *   in the readme. If this parameter is set to `true`, they will run in any case.
 *   Default is `false`
 *
 * @author Davide Farella
 */
class ReleaseManager internal constructor(
    project: Project,
    forceRefresh: Boolean = false
) : Project by project {

    private val prevVersion = README_VERSION_REGEX.find(README_FILE.readText())!!.groupValues[1]
    private val versionName = libVersion!!.versionName

    val isNewVersion = forceRefresh || prevVersion != versionName
    private val shouldRefresh = forceRefresh || isNewVersion

    /** Move jar/aar archives into '/[folderName]/<libName>' */
    fun moveArchives(folderName: String) {
        // Setup folder
        val newDir = File(rootDir, folderName + File.separator + name)
        if (!newDir.exists()) newDir.mkdirs()

        moveJars(into = newDir)
        moveAars(into = newDir)
    }

    private fun moveJars(into: File) {
        JAR_DIRECTORY.listFiles()?.forEach { file ->
            val newFile = File(into, file.name.replace("-$versionName", ""))
            // If file is absent
            if (!newFile.exists()) {
                file.copyTo(newFile)
            }
            // DO NOT DELETE JAR FILE, IT'S REQUIRED BY JetifyTransform
        }
    }

    private fun moveAars(into: File) {
        AAR_DIRECTORY.listFiles()?.forEach { file ->
            if ("release" in file.name) {
                val newFile = File(
                    into,
                    file.name
                        .replace("-release", "")
                        .replace("-$versionName", "")
                )
                // If file is absent
                if (!newFile.exists()) {
                    file.copyTo(newFile)
                }
            }
            file.delete()
        }
    }

    /** Generate KDoc if new library is available */
    fun generateKdocIfNeeded() {
        if (shouldRefresh) tasks.named<DokkaTask>("dokka").orNull?.generate()
    }

    /** Update readme with new version */
    fun updateReadme() {
        if (shouldRefresh) {
            val timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))

            README_FILE.writeText(
                README_FILE.readText()
                    .replace(README_VERSION_REGEX, readmeVersion(humanReadableName, versionName, timestamp))
            )
        }
    }

    /** Print release to new_releases.tmp */
    fun printToNewReleasesFile() {
        if (shouldRefresh) {
            NEW_RELEASES_FILE.writeText(
                "${NEW_RELEASES_FILE.readText()}$humanReadableName $versionName\n"
            )
        }
    }

    private companion object {
        val Project.JAR_DIRECTORY get() = File(buildDir, "libs")
        val Project.AAR_DIRECTORY get() = File(buildDir, "outputs" + File.separator + "aar")
        val Project.README_FILE get() = File(rootDir, "README.md")
        val Project.README_VERSION_REGEX get() = readmeVersion(humanReadableName, "(.+)", "(.+)").toRegex()
        val Project.NEW_RELEASES_FILE get() = File(rootDir, "new_releases.tmp")
            .also {
                // 10 min lifetime
                if (it.lastModified() < System.currentTimeMillis() - 10 * 60 * 1000) it.delete()
                if (!it.exists()) it.createNewFile()
            }

        fun readmeVersion(name: String, version: String, timestamp: String) =
            """$name: \*\*$version\*\* - _released on: ${timestamp}_"""
    }
}
