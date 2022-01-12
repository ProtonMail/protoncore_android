/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.test.android.instrumented.ui.compose

import androidx.annotation.StringRes
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.text.input.ImeAction
import me.proton.core.test.android.instrumented.FusionConfig
import me.proton.core.test.android.instrumented.utils.StringUtils
import java.util.ArrayList

open class NodeBuilder {
    var shouldUseUnmergedTree: Boolean = false
    private val semanticsMatchers = ArrayList<SemanticsMatcher>()

    /**
     * Final semantic matcher
     */
    fun semanticMatcher(): SemanticsMatcher {
        try {
            var finalSemanticMatcher = semanticsMatchers.first()
            semanticsMatchers.drop(1).forEach {
                finalSemanticMatcher = finalSemanticMatcher.and(it)
            }
            return finalSemanticMatcher
        } catch (ex: NoSuchElementException) {
            throw AssertionError("At least one matcher should be provided to operate on the node.")
        }
    }

    /** Node filters **/
    fun withText(@StringRes textId: Int, substring: Boolean = false, ignoreCase: Boolean = true) = apply {
        withText(StringUtils.stringFromResource(textId, substring, ignoreCase))
    }

    fun withText(text: String, substring: Boolean = false, ignoreCase: Boolean = true) = apply {
        semanticsMatchers.add(hasText(text, substring, ignoreCase))
    }

    fun containsText(@StringRes textId: Int) = apply {
        withText(StringUtils.stringFromResource(textId), substring = true, ignoreCase = false)
    }

    fun containsText(text: String) = apply {
        withText(text, substring = true, ignoreCase = false)
    }

    fun withContentDesc(contentDescriptionText: String, substring: Boolean = false, ignoreCase: Boolean = true) =
        apply {
            semanticsMatchers.add(hasContentDescription(contentDescriptionText, substring, ignoreCase))
        }

    fun withContentDesc(
        @StringRes contentDescriptionTextId: Int,
        substring: Boolean = false,
        ignoreCase: Boolean = true
    ) = apply {
        withContentDesc(StringUtils.stringFromResource(contentDescriptionTextId, substring, ignoreCase))
    }

    fun withContentDescContains(@StringRes textId: Int) = apply {
        withContentDesc(StringUtils.stringFromResource(textId), substring = true, ignoreCase = false)
    }

    fun withContentDescContains(contentDescriptionText: String) = apply {
        withContentDesc(contentDescriptionText, substring = true, ignoreCase = false)
    }

    fun withUnmergedTree() = apply {
        shouldUseUnmergedTree = true
    }

    fun withTag(tag: String) = apply {
        semanticsMatchers.add(SemanticsMatcher.expectValue(SemanticsProperties.TestTag, tag))
    }

    fun isClickable() = apply {
        semanticsMatchers.add(SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick))
    }

    fun isNotClickable() = apply {
        semanticsMatchers.add(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
    }

    fun isChecked() = apply {
        semanticsMatchers.add(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.On))
    }

    fun isNotChecked() = apply {
        semanticsMatchers.add(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.Off))
    }

    fun isCheckable() =
        apply { semanticsMatchers.add(SemanticsMatcher.keyIsDefined(SemanticsProperties.ToggleableState)) }

    fun isEnabled() = apply {
        semanticsMatchers.add(!SemanticsMatcher.keyIsDefined(SemanticsProperties.Disabled))
    }

    fun isDisabled() = apply {
        semanticsMatchers.add(SemanticsMatcher.keyIsDefined(SemanticsProperties.Disabled))
    }

    fun isFocusable() = apply {
        semanticsMatchers.add(SemanticsMatcher.keyIsDefined(SemanticsProperties.Focused))
    }

    fun isNotFocusable() = apply {
        semanticsMatchers.add(SemanticsMatcher.keyNotDefined(SemanticsProperties.Focused))
    }

    fun isFocused() = apply {
        semanticsMatchers.add(SemanticsMatcher.expectValue(SemanticsProperties.Focused, true))
    }

    fun isNotFocused() = apply {
        semanticsMatchers.add(SemanticsMatcher.expectValue(SemanticsProperties.Focused, false))
    }

    fun isSelected() = apply {
        semanticsMatchers.add(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
    }

    fun isNotSelected() = apply {
        semanticsMatchers.add(SemanticsMatcher.expectValue(SemanticsProperties.Selected, false))
    }

    fun isSelectable() = apply {
        semanticsMatchers.add(SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected))
    }

    fun isScrollable() = apply {
        semanticsMatchers.add(SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollBy))
    }

    fun isNotScrollable() = apply {
        semanticsMatchers.add(SemanticsMatcher.keyNotDefined(SemanticsActions.ScrollBy))
    }

    fun isHeading() = apply {
        semanticsMatchers.add(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
    }

    fun isDialog() = apply {
        semanticsMatchers.add(SemanticsMatcher.keyIsDefined(SemanticsProperties.IsDialog))
    }

    fun isPopup() = apply {
        semanticsMatchers.add(SemanticsMatcher.keyIsDefined(SemanticsProperties.IsPopup))
    }

    fun isRoot() = apply {
        semanticsMatchers.add(SemanticsMatcher("isRoot") { it.isRoot })
    }

    fun withProgressBarRangeInfo(rangeInfo: ProgressBarRangeInfo) = apply {
        semanticsMatchers.add(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, rangeInfo))
    }

    fun hasStateDescription(stateDescription: String) = apply {
        semanticsMatchers.add(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, stateDescription))
    }

    fun hasImeAction(imeAction: ImeAction) = apply {
        semanticsMatchers.add(SemanticsMatcher.expectValue(SemanticsProperties.ImeAction, imeAction))
    }

    fun hasSetTextAction() = apply {
        semanticsMatchers.add(SemanticsMatcher.keyIsDefined(SemanticsActions.SetText))
    }

    fun isDescendantOf(ancestorNode: OnNode) = apply {
        semanticsMatchers.add(hasAnyAncestor(ancestorNode.semanticMatcher()))
    }

    fun hasParent(parentNode: OnNode) = apply {
        semanticsMatchers.add(parentNode.semanticMatcher())
    }

    fun hasChild(childNode: OnNode) = apply {
        semanticsMatchers.add(hasAnyChild(childNode.semanticMatcher()))
    }

    fun hasSibling(siblingNode: OnNode) = apply {
        semanticsMatchers.add(hasAnySibling(siblingNode.semanticMatcher()))
    }

    fun hasDescendant(descendantNode: OnNode) = apply {
        semanticsMatchers.add(hasAnyDescendant(descendantNode.semanticMatcher()))
    }

    fun handlePrint(action: () -> Any) {
        try {
            action()
        } catch (e: AssertionError) {
            if (FusionConfig.compose.shouldPrintHierarchyOnFailure) {
                FusionConfig.compose.testRule.onRoot().printToLog(FusionConfig.fusionTag)
            }
        }
    }
}
