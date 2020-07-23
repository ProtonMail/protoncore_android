package me.proton.android.core.presentation.ui.adapter

import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import me.proton.core.test.android.ExecutorsTest
import me.proton.core.test.android.executorsTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test suite for [ProtonAdapter]
 */
@RunWith(RobolectricTestRunner::class)
internal class ProtonAdapterTest: ExecutorsTest by executorsTest {
    
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
    fun `filter works correctly`() {

        // GIVEN
        val adapter = ProtonAdapter(
            getView = { parent, _ ->  TextView(parent.context) },
            onBind = {},
            diffCallback = ExampleUiModel.DiffCallback,
            onFilter = { element, constraint-> constraint in element.name }
        )
            .apply {
                submitList((0..20).map { ExampleUiModel(it, "name: $it") })
            }

        // WHEN
        adapter.filter.filter(null)
        adapter.filter.filter("hello world")
        adapter.filter.filter("1")

        // THEN
        // 11 items with name matching "1"
        assertEquals(11, adapter.itemCount)
    }
}
