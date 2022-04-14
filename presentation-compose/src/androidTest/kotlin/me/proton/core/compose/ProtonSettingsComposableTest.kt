package me.proton.core.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onSibling
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import me.proton.core.compose.component.ProtonSettingsRadioItem
import me.proton.core.compose.component.ProtonSettingsToggleItem
import me.proton.core.compose.theme.ProtonTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class ProtonSettingsComposableTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testToggleViewSwitchIsDisabledWhenValueIsNull() {
        setupToggleItemWith(
            name = "Conversation Grouping",
            hint = "Turn this on to group messages that belong to the same conversation",
            value = null
        )

        composeTestRule
            .onNode(isToggleable())
            .assertIsDisplayed()
            .assertIsNotEnabled()
            .assertIsOff()
    }

    @Test
    fun testToggleViewShowsOnlyNameWhenHintIsNull() {
        setupToggleItemWith(
            name = "Conversation Grouping",
            hint = null,
            value = null
        )

        composeTestRule
            .onNodeWithText("Conversation Grouping")
            .assertIsDisplayed()
            .onSibling()
            .assertDoesNotExist()
    }

    @Test
    fun testRadioItemIsNotSelected() {
        setupRadioItemWith("Unselected item", false)

        composeTestRule
            .onNodeWithText("Unselected item")
            .assertIsDisplayed()
            .onChild()
            .assertIsNotSelected()
    }

    @Test
    fun testRadioItemIsSelected() {
        setupRadioItemWith("Selected item", true)

        composeTestRule
            .onNodeWithText("Selected item")
            .onChild()
            .assertIsDisplayed()
            .assertIsSelected()
    }

    @Test
    fun testRadioItemCallbackIsInvokedWithItemNameWhenAnItemIsSelected() {
        var selectedItemName: String? = null
        setupRadioItemWith("Option1", false) {
            selectedItemName = it
        }
        assertNull(selectedItemName)

        composeTestRule
            .onNodeWithText("Option1")
            .performClick()

        assertEquals("Option1", selectedItemName)
    }


    private fun setupRadioItemWith(
        name: String,
        value: Boolean,
        onItemSelected: (String) -> Unit = {}
    ) {
        composeTestRule.setContent {
            ProtonTheme {
                ProtonSettingsRadioItem(
                    name = name,
                    isSelected = value,
                    onItemSelected = onItemSelected
                )
            }
        }
    }

    private fun setupToggleItemWith(name: String, hint: String?, value: Boolean?) {
        composeTestRule.setContent {
            ProtonTheme {
                ProtonSettingsToggleItem(
                    name = name,
                    hint = hint,
                    value = value,
                    onToggle = {}
                )
            }
        }
    }

}
