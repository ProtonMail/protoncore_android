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

import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.AppCompatImageView
import me.proton.core.test.android.instrumented.ui.espresso.OnView

internal object SystemUI {
    val positiveDialogBtn = OnView().withId(android.R.id.button1)
    val negativeDialogBtn = OnView().withId(android.R.id.button2)
    val neutralDialogBtn = OnView().withId(android.R.id.button3)
    val moreOptionsBtn = OnView()
        .instanceOf(AppCompatImageView::class.java)
        .withParent(OnView().instanceOf(ActionMenuView::class.java))
}
