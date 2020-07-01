@file:Suppress("UnstableApiUsage")

package ch.protonmail.libs.lint.detectors

import ch.protonmail.libs.lint.DetectorIssueProvider
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.android.tools.lint.detector.api.Category.Companion.CORRECTNESS
import com.intellij.psi.PsiField
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.getContainingUClass

/**
 * Lint detector for find fields of [Serializable] entities that has not [SerialName] annotation
 * @author Davide Farella
 */
class NoSerialNameAnnotationDetector : Detector(), SourceCodeScanner {

    /** Declared list for UastType that must be checked by the Linter */
    override fun getApplicableUastTypes() = listOf(UAnnotation::class.java)

    override fun createUastHandler(context: JavaContext) = Handler(context)

    /** [UElementHandler] for visit elements */
    class Handler(private val context: JavaContext) : UElementHandler() {

        /** Filter by [Serializable] annotations */
        override fun visitAnnotation(node: UAnnotation) {
            val source = node.sourcePsi ?: return
            if (source.text == "@${Serializable::class.simpleName}") {
                this(node)
            }
        }

        /** Run the evaluations on [UAnnotation] */
        operator fun invoke(node: UAnnotation) {
            val uClass = node.getContainingUClass() ?: return
            uClass.allFields.forEach { this(it) }
        }

        /** Run the evaluations on [PsiField] and report if needed */
        operator fun invoke(field: PsiField) {
            val code = field.text
            if (!code.startsWith("@${SerialName::class.simpleName}")) {
                context.report(
                    issue =         Issue(),
                    scope =         field,
                    location =      context.getNameLocation(field),
                    message =       "Missing 'SerialName' annotation",
                    quickfixData =  fix(code, field.name, "Add 'SerialName' annotation")
                )
            }
        }

        /** @return [LintFix] */
        private fun fix(
            code: String,
            fieldName: String,
            @Suppress("SameParameterValue") message: String
        ): LintFix = LintFix.create()
            .name(message)
            .replace()
            .text(code)
            .with("@SerialName(\"$fieldName\")\n$code")
            .build()
    }

    companion object : DetectorIssueProvider {

        override val ISSUES = listOf(Issue())

        private fun id() = "SerialName_missing"
        private fun description() = "'SerialName' annotation is missing on field of 'Serializable' entity"
        private fun explanation() = "Provide a 'SerialName' value"

        @Suppress("FunctionName")
        private fun Issue() = Issue.create(
            id =                id(),
            briefDescription =  description(),
            explanation =       explanation(),
            category =          CORRECTNESS,
            priority =          5,
            severity =          Severity.ERROR,
            implementation =    Implementation(NoSerialNameAnnotationDetector::class.java, Scope.JAVA_FILE_SCOPE)
        )
    }
}
