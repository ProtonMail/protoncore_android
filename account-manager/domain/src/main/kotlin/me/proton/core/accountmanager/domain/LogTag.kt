package me.proton.core.accountmanager.domain

object LogTag {
    /** Tag for Invalid User Key. */
    const val INVALID_USER_KEY = "core.accountmanager.invalid.user.key"

    /** Tag for Invalid UserAddress Key. */
    const val INVALID_USER_ADDRESS_KEY = "core.accountmanager.invalid.useraddress.key"

    /** Tag for session creation. */
    const val SESSION_CREATE = "core.accountmanager.session.create"

    /** Tag for session refresh. */
    const val SESSION_REFRESH = "core.accountmanager.session.refresh"

    /** Tag for session request (unauthenticated). */
    const val SESSION_REQUEST = "core.accountmanager.session.request"

    /** Tag for session force logout. */
    const val SESSION_FORCE_LOGOUT = "core.accountmanager.session.forcelogout"

    /** Tag for session scopes. */
    const val SESSION_SCOPES = "core.accountmanager.session.scopes"

    /** Default tag for any other issue we need to log */
    const val DEFAULT = "core.accountmanager.default"
}