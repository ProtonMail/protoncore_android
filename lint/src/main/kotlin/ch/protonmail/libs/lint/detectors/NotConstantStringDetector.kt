@file:Suppress("UnstableApiUsage")

package ch.protonmail.libs.lint.detectors

import ch.protonmail.libs.lint.DetectorIssueProvider
import ch.protonmail.libs.lint.detectors.NotConstantStringDetector.Companion.Issue
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.android.tools.lint.detector.api.Category.Companion.CORRECTNESS
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UVariable

/**
 * Lint detector for find hardcoded [String]s that should replaced with constants
 * @author Davide Farella
 */
class NotConstantStringDetector : Detector(), SourceCodeScanner {

    /** Declared list for UastType that must be checked by the Linter */
    override fun getApplicableUastTypes() = listOf(UVariable::class.java)

    override fun createUastHandler(context: JavaContext) = Handler(context)

    /** [UElementHandler] for visit elements */
    class Handler(private val context: JavaContext) : UElementHandler() {

        override fun visitVariable(node: UVariable) {
            this(node)
        }

        /** Run the evaluations and report if needed */
        operator fun invoke(node: UVariable) {
            val initializerCode = node.uastInitializer?.asSourceString()
            evaluations.forEach { (issue, matcher) ->
                if (matcher.doesMatch(initializerCode)) {
                    context.report(
                        issue =         issue,
                        scope =         node as UElement,
                        location =      context.getNameLocation(node),
                        message =       "Use '${matcher.validCode}' constant instead",
                        quickfixData =  matcher.fix(initializerCode, "Replace with constant")
                    )
                }
            }
        }
    }

    /** A matcher for evaluate and replace invalid code */
    private class Matcher(val invalidCode: String, val validCode: String) {

        /** @return `true` if [code] matches [invalidCode] */
        fun doesMatch(code: String?) = code == invalidCode

        /** @return [LintFix] */
        fun fix(code: String?, message: String): LintFix = LintFix.create()
            .name(message)
            .replace()
            .text(code)
            .with(validCode)
            .build()
    }

    companion object : DetectorIssueProvider {

        /** Map of [Issue]s with relative [Matcher] */
        private val evaluations = mapOf(

            // Kotlin methods
            IssueMatcherPair("Charsets.UTF_8", "charset(\"UTF8\")"),

            // ch.protonmail.libs.core.utils.StringUtils.kt
            IssueMatcherPair("EMPTY_STRING", "\"\""),

            // ch.protonmail.libs.core.ProtonAddresses.kt
            IssueMatcherPair("HTTP_PROTONMAIL_CH", "\"http://protonmail.ch\""),
            IssueMatcherPair("HTTPS_PROTONMAIL_CH", "\"https://protonmail.ch\""),
            IssueMatcherPair("HTTP_PROTONMAIL_COM", "\"http://protonmail.com\""),
            IssueMatcherPair("HTTPS_PROTONMAIL_COM", "\"https://protonmail.com\""),

            // ch.protonmail.libs.crypto.Constants.kt
            IssueMatcherPair("AES", "\"AES\""),
            IssueMatcherPair("RSA", "\"RSA\""),
            IssueMatcherPair("SHA_256", "\"SHA-256\"")
        )

        override val ISSUES = evaluations.keys.toList()

        private fun id(constantName: String) = "${constantName}_constant"
        private fun description() = "Use constant instead of hardcoded string"
        private fun explanation(constantName: String) = "'$constantName' constant should be used"

        @Suppress("FunctionName")
        private fun Issue(constantName: String) = Issue.create(
            id =                id(constantName),
            briefDescription =  description(),
            explanation =       explanation(constantName),
            category =          CORRECTNESS,
            priority =          3,
            severity =          Severity.WARNING,
            implementation =    Implementation(NotConstantStringDetector::class.java, Scope.JAVA_FILE_SCOPE)
        )

        /** @return [Pair] of [Issue] and [Matcher] */
        @Suppress("FunctionName") // Constructor function
        private fun IssueMatcherPair(constantName: String, invalidCode: String) =
            Issue(constantName) to Matcher(invalidCode, constantName)
    }
}
