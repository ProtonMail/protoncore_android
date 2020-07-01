package ch.protonmail.libs.lint.detectors.testUtils

import ch.protonmail.libs.lint.DetectorIssueProvider
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin

/**
 * Base class for Tests for Lint
 * @param provider [DetectorIssueProvider] for provide issues to apply to [TestLintTask]
 *
 * @author Davide Farella
 */
abstract class LintTest(
    private val provider: DetectorIssueProvider,
    private val useAndroidSdk: Boolean = false
) {

    /**
     * Create a [TestLintTask] from the receiver [String] representation of a Kotlin file
     * @return [TestLintTask]
     * @see setup
     */
    protected fun String.setupTest() : TestLintTask {
        val formatted = trimMargin()
        val task = lint().files(kotlin(formatted))
        return task.setup()
    }

    /** Apply issues to the receiver [TestLintTask] using the declared [provider] */
    private fun TestLintTask.setup(): TestLintTask = apply {
        issues(*provider.ISSUES.toTypedArray())
        allowMissingSdk(!useAndroidSdk)
    }
}
