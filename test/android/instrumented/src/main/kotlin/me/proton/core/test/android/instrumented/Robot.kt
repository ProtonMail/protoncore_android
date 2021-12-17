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

package me.proton.core.test.android.instrumented

import me.proton.core.test.android.instrumented.ui.espresso.OnDevice
import me.proton.core.test.android.instrumented.ui.espresso.OnIntent
import me.proton.core.test.android.instrumented.ui.espresso.OnListView
import me.proton.core.test.android.instrumented.ui.espresso.OnRecyclerView
import me.proton.core.test.android.instrumented.ui.espresso.OnRootView
import me.proton.core.test.android.instrumented.ui.espresso.OnView

interface Robot {

    val device: OnDevice
        get() = OnDevice()

    val intent: OnIntent
        get() = OnIntent()

    val listView: OnListView
        get() = OnListView()

    val recyclerView: OnRecyclerView
        get() = OnRecyclerView()

    val rootView: OnRootView
        get() = OnRootView()

    val view: OnView
        get() = OnView()
}
