package mockproxy

import com.android.build.gradle.internal.coverage.JacocoReportTask.JacocoReportWorkerAction.Companion.logger
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Properties

class MockFilesPullerPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val properties = Properties().apply {
            runCatching {
                load(project.rootDir.resolve("local.properties").inputStream())
            }.recoverCatching {
                load(project.rootDir.resolve("private.properties").inputStream())
            }.getOrElse { throwable ->
                // Provide empty properties to allow the app to be built without secrets
                logger.warn(
                    "MockFilePullerPlugin: local.properties or private.properties " +
                            "file not found. Proceeding with empty properties." +
                            "\n Message ${throwable.message}"
                )
                Properties()
            }
        }

        project.tasks.register("pullMockFiles", PullMockFilesTask::class.java) {
            // Default setup: destination directory within the project
            destinationDir =
                project.layout.projectDirectory.dir(
                    properties.getProperty("PROJECT_MOCK_FILES_DIR")
                ).asFile
            sourceDir =
                project.layout.projectDirectory.dir(
                    properties.getProperty("MOCK_PROXY_RECORD_DIR")
                ).asFile
        }
    }
}


abstract class PullMockFilesTask : DefaultTask() {
    @InputDirectory
    lateinit var sourceDir: File

    @OutputDirectory
    lateinit var destinationDir: File

    init {
        group = "Mock"
        description = "Pulls mock files from a source directory to the destination directory."
    }

    @TaskAction
    fun pullMockFiles() {
        logger.lifecycle(
            "MockFilePullerPlugin: Pulling mock files from $sourceDir to $destinationDir"
        )

        if (!sourceDir.exists()) {
            throw IllegalArgumentException("Source directory does not exist: $sourceDir")
        }
        destinationDir.mkdirs()
        sourceDir.walkTopDown().forEach { file ->
            val destinationFile = File(destinationDir, file.relativeTo(sourceDir).path)
            if (file.isDirectory) {
                destinationFile.mkdirs()
            } else {
                Files.copy(
                    file.toPath(),
                    destinationFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
                logger.lifecycle(
                    "MockFilePullerPlugin: Copied: ${file.absolutePath} ->" +
                            " ${destinationFile.absolutePath}"
                )
            }
        }
    }
}
