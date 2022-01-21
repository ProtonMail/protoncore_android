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

import android.util.Log
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.semantics.AccessibilityAction
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsToggleable
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAncestors
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.onSibling
import androidx.compose.ui.test.onSiblings
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performKeyPress
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import me.proton.core.test.android.instrumented.FusionConfig
import me.proton.core.test.android.instrumented.ProtonTest

/**
 * Contains identifiers, actions, and checks for Compose UI onNode element, i.e. [SemanticsNodeInteraction].
 */
class OnNode(
    private val interaction: SemanticsNodeInteraction? = null,
) : NodeBuilder<OnNode>() {

    fun nodeInteraction(doesNotExist: Boolean = false, timeout: Long = 10_000L): SemanticsNodeInteraction {
        if (interaction == null) {
            val newInteraction = FusionConfig.compose.testRule.onNode(
                semanticMatcher(),
                shouldUseUnmergedTree
            )
            if (!doesNotExist) {
                FusionConfig.compose.testRule.waitUntil(timeout) { exists() }
            }
            return newInteraction
        }
        return interaction
    }

    private fun toNode(action: () -> SemanticsNodeInteraction) =
        handlePrint {
            if (FusionConfig.compose.shouldPrintToLog) action().printToLog(FusionConfig.fusionTag) else action()
        }

    /** Node actions **/
    fun click() = apply { toNode { nodeInteraction().performScrollTo() } }

    fun performAction(action: SemanticsPropertyKey<AccessibilityAction<() -> Boolean>>) = apply {
        toNode { nodeInteraction().apply { performSemanticsAction(action) } }
    }

    fun scrollTo() = apply { toNode { nodeInteraction().performScrollTo() } }

    fun sendTouchInput(block: TouchInjectionScope.() -> Unit) = apply { toNode { nodeInteraction().performTouchInput(block) } }

    fun clearText() = apply { toNode { nodeInteraction().apply { performTextClearance() } } }

    fun typeText(text: String) = apply { toNode { nodeInteraction().apply { performTextInput(text) } } }

    fun replaceText(text: String) = apply { toNode { nodeInteraction().apply { performTextReplacement(text) } } }

    fun pressKey(keyEvent: KeyEvent) = apply { toNode { nodeInteraction().apply { performKeyPress(keyEvent) } } }

    fun sendImeAction() = apply { toNode { nodeInteraction().apply { performImeAction() } } }

    /** Node checks **/
    fun checkExist() = apply { toNode { nodeInteraction().assertExists() } }

    fun checkNotExist() = apply { nodeInteraction(doesNotExist = true).assertDoesNotExist() }

    fun checkDisplayed() = apply { toNode { nodeInteraction().assertIsDisplayed() } }

    fun checkNotDisplayed() = apply { toNode { nodeInteraction().assertIsNotDisplayed() } }

    fun checkEnabled() = apply { toNode { nodeInteraction().assertIsEnabled() } }

    fun checkNotEnabled() = apply { toNode { nodeInteraction().assertIsNotEnabled() } }

    fun checkIsChecked() = apply { toNode { nodeInteraction().assertIsOn() } }

    fun checkNotChecked() = apply { toNode { nodeInteraction().assertIsOff() } }

    fun checkSelected() = apply { toNode { nodeInteraction().assertIsSelected() } }

    fun checkNotSelected() = apply { toNode { nodeInteraction().assertIsNotSelected() } }

    fun checkIsCheckable() = apply { toNode { nodeInteraction().assertIsToggleable() } }

    fun checkSelectable() = apply { toNode { nodeInteraction().assertIsSelectable() } }

    fun checkFocused() = apply { toNode { nodeInteraction().assertIsFocused() } }

    fun checkNotFocused() = apply { toNode { nodeInteraction().assertIsNotFocused() } }

    fun checkContentDescEquals(value: String) =
        apply { toNode { nodeInteraction().assertContentDescriptionEquals(value) } }

    fun checkContentDescContains(text: String) =
        apply {
            toNode {
                nodeInteraction().assertContentDescriptionContains(
                    text,
                    substring = false,
                    ignoreCase = false
                )
            }
        }

    fun checkContentDescContainsIgnoringCase(text: String) =
        apply {
            toNode {
                nodeInteraction().assertContentDescriptionContains(
                    text,
                    substring = false,
                    ignoreCase = true
                )
            }
        }

    fun checkTextEquals(value: String) = apply {
        toNode { nodeInteraction().assertTextEquals(value) }
    }

    fun checkProgressBar(range: ProgressBarRangeInfo) = apply {
        toNode { nodeInteraction().assertRangeInfoEquals(range) }
    }

    fun checkClickable() = apply { toNode { nodeInteraction().assertHasClickAction() } }

    fun checkNotClickable() = apply { toNode { nodeInteraction().assertHasNoClickAction() } }

    fun check(matcher: SemanticsMatcher, messagePrefixOnError: (() -> String)?) = apply {
        toNode { nodeInteraction().assert(matcher, messagePrefixOnError) }
    }

    /** Node selectors **/
    fun onChildAt(position: Int) = nodeInteraction().onChildAt(position)

    fun onChild() = OnNode(nodeInteraction().onChild()).nodeInteraction()

    fun onParent() = OnNode(nodeInteraction().onParent()).nodeInteraction()

    fun onSibling() = OnNode(nodeInteraction().onSibling()).nodeInteraction()

    fun onChildren() = OnAllNodes(nodeInteraction().onChildren())

    fun onSiblings() = OnAllNodes(nodeInteraction().onSiblings())

    fun onAncestors() = OnAllNodes(nodeInteraction().onAncestors())

    /** Helpers **/
    fun exists(): Boolean {
        try {
            checkExist()
        } catch (e: Exception) {
            val firstLine = e.message?.split("\n")?.get(0)
            Log.v(ProtonTest.testTag,"Waiting for condition. Status: $firstLine")
            return false
        }
        return true
    }
}
