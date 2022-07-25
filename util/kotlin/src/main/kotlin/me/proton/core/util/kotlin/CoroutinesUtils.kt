package me.proton.core.util.kotlin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

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

@Suppress("UseDataClass")
class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override val Io: CoroutineDispatcher = Dispatchers.IO
    override val Comp: CoroutineDispatcher = Dispatchers.Default
    override val Main: CoroutineDispatcher = Dispatchers.Main
}

@Suppress("PropertyName", "VariableNaming")
interface CoroutineScopeProvider {
    val GlobalDefaultSupervisedScope: CoroutineScope
    val GlobalIOSupervisedScope: CoroutineScope
}

@Suppress("UseDataClass")
class DefaultCoroutineScopeProvider @Inject constructor(dispatcherProvider: DispatcherProvider) :
    CoroutineScopeProvider {
    override val GlobalDefaultSupervisedScope = CoroutineScope(dispatcherProvider.Comp + SupervisorJob())
    override val GlobalIOSupervisedScope = CoroutineScope(dispatcherProvider.Io + SupervisorJob())
}
