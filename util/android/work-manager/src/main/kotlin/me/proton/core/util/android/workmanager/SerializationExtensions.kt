@file:Suppress("unused")

package me.proton.core.util.android.workmanager

import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.workDataOf
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.SerializationStrategy
import me.proton.core.util.kotlin.deserialize
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
    deserializer: DeserializationStrategy<T>?
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
): T = getString(SERIALIZED_DATA_KEY)!!.deserialize(deserializer)

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

/** @return [ListenableWorker.Result.Success] with a data */
inline fun <reified T : Any> ListenableWorker.success(data: T): ListenableWorker.Result.Success =
    ListenableWorker.Result.success(workDataOf(SERIALIZED_DATA_KEY to data.serialize()))
        as ListenableWorker.Result.Success
