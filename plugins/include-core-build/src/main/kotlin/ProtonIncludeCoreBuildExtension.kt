/*
 * Copyright 2003-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import me.champeau.gradle.igp.IncludedGitRepo
import me.champeau.gradle.igp.internal.DefaultIncludeGitExtension
import org.gradle.api.Action
import org.gradle.api.initialization.ConfigurableIncludedBuild
import org.gradle.api.initialization.Settings
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import javax.inject.Inject

/**
 * The main configuration of the Git include plugin.
 */
interface ProtonIncludeCoreBuildExtension {

    /**
     * Determines, in milliseconds, how often the repository should be updated.
     *
     * By default, 24 hours.
     *
     * @return the refresh interval property
     */
    val refreshIntervalMillis: Property<Long>

    /**
     * The URI to the repository, using any URI supported by JGit.
     *
     * @return the URI property
     */
    val uri: Property<String>

    /**
     * The branch to checkout.
     *
     * @return the branch property
     */
    val branch: Property<String>

    /**
     * A tag to checkout.
     *
     * @return the tag property
     */
    val tag: Property<String>

    /**
     * A specific commit to checkout.
     *
     * @return the commit property
     */
    val commit: Property<String>

    /**
     * Allows configuring the included build.
     *
     * @param relativePath the relative path from the checkout directory to the project to include.
     */
    fun includeBuild(relativePath: String) {
        includeBuild(relativePath) { }
    }

    /**
     * Allows configuring the included build, in particular dependency substitutions.
     *
     * @param spec the configuration
     */
    fun includeBuild(spec: Action<ConfigurableIncludedBuild>)

    /**
     * Allows configuring the included build.
     *
     * @param relativePath the relative path from the checkout directory to the project to include.
     * @param spec the spec of the included build.
     */
    fun includeBuild(relativePath: String, spec: Action<ConfigurableIncludedBuild>)

    companion object {
        fun Settings.createExtension(): ProtonIncludeCoreBuildExtension = extensions.create(
            ProtonIncludeCoreBuildExtension::class.java,
            "includeCoreBuild",
            DefaultProtonIncludeCoreBuildExtension::class.java,
            this
        )
    }
}

abstract class DefaultProtonIncludeCoreBuildExtension @Inject constructor(
    val settings: Settings,
) : ProtonIncludeCoreBuildExtension {

    @Inject
    protected abstract fun getObjects(): ObjectFactory

    @Inject
    protected abstract fun getProviders(): ProviderFactory

    private val includes: MutableList<IncludedBuild> = mutableListOf()

    override fun includeBuild(spec: Action<ConfigurableIncludedBuild>) {
        includeBuild(".") { spec.execute(this) }
    }

    override fun includeBuild(relativePath: String, spec: Action<ConfigurableIncludedBuild>) {
        includes.add(IncludedBuild(relativePath, spec));
    }

    private fun isLocalPresent() = with(getProviders()) {
        gradleProperty(localRepoProperty).orElse(systemProperty(localRepoProperty)).isPresent
    }

    fun hasIncludes() = includes.isNotEmpty() ||
            isLocalPresent() ||
            branch.isPresent || tag.isPresent || commit.isPresent

    fun configure(includedGitRepo: IncludedGitRepo) {
        when {
            isLocalPresent() -> return
            branch.isPresent -> includedGitRepo.branch.set(branch.get())
            tag.isPresent -> includedGitRepo.tag.set(tag.get())
            commit.isPresent -> includedGitRepo.commit.set(commit.get())
        }

        for (include in includes) {
            includedGitRepo.includeBuild(include.directory, include.spec)
        }
    }

    data class IncludedBuild(
        val directory: String,
        val spec: Action<ConfigurableIncludedBuild>,
    )

    companion object {
        private const val localRepoProperty =
            DefaultIncludeGitExtension.LOCAL_GIT_PREFIX + ProtonIncludeCoreBuildPlugin.repoDir
    }
}
