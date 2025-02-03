package ch.protonmail.libs.lint.detectors

import com.android.tools.lint.detector.api.*
import org.w3c.dom.Element

class UsesCleartextTrafficManifestDetector : Detector(), XmlScanner {

    override fun getApplicableElements(): Collection<String> {
        // Look for <uses-permission> and <application> tags
        return listOf("uses-permission", "application")
    }

    override fun visitElement(context: XmlContext, element: Element) {
        if (element.tagName == "application") {
            val cleartextTraffic = element.getAttribute("android:usesCleartextTraffic")
            if (cleartextTraffic == "true") {
                context.report(
                    ISSUE_FORBIDDEN_USES_CLEARTEXT,
                    element,
                    context.getLocation(element),
                    "`usesCleartextTraffic=\"true\"` must not be included in the release manifest."
                )
            }
        }
    }

    companion object {
        val ISSUE_FORBIDDEN_USES_CLEARTEXT = Issue.create(
            id = "ForbiddenUsesCleartextTraffic",
            briefDescription = "Forbidden usesCleartextTraffic in release build",
            explanation = "Cleartext traffic should not be enabled in the release manifest.",
            category = Category.SECURITY,
            priority = 9,
            severity = Severity.ERROR,
            implementation = Implementation(
                ForbiddenManifestCheck::class.java,
                Scope.MANIFEST_SCOPE
            )
        )
    }
}
