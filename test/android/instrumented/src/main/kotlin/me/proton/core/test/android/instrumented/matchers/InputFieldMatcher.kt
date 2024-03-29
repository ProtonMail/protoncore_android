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

package me.proton.core.test.android.instrumented.matchers

import android.view.View
import android.widget.EditText
import me.proton.core.presentation.R
import me.proton.core.presentation.ui.view.ProtonInput
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun inputFieldMatcher(id: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        public override fun matchesSafely(view: View): Boolean {
            if (view is EditText) {
                if (view.id == id) return true
                if (view.id == R.id.input) {
                    // EditText probably part of a ProtonInput, checking parent
                    val possibleProtonInputParent = view.parent.parent?.parent
                    if (possibleProtonInputParent is ProtonInput && possibleProtonInputParent.id == id) {
                        return true
                    }
                }
            }
            return false
        }

        override fun describeTo(description: Description) {
            description.appendText("Input field is a ProtonInput or an EditText")
        }
    }
}
