package me.proton.android.core.domain.entity

import me.proton.android.core.domain.entity.Keys

/**
 * Created by dinokadrikj on 4/14/20.
 *
 * This is the base [User] entity in the system. It should be open so that every client/product can
 * extend it and provide client-specific data and fields.
 */
// TODO: complete it
open class User(
    val username: String,
    val keys: List<Keys>? = null
)