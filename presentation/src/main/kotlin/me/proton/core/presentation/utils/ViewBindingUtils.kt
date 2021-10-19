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

package me.proton.core.presentation.utils

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import me.proton.core.presentation.R
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Delegates a viewBinding read-only property storing it inside the bind view's tag.
 *
 * usage:
 * ```
 *  class ExampleFragment : Fragment {
 *      //...
 *      private val binding: ExampleFragmentBinding by viewBinding(ExampleFragmentBinding::bind)
 *      // ...
 *  }
 * ```
 *
 * @param ViewBindingT the [ViewBinding] property type.
 * @param bind You need to call [ViewBindingT].bind static method to bind the view binding
 *  to fragment's view.
 * @return a [ViewBindingT] delegates
 *
 */
fun <ViewBindingT : ViewBinding> viewBinding(
    bind: (View) -> ViewBindingT
): ReadOnlyProperty<Fragment, ViewBindingT> {
    return FragmentViewBindingDelegate(bind)
}

private class FragmentViewBindingDelegate<out ViewBindingT : ViewBinding>(
    private val bind: (View) -> ViewBindingT
) : ReadOnlyProperty<Fragment, ViewBindingT> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): ViewBindingT {
        return thisRef.requireView().getOrPutBinding(R.id.view_binding_tag)
    }

    @Suppress("UNCHECKED_CAST")
    private fun View.getOrPutBinding(key: Int): ViewBindingT {
        val binding = getTag(key) as? ViewBindingT
        if (binding == null) {
            val newBinding = bind(this)
            setTag(key, newBinding)
            return newBinding
        }
        return binding
    }
}

fun <ViewBindingT : ViewBinding> Activity.viewBinding(
    bindingInflater: (LayoutInflater) -> ViewBindingT
) = lazy { bindingInflater(layoutInflater) }
