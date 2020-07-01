@file:Suppress("EXPERIMENTAL_FEATURE_WARNING") // Inline class

package ch.protonmail.libs.auth.api

/**
 * Objects of this class can be attached to OkHttp's requests and be read by Interceptors
 * @param username username which we want to authorize request for, remove auth if null
 */
internal inline class AuthTag(val username: String?)
