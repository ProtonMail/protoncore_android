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

package me.proton.android.core.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import me.proton.android.core.presentation.utils.getAnnotatedLayout

/**
 * Created by dinokadrikj on 5/15/20.
 *
 * Base Proton Fragment from which all project fragments should extend.
 */
abstract class ProtonFragment<VM : ViewModel, DB : ViewDataBinding> : Fragment() {

    lateinit var viewModel: VM

    private var internalBinding: DB? = null
    protected val binding: DB
        get() = internalBinding
            ?: throw IllegalStateException("Accessing binding outside of lifecycle")

    open fun onViewCreated() {}

    abstract fun initViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        internalBinding = DataBindingUtil.inflate(inflater, getAnnotatedLayout(), container, false)
        binding.setLifecycleOwner { lifecycle }
        super.onCreateView(inflater, container, savedInstanceState)
        onViewCreated()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        internalBinding = null
    }
}