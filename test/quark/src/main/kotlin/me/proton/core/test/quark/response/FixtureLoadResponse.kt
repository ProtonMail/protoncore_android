package me.proton.core.test.quark.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class FixtureLoadResponse(
    val users: List<User>,
    val stats: Stats
)

@Serializable
public data class User(
    @SerialName("ID")
    val id: UserId,
    val name: String,
    val password: String,
    val organizationID: String? = null,
    val memberID: String? = null,
    val members: List<User> = emptyList(),
    val keys: List<UserKey> = emptyList()
)

@Serializable
public data class UserId(
    val encrypted: String,
    val raw: Long
)

@Serializable
public data class UserKey(
    val public: String,
    val private: String
)

@Serializable
public data class Stats(
    @SerialName("durations_ms")
    val durationMs: Double,

    @SerialName("memory_bytes")
    val memoryBytes: Long
)
