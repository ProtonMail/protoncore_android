@file:Suppress("UnstableApiUsage")

package ch.protonmail.libs.lint

import ch.protonmail.libs.lint.detectors.HardcodedCoroutineDispatcherDetector
import ch.protonmail.libs.lint.detectors.NoSerialNameAnnotationDetector
import ch.protonmail.libs.lint.detectors.NotConstantStringDetector
import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

/**
 * [IssueRegistry] for custom lint for Proton apps and libs
 * @author Davide Farella
 */
@Suppress("unused") // Declared in Gradle
class ProtonIssueRegistry : IssueRegistry() {

    /**
     * The Lint API version this issue registry's checks were compiled.
     * You should return [CURRENT_API].
     */
    override val api: Int = CURRENT_API

    /**
     * The list of issues that can be found by all known detectors (including those that may be
     * disabled!)
     */
    override val issues: List<Issue> get() = listOf(
        HardcodedCoroutineDispatcherDetector,
        NoSerialNameAnnotationDetector,
        NotConstantStringDetector,
        UsesCleartextTrafficManifestDetector
    ).flatMap { it.ISSUES }
}
