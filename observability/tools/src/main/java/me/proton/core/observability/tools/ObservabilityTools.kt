/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.observability.tools

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.victools.jsonschema.generator.Option
import com.github.victools.jsonschema.generator.OptionPreset
import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.SchemaVersion
import com.github.victools.jsonschema.module.swagger2.Swagger2Module
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.ObservabilityData
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

fun main(args: Array<String>) {
    BaseCommand().subcommands(GenerateCommand(), SearchCommand(), DownloadCommand()).main(args)
}

class BaseCommand : NoOpCliktCommand()

class GenerateCommand : CliktCommand() {
    private val schemaIdRegex =
        Regex("^https://proton\\.me/(android_[a-z]+_\\w+_v\\d+\\.schema\\.json)$")

    private val baseClass by argument(
        help = "Fully-qualified class name of a sealed class that " +
                "directly inherits from ${ObservabilityData::class.qualifiedName}."
    )

    private val outputDir by option(
        help = "Path (absolute or relative to `observability/tools`) where the JSON files will be written. " +
                "Existing files are overwritten."
    ).file(
        canBeDir = true,
        canBeFile = false,
        mustBeReadable = true,
        mustBeWritable = true
    ).required()

    override fun run() {
        val config = getSchemaGeneratorConfig()
        val generator = SchemaGenerator(config)

        outputDir.mkdirs()

        val baseClass = Class.forName(baseClass).kotlin
        require(baseClass.superclasses.contains(ObservabilityData::class)) {
            "The class ${baseClass.qualifiedName} should inherit " +
                    "directly from ${ObservabilityData::class.qualifiedName}."
        }
        getAllSubclasses(baseClass, SchemaId::class).forEach { kClass ->
            val schemaId = kClass.java.getSchemaId()
                ?: error("Class is not annotated with ${SchemaId::class.simpleName} (class: ${kClass.simpleName})")
            val node = generator.generateSchema(kClass.java)
            val mapper = JsonMapper.builder()
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build()
            val printer = DefaultPrettyPrinter()
                .withObjectIndenter(DefaultIndenter("    ", "\n"))
            val jsonSchema = mapper.writer(printer).writeValueAsString(node)
            val match = schemaIdRegex.find(schemaId.id)
                ?: error("Could not match the required schema ID format.")
            val filename = match.groupValues[1]
            val outputFile = File(outputDir, filename)
            outputFile.writeText(jsonSchema)
        }
    }

    private fun <T : Any, A : Any> getAllSubclasses(
        kClass: KClass<T>,
        annotatedBy: KClass<A>
    ): List<KClass<out T>> {
        return when {
            kClass.isSealed -> kClass.sealedSubclasses.flatMap { getAllSubclasses(it, annotatedBy) }
            kClass.java.getSchemaId() != null -> listOf(kClass)
            else -> error("Class is not annotated with ${SchemaId::class.simpleName} (class: ${kClass.simpleName})")
        }
    }

    private fun Class<*>.getSchemaId(): SchemaId? = getAnnotation(SchemaId::class.java)

    private fun getSchemaGeneratorConfig() =
        SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
            .with(Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT)
            .with(Option.ENUM_KEYWORD_FOR_SINGLE_VALUES)
            .with(Option.INLINE_ALL_SCHEMAS)
            .with(Swagger2Module())
            .apply {
                forTypesInGeneral()
                    .withIdResolver { typeScope ->
                        val klass = typeScope.type.erasedType
                        klass.getSchemaId()?.id?.also {
                            require(schemaIdRegex.matches(it)) {
                                "Schema ID doesn't match the required format (class ${klass.name}): '$it'."
                            }
                        }
                    }
                    .withTitleResolver {
                        if (ObservabilityData::class.java.isAssignableFrom(it.type.erasedType)) {
                            it.type.erasedType.name
                        } else null
                    }

                forFields()
                    .withRequiredCheck { true }
            }
            .build()
}

abstract class ApiCommand : CliktCommand() {

    protected val grafanaApiKey by option().required()
    protected val grafanaUrl by option().required()

    protected fun execute(link: String): String {
        val url = requireNotNull(grafanaUrl.toHttpUrl().resolve(link)) {
            "Could not create URL for $grafanaUrl."
        }
        val bearer = "Bearer $grafanaApiKey"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", bearer)
            .build()
        val response = OkHttpClient().newCall(request).execute()
        check(response.isSuccessful) { "Could not execute `$request`" }
        return requireNotNull(response.body?.use { it.charStream().readText() }) {
            "Could not read response body for $url."
        }
    }

    protected fun search(query: String): JsonArray {
        val json = Json { prettyPrint = true }
        val response = execute("/api/search?query=$query")
        return json.decodeFromString(response)
    }
}

class SearchCommand : ApiCommand() {

    private val query by option()

    override fun run() {
        println("Searching ...")
        val response = search(query ?: "")
        println("Response: $response")
    }
}

class DownloadCommand : ApiCommand() {

    private val query by option()
    private val outputDir by option().file(
        canBeDir = true,
        canBeFile = false,
        mustBeReadable = true,
        mustBeWritable = true
    ).required()

    override fun run() {
        require(grafanaUrl.isNotBlank())
        require(grafanaApiKey.isNotBlank())

        val json = Json { prettyPrint = true }

        val dashboards = search(query ?: "Android").associate {
            val uid = it.jsonObject["uid"]?.jsonPrimitive?.content
            val uri = it.jsonObject["uri"]?.jsonPrimitive?.content
            val filename = uri?.split("/")?.last()
            requireNotNull(uid) to "${requireNotNull(filename)}.json"
        }

        dashboards.forEach { (uid, filename) ->
            println("Downloading $filename ($uid) ...")
            val response = execute("/api/dashboards/uid/$uid")
            val obj = json.decodeFromString<JsonObject>(response)
            val dashboard = requireNotNull(obj["dashboard"]) { "Dashboard not found for $uid." }
            val file = File(outputDir, filename)
            file.writeText(json.encodeToString(dashboard))
            println("Written to ${file.absolutePath}")
        }
    }
}
