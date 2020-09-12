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

@file:Suppress("unused")

package me.proton.core.util.android.workmanager

import androidx.annotation.VisibleForTesting
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.workDataOf
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import me.proton.core.util.kotlin.deserializeOrNull
import me.proton.core.util.kotlin.serialize

/*
 * Set of extensions for serialize / deserialize Work's Data
 * Author: Davide Farella
 */

/** Being [Data] a set of Pairs, this is the key for the entry of our serialized objects */
@PublishedApi // needed for inline
internal const val SERIALIZED_DATA_KEY = "serialized_data_key"

/**
 * @return [T] deserialized from receiver [WorkInfo.mOutputData]
 *
 * @param T must be a class annotate with [Serializable]
 * @param deserializer optional [DeserializationStrategy] of [T], if no value is passed, the
 *   [ImplicitReflectionSerializer] will be used
 *
 * @throws KotlinNullPointerException if no serialized data in present in [Data]
 */
inline fun <reified T : Any> WorkInfo.outputData(
    deserializer: DeserializationStrategy<T>? = null
): T = outputData.deserialize(deserializer)

/**
 * @return [T] deserialized from receiver [Data]
 *
 * @param T must be a class annotate with [Serializable]
 * @param deserializer optional [DeserializationStrategy] of [T], if no value is passed, the
 *   [ImplicitReflectionSerializer] will be used
 *
 * @throws KotlinNullPointerException if no serialized data in present in [Data]
 */
inline fun <reified T : Any> Data.deserialize(
    deserializer: DeserializationStrategy<T>? = null
): T {
    val serialized = checkNotNull(getString(SERIALIZED_DATA_KEY)) {
        "No serializable data found for this Data model"
    }
    return checkNotNull(serialized.deserializeOrNull(deserializer)) {
        "Serializable data is found for this Data model, but cannot be deserialized for the requested type"
    }
}

/**
 * @return [Data] created by serializing receiver [T]
 *
 * @param T must be a class annotate with [Serializable]
 * @param serializer optional [SerializationStrategy] of [T], if no value is passed, the
 *   [ImplicitReflectionSerializer] will be used
 */
inline fun <reified T : Any> T.toWorkData(
    serializer: SerializationStrategy<T>? = null
): Data = workDataOf(SERIALIZED_DATA_KEY to serialize(serializer))

/**
 * @return [T] deserialized from [ListenableWorker.getInputData]
 *
 * @param T must be a class annotate with [Serializable]
 * @param deserializer optional [DeserializationStrategy] of [T], if no value is passed, the
 *   [ImplicitReflectionSerializer] will be used
 *
 * @throws KotlinNullPointerException if no serialized data in present in [Data]
 */
inline fun <reified T : Any> ListenableWorker.input(
    deserializer: DeserializationStrategy<T>? = null
) = inputData.deserialize(deserializer)

/** @return [ListenableWorker.Result.Success] with a data */
inline fun <reified T : Any> ListenableWorker.success(data: T): ListenableWorker.Result.Success =
    ListenableWorker.Result.success(workDataOf(SERIALIZED_DATA_KEY to data.serialize()))
        as ListenableWorker.Result.Success


// Test purpose only, needs to stay in this source set in order to the Annotation to be processed
@Serializable
@VisibleForTesting
internal data class TestWorkInput(
    val name: String,
    val number: Int
)
