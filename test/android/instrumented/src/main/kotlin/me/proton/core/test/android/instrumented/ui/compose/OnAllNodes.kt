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

import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.printToString
import me.proton.core.test.android.instrumented.FusionConfig

/**
 * Contains identifiers, actions, and checks for Compose UI allNodes element, i.e. [SemanticsNodeInteractionCollection].
 */
class OnAllNodes(
    private val interaction: SemanticsNodeInteractionCollection? = null,
) : NodeBuilder() {

    private fun toNodes(action: () -> SemanticsNodeInteractionCollection) =
        handlePrint {
            if (FusionConfig.compose.shouldPrintToLog) action().printToLog(FusionConfig.fusionTag) else action()
        }

    private fun nodeInteraction(): SemanticsNodeInteractionCollection =
        interaction ?: FusionConfig.compose.testRule.onAllNodes(semanticMatcher(), shouldUseUnmergedTree)

    /** Node selectors **/

    fun onChild(node: OnNode) = OnNode(node.nodeInteraction())

    fun onSibling(node: OnNode) = OnNode(node.nodeInteraction())

    fun onAncestor(node: OnNode) = OnNode(node.nodeInteraction())

    /** Node actions **/
    fun atPosition(position: Int) = OnNode(nodeInteraction()[position])

    fun filterToOne(node: OnNode) = OnNode(nodeInteraction().filterToOne(node.semanticMatcher()))

    fun onFirst() = OnNode(nodeInteraction().onFirst())

    fun onLast() = OnNode(nodeInteraction().onLast())

    fun filter(node: OnNode) = apply { nodeInteraction().filter(node.semanticMatcher()) }

    fun assertAll(node: OnNode) = apply { toNodes { nodeInteraction().assertAll(node.semanticMatcher()) } }

    fun assertAny(node: OnNode) = apply { toNodes { nodeInteraction().assertAny(node.semanticMatcher()) } }

    fun assertCountEquals(count: Int) = apply { toNodes { nodeInteraction().assertCountEquals(count) } }

    fun printToString(depth: Int) = apply { nodeInteraction().printToString(depth) }

    fun printToLog(tag: String, depth: Int) = apply { nodeInteraction().printToLog(tag, depth) }
}
