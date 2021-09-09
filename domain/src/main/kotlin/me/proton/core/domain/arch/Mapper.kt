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

package me.proton.core.domain.arch

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.util.kotlin.Invokable

/**
 * Base block for map Model across different layers, e.g. from Business model to Ui model.
 * It serves as provide extensions function for transformation, specifically for:
 * * a simple model
 * * a collection of models
 * * a flow of models
 *
 * @param In the source model to be mapped
 * @param Out the result model
 *
 * Implements [Invokable]
 */
interface Mapper<in In, out Out> : Invokable

/**
 * Enable to execute a `map` operation on the [Iterable] receiver, passing the [Mapper] as argument.
 * Example: `` myBusinessModelList.map(myUiModelMapper) { it.toUiModel() } ``
 *
 * @param M type of the [Mapper]
 * @param In source model
 * @param Out result model
 *
 * @return [List] of [Out]
 */
fun <M : Mapper<In, Out>, In, Out> Iterable<In>.map(mapper: M, f: M.(In) -> Out): List<Out> =
    map { mapper.f(it) }

/**
 * Enable to execute a `map` operation on the [Flow] receiver, passing the [Mapper] as argument.
 * Example: `` myBusinessModelFlow.map(myUiModelMapper) { it.toUiModel() } ``
 *
 * @param M type of the [Mapper]
 * @param In source model
 * @param Out result model
 *
 * @return [Flow] of [Out]
 */
fun <M : Mapper<In, Out>, In, Out> Flow<In>.map(mapper: M, f: M.(In) -> Out): Flow<Out> =
    map { mapper.f(it) }
