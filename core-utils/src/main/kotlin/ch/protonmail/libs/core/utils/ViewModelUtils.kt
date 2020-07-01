@file:Suppress("unused") // Public APIs

package ch.protonmail.libs.core.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.get
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/*
 * Utilities for Android's ViewModel
 * Author: Davide Farella
 */

/** A [ViewModelProvider.Factory] that has a checked type for [ViewModel] */
abstract class ViewModelFactory<VM: ViewModel> : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") // T is VM
    override fun <T : ViewModel?> create(modelClass: Class<T>) = create() as T
    abstract fun create() : VM
}

/** @return [ReadOnlyProperty] delegate of type [VM] for [FragmentActivity] */
inline fun <reified VM : ViewModel> FragmentActivity.viewModel(
    crossinline getFactory: () -> ViewModelFactory<VM>? = { null }
) = object : ReadOnlyProperty<FragmentActivity, VM> {
    private val viewModel by lazy { ViewModelProvider(this@viewModel, getFactory()).get<VM>() }
    override fun getValue(thisRef: FragmentActivity, property: KProperty<*>) = viewModel
}

/** @return [ReadOnlyProperty] delegate of type [VM] for [Fragment] */
inline fun <reified VM : ViewModel> Fragment.viewModel(
    crossinline getFactory: () -> ViewModelFactory<VM>? = { null }
) = object : ReadOnlyProperty<Fragment, VM> {
    private val viewModel by lazy { ViewModelProvider(this@viewModel, getFactory()).get<VM>() }
    override fun getValue(thisRef: Fragment, property: KProperty<*>) = viewModel
}

/** @return [ViewModelProvider]. Call its constructor with nullable [ViewModelProvider.Factory] */
@Suppress("FunctionName") // Constructor function
fun ViewModelProvider(owner: ViewModelStoreOwner, factory: ViewModelProvider.Factory? = null) =
    factory?.let { ViewModelProvider(owner, factory) } ?: ViewModelProvider(owner)
