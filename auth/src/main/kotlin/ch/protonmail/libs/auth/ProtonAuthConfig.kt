package ch.protonmail.libs.auth

import ch.protonmail.libs.core.Invokable

/**
 * Configurations for the library
 * Implements [Invokable]
 */
object ProtonAuthConfig : Invokable {

    /** This must be set on the Application creation and is the secret for the specific client */
    lateinit var clientSecret: String
}
