/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.pwall.json.schema.codegen.CodeGenerator
import org.gradle.kotlin.dsl.implementation
import org.gradle.kotlin.dsl.testImplementation
import studio.forface.easygradle.dsl.*
import java.io.ByteArrayOutputStream

plugins {
    protonKotlinLibrary
    kotlin("plugin.serialization")
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("net.pwall.json:json-kotlin-schema-codegen:0.86")
        classpath("org.jetbrains.kotlinx:kotlinx-serialization-core:1.4.1")
    }
}

publishOption.shouldBePublishedAsLib = true

proton {
    apiModeWarning()
}

kotlin {
    sourceSets {
        main {
            kotlin.srcDir("src/generated/kotlin")
        }
    }
}

tasks.register<GenerateFromJsonSchema>("generateFromJsonSchema") {
    configFile.set(file("json-schema-config.json"))
    jsonRegistryDir.set(rootDir.resolve("../json-schema-registry"))
    jsonSchemaFilePattern.set("observability/client/android_core_*.schema.json")
    outputDir.set(file("src/generated/kotlin"))
}

dependencies {
    api(`javax-inject`)
    implementation(`coroutines-core`)
    implementation(`serialization-core`)
    implementation(project(Module.kotlinUtil))
    testImplementation(junit)
    testImplementation(`kotlin-test`)
    testImplementation(`serialization-json`)
}

/** Generates Kotlin classes from JSON schema files. */
abstract class GenerateFromJsonSchema : DefaultTask() {
    /** Configuration file for the `json-kotlin-schema-codegen` library
     * [Configuration options](https://github.com/pwall567/json-kotlin-schema-codegen/blob/main/CONFIG.md)
     */
    @get:InputFile
    abstract val configFile: RegularFileProperty

    /** The directory with `json-schema-registry` repository. */
    @get:InputDirectory
    abstract val jsonRegistryDir: DirectoryProperty

    /** The pattern for matching the JSON schema files for which we want to generate Kotlin classes. */
    @get:Input
    abstract val jsonSchemaFilePattern: Property<String>

    /** The output directory for the generated Kotlin classes. */
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    private val matchingJsonSchemaFiles by lazy {
        jsonRegistryDir.asFileTree.matching { include(jsonSchemaFilePattern.get()) }
    }

    @TaskAction
    fun execute() {
        validate()
        generate()
        applyFixes()
    }

    private fun applyFixes() {
        outputDir.asFileTree.forEach { file ->
            if (file.isDirectory || !file.name.endsWith(".kt")) return@forEach

            val contents = file.readText()
            val newContents = contents
                .replace("val Labels: Labels", "@kotlinx.serialization.SerialName(\"Labels\") val labels: Labels")
                .replace("data class Labels(", "@Serializable data class Labels constructor(")

            if (newContents != contents) {
                file.writeText(newContents)
            }
        }
    }

    private fun generate() {
        CodeGenerator().apply {
            configure(configFile.asFile.get())
            baseDirectoryName = outputDir.asFile.get().path
            generatorComment = "Generated from ${jsonSchemaRegistryGitHash()}"
        }.generate(matchingJsonSchemaFiles.files.toList())
    }

    private fun jsonSchemaRegistryGitHash(): String {
        val jsonRegistryHash = ByteArrayOutputStream()

        project.exec {
            workingDir(jsonRegistryDir.asFile)
            commandLine("git", "rev-parse", "--short", "HEAD")
            standardOutput = jsonRegistryHash
        }.assertNormalExitValue()

        return jsonRegistryHash.toString()
    }

    private fun validate() {
        val schemaFileRegex = Regex("^android_core_.*_v[0-9]+\\.schema\\.json$")
        val configJson = Json.parseToJsonElement(configFile.asFile.get().readText()).jsonObject
        val classNames = configJson["classNames"]!!.jsonObject.keys

        matchingJsonSchemaFiles.forEach { file ->
            require(file.name.matches(schemaFileRegex)) {
                "File name does not match the required structure (file $file)."
            }

            val contents = Json.parseToJsonElement(file.readText())
            val schemaId = contents.jsonObject["\$id"]?.jsonPrimitive?.content

            require(schemaId == "https://proton.me/${file.name}") {
                "The `\$id` property inside the JSON schema does not match the required structure ($file)."
            }

            require(schemaId in classNames) {
                val proposedClassName = file.name
                    .removeSuffix(".schema.json")
                    .removePrefix("android_core_")
                    .split("_")
                    .joinToString("") { it.capitalize() }
                """
                    The schema is not mapped to a Kotlin class.
                    Put the mapping under `classNames` in ${configFile.get()}, e.g.:
                    "$schemaId": $proposedClassName
                """.trimIndent()
            }
        }
    }
}
