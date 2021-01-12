/*
 * Copyright (c) 2020 Proton Technologies AG
 *
 * This file is part of ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail. If not, see https://www.gnu.org/licenses/.
 */

package me.proton.core.test.android.instrumented.uimatchers

import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.AppCompatImageView
import me.proton.core.test.android.instrumented.CoreRobot

object SystemUIMatchers : CoreRobot {

    val positiveDialogBtn = view.withId(android.R.id.button1)
    val negativeDialogBtn = view.withId(android.R.id.button2)
    val neutralDialogBtn = view.withId(android.R.id.button3)

    val moreOptionsBtn = view
        .instanceOf(AppCompatImageView::class.java)
        .withParent(view.instanceOf(ActionMenuView::class.java))
}
