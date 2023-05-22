package me.proton.core.accountmanager.data

import me.proton.core.util.kotlin.LoggerLogTag

object LogTag {
    /** Tag for Invalid User Key. */
    const val INVALID_USER_KEY = "core.accountmanager.invalid.user.key"

    /** Tag for Invalid UserAddress Key. */
    const val INVALID_USER_ADDRESS_KEY = "core.accountmanager.invalid.useraddress.key"

    /** Tag for session creation. */
    val SESSION_CREATE = LoggerLogTag("core.accountmanager.session.create")

    /** Tag for session refresh. */
    val SESSION_REFRESH = LoggerLogTag("core.accountmanager.session.refresh")

    /** Tag for session request (unauthenticated). */
    val SESSION_REQUEST = LoggerLogTag("core.accountmanager.session.request")

    /** Tag for session force logout. */
    val SESSION_FORCE_LOGOUT = LoggerLogTag("core.accountmanager.session.forcelogout")

    /** Tag for session scopes. */
    val SESSION_SCOPES = LoggerLogTag("core.accountmanager.session.scopes")

    /** Default tag for any other issue we need to log */
    const val DEFAULT = "core.accountmanager.default"
}