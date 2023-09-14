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
package configuration

import com.android.build.api.dsl.ApplicationBuildType
import com.android.build.api.dsl.DefaultConfig
import com.android.build.api.dsl.ProductFlavor
import com.android.build.gradle.AppExtension
import configuration.extensions.environmentConfiguration
import configuration.extensions.mergeWith
import configuration.extensions.printEnvironmentConfigDetails
import configuration.extensions.sourceClassContent
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized

class ProtonEnvironmentConfigurationPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.logger.info("Applying Proton environment configurations")

        target.afterEvaluate {
            handleConfigurations(target)
        }
    }

    /**
     * Handles the configurations for the given project.
     *
     * Goes through all the product flavors and build types of the project,
     * merges their configurations, and then creates the necessary directories and files.
     *
     * @param project The project for which the configurations are being handled.
     */
    private fun handleConfigurations(project: Project) {
        project.extensions.getByType(AppExtension::class.java).apply {
            productFlavors.all { flavor ->
                buildTypes.all { buildType ->
                    val mergedConfig = mergeConfigurations(defaultConfig, buildType, flavor)
                    val variantName = flavor.name + buildType.name.capitalized()

                    project.logger.debug("environment configuration :$variantName")
                    project.printEnvironmentConfigDetails(mergedConfig)

                    project.createJavaFileInDir(flavor, buildType, mergedConfig.sourceClassContent())
                    true
                }
            }
        }
    }

    /**
     * Creates a [javaClassName].java file in [generatedSourceLocation]/[flavor].name/[buildType].name/[packagePath]
     * adds flavor directories to source sets
     *
     * @param flavor The product flavor.
     * @param buildType The build type.
     * @param sourceClassContent The content to be written to the source class file.
     * @param generatedSourceLocation Generated files dir
     * @param javaClassName Java class name to be generated
     * @param packagePath Package directory
     */
    @Suppress("LongParameterList")
    private fun Project.createJavaFileInDir(
        flavor: ProductFlavor,
        buildType: ApplicationBuildType,
        sourceClassContent: String,
        generatedSourceLocation: String = ENV_CONFIG_LOCATION,
        javaClassName: String = DEFAULTS_CLASS_NAME,
        packagePath: String = PACKAGE_NAME.replace(".", "/")
    ) {
        val variantLocation = "$generatedSourceLocation/${flavor.name}/${buildType.name}"
        val variantName = flavor.name + buildType.name.capitalized()
        val configDirectory = project.buildDir.resolve("$variantLocation/$packagePath")
        val variantDirectory = project.buildDir.resolve(variantLocation)

        configDirectory.mkdirs()

        val environmentConfigFile = configDirectory
            .resolve("$javaClassName.java").apply {
                writeText(sourceClassContent)
            }

        project.logger.info("generated ${environmentConfigFile.path}")

        project.extensions.getByType(AppExtension::class.java).sourceSets
            .findByName(variantName)
            ?.java?.apply {
                // avoid duplicate content roots
                if (!srcDirs.contains(variantDirectory)) {
                    srcDir(variantDirectory)
                }
            }
    }

    private fun mergeConfigurations(
        defaultConfig: DefaultConfig,
        buildType: ApplicationBuildType,
        flavor: ProductFlavor
    ): EnvironmentConfig = defaultConfig.environmentConfiguration
        .mergeWith(buildType.environmentConfiguration)
        .mergeWith(flavor.environmentConfiguration)

    companion object {
        const val ENV_CONFIG_LOCATION: String = "generated/source/envConfig"
        const val PACKAGE_NAME: String = "me.proton.core.configuration"
        const val DEFAULTS_CLASS_NAME: String = "EnvironmentConfigurationDefaults"
    }
}
