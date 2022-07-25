package me.proton.core.util.kotlin

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Provides [CoroutineDispatcher]s in order to inject them in the constructor of a component allowing it to be tested
 *
 * @author Davide Farella
 */
@Suppress("PropertyName", "VariableNaming") // Non conventional naming starting with uppercase letter
interface DispatcherProvider {

    /** [CoroutineDispatcher] meant to run IO operations */
    val Io: CoroutineDispatcher

    /** [CoroutineDispatcher] meant to run computational operations */
    val Comp: CoroutineDispatcher

    /** [CoroutineDispatcher] meant to run on main thread */
    val Main: CoroutineDispatcher
}
