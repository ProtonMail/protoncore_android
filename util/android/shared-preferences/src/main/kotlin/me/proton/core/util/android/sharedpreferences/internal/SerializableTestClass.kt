package me.proton.core.util.android.sharedpreferences.internal

import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
internal data class SerializableTestClass(
    val n: String,
    val c: SerializableTestChild
)

@Serializable
internal data class SerializableTestChild(
    val a: Int,
    val b: Boolean
)
