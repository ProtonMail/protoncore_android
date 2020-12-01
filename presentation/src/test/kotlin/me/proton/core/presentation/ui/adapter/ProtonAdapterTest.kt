/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.presentation.ui.adapter

import android.os.Build
import android.os.Looper.getMainLooper
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import me.proton.core.test.android.ExecutorsTest
import me.proton.core.test.android.executorsTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test suite for [ProtonAdapter]
 */
@Config(sdk = [Build.VERSION_CODES.M])
@RunWith(RobolectricTestRunner::class)
internal class ProtonAdapterTest : ExecutorsTest by executorsTest {

    private data class ExampleUiModel(val id: Int, val name: String) {
        companion object {
            val DiffCallback = object : DiffUtil.ItemCallback<ExampleUiModel>() {

                override fun areItemsTheSame(oldItem: ExampleUiModel, newItem: ExampleUiModel) =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: ExampleUiModel, newItem: ExampleUiModel) =
                    oldItem == newItem

            }
        }
    }

    @Test
    fun filterWorksCorrectly() {

        // GIVEN
        val adapter = ProtonAdapter(
            getView = { parent, _ -> TextView(parent.context) },
            onBind = {},
            diffCallback = ExampleUiModel.DiffCallback,
            onFilter = { element, constraint -> constraint in element.name }
        )
            .apply {
                submitList((0..20).map { ExampleUiModel(it, "name: $it") })
            }

        // WHEN
        adapter.filter.filter(null)
        adapter.filter.filter("hello world")
        adapter.filter.filter("1")

        shadowOf(getMainLooper()).idle()

        // THEN
        // 11 items with name matching "1"
        assertEquals(11, adapter.itemCount)
    }
}
