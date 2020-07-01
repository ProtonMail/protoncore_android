package me.proton.android.core.data.api

import ch.protonmail.libs.core.Invokable

/**
 * Configurations for the library
 * Implements [Invokable]
 */
object ProtonAuthConfig : Invokable {

    /** This must be set on the Application creation and is the secret for the specific client */
    lateinit var clientSecret: String
}
