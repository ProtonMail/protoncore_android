package me.proton.core.domain.arch

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.util.kotlin.invoke
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * API test suite for [Mapper]
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class MapperTest {

    data class BusinessModel(val i: Int)
    data class UiModel(val s: String)

    class TestMapper : Mapper<BusinessModel, UiModel> {
        fun BusinessModel.toUiModel() = UiModel(i.toString())
    }

    private val testMapper = TestMapper()

    @Test
    fun `simple model API`() {
        val uiModel = testMapper { BusinessModel(15).toUiModel() }
        assertEquals(UiModel("15"), uiModel)
    }

    @Test
    fun `models List API`() {
        val uiModels = listOf(BusinessModel(10), BusinessModel(15), BusinessModel(20))
            .map(testMapper) { it.toUiModel() }

        assertEquals(
            listOf(UiModel("10"), UiModel("15"), UiModel("20")),
            uiModels
        )
    }

    @Test
    fun `models Flow API`() = runBlockingTest {
        val uiModels = flowOf(BusinessModel(10), BusinessModel(15), BusinessModel(20))
            .map(testMapper) { it.toUiModel() }
            .toList()

        assertEquals(
            listOf(UiModel("10"), UiModel("15"), UiModel("20")),
            uiModels
        )
    }
}
