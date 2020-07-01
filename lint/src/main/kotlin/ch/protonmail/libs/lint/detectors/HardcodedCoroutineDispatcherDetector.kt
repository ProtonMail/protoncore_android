@file:Suppress("UnstableApiUsage")

package ch.protonmail.libs.lint.detectors

import ch.protonmail.libs.lint.DetectorIssueProvider
import ch.protonmail.libs.lint.categories.Category.TESTABILITY
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.android.tools.lint.detector.api.Severity.WARNING
import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.kotlin.KotlinUFunctionCallExpression
import org.jetbrains.uast.util.isConstructorCall

/**
 * Lint detector for find hardcoded [CoroutineDispatcher] in classes
 * @author Davide Farella
 */
class HardcodedCoroutineDispatcherDetector : Detector(), SourceCodeScanner {

    /** Declared list for UastType that must be checked by the Linter */
    override fun getApplicableUastTypes() = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext) = Handler(context)

    /** [UElementHandler] for visit elements */
    class Handler(private val context: JavaContext) : UElementHandler() {

        override fun visitCallExpression(node: UCallExpression) {
            // filter constructors
            if (!node.isConstructorCall()) this(node)
        }

        /** Run the evaluations and report if needed */
        operator fun invoke(node: UCallExpression) {
            // `true` if any arguments matches `DISPATCHER_MATCHERS`
            val hasHardcodedCoroutineDispatcher =
                (node as? KotlinUFunctionCallExpression)?.valueArguments?.any {
                    it.asSourceString() in DISPATCHER_MATCHERS
                } ?: false

            if (hasHardcodedCoroutineDispatcher) {
                context.report(
                    issue = Issue(),
                    scope = node as UElement,
                    location = context.getNameLocation(node),
                    message = "Inject the Dispatcher in the constructor instead"//,
                    // TODO: quickfixData = fix(initializerCode, "Replace with constant")
                )
            }
        }


        /** @return [LintFix] */
        private fun fix(
            code: String,
            fieldName: String,
            @Suppress("SameParameterValue") message: String
        ): LintFix = TODO("create quickFix")
//        LintFix.create()
//        .name(message)
//        .replace()
//        .text(code)
//        .with("@SerialName(\"$fieldName\")\n$code")
//        .build()
    }

    companion object : DetectorIssueProvider {

        override val ISSUES = listOf(Issue())

        private fun id() = "CoroutineDispatcher_hardcoded"
        private fun description() = "'SerialName' annotation is missing on field of 'Serializable' entity"
        private fun explanation() = "Provide a 'SerialName' value"

        @Suppress("FunctionName")
        private fun Issue() = Issue.create(
            id = id(),
            briefDescription = description(),
            explanation = explanation(),
            category = TESTABILITY,
            priority = 5,
            severity = WARNING,
            implementation = Implementation(
                HardcodedCoroutineDispatcherDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        private val PATHS = arrayOf("", "Dispatchers.", "kotlinx.coroutines.Dispatchers.")
        private val TYPES = arrayOf("Main", "Default", "IO")
        val DISPATCHER_MATCHERS = PATHS.flatMap { path -> TYPES.map { type -> "$path$type" } }
    }
}
