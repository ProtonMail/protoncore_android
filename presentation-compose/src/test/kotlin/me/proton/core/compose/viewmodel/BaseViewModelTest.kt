package me.proton.core.compose.viewmodel

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import kotlin.test.BeforeTest
import kotlin.test.Test

class BaseViewModelTest : CoroutinesTest by CoroutinesTest() {
    private lateinit var tested: ExampleViewModel

    @BeforeTest
    fun setUp() {
        tested = ExampleViewModel()
    }

    @Test
    fun `state is emitted after an error`() = coroutinesTest {
        tested.state.test {
            assertIs<ExampleState.Loading>(awaitItem())

            tested.perform(ExampleAction.LoadWithError)
            assertIs<ExampleState.Error>(awaitItem())

            tested.perform(ExampleAction.Load)
            assertIs<ExampleState.Idle>(awaitItem())
        }
    }
}

private class ExampleViewModel : BaseViewModel<ExampleAction, ExampleState>(
    initialAction = ExampleAction.LoadWithError,
    initialState = ExampleState.Loading
) {
    override fun onAction(action: ExampleAction): Flow<ExampleState> = when (action) {
        is ExampleAction.Load -> flow {
            emit(ExampleState.Idle)
        }

        is ExampleAction.LoadWithError -> flow {
            emit(ExampleState.Loading)
            error("Loading error")
        }
    }

    override suspend fun FlowCollector<ExampleState>.onError(throwable: Throwable) {
        emit(ExampleState.Error(throwable))
    }
}

private sealed interface ExampleAction {
    data object Load : ExampleAction
    data object LoadWithError : ExampleAction
}

private sealed interface ExampleState {
    data object Loading : ExampleState
    data object Idle : ExampleState
    data class Error(val throwable: Throwable) : ExampleState
}
