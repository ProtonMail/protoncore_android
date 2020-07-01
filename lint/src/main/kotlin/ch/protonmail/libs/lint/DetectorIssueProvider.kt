@file:Suppress("PropertyName", "UnstableApiUsage")

package ch.protonmail.libs.lint

import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

/**
 * An interface for Companion objects of [Detector]s for get it's [Issue]s
 * @author Davide Farella
 */
interface DetectorIssueProvider {

    /** @return [List] of [Issue] for given [Detector] */
    val ISSUES: List<Issue>
}
