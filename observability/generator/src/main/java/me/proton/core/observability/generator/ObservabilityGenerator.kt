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

package me.proton.core.observability.generator

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.victools.jsonschema.generator.Option
import com.github.victools.jsonschema.generator.OptionPreset
import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.SchemaVersion
import com.github.victools.jsonschema.module.swagger2.Swagger2Module
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.ObservabilityData
import java.io.File

fun main(args: Array<String>) {
    BaseCommand().subcommands(GenerateCommand()).main(args)
}

class BaseCommand : NoOpCliktCommand()

class GenerateCommand : CliktCommand() {
    private val schemaIdRegex = Regex("^https://proton\\.me/(android_core_\\w+_v\\d+\\.schema\\.json)$")

    private val outputDir by option().file(
        canBeDir = true,
        canBeFile = false,
        mustBeReadable = true,
        mustBeWritable = true
    ).required()

    override fun run() {
        val config = getSchemaGeneratorConfig()
        val generator = SchemaGenerator(config)

        outputDir.mkdirs()

        ObservabilityData::class.sealedSubclasses.forEach { kClass ->
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
            val match = schemaIdRegex.find(schemaId.id) ?: error("Could not match the required schema ID format.")
            val filename = match.groupValues[1]
            val outputFile = File(outputDir, filename)
            outputFile.writeText(jsonSchema)
        }
    }

    private fun Class<*>.getSchemaId(): SchemaId? = getAnnotation(SchemaId::class.java)

    private fun getSchemaGeneratorConfig() =
        SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
            .with(Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT)
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
