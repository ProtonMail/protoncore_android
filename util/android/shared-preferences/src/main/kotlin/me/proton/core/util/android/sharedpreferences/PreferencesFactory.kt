package me.proton.core.util.android.sharedpreferences

import android.content.SharedPreferences
import me.proton.core.util.kotlin.EMPTY_STRING

/**
 * Lambda that returns [SharedPreferences]
 *
 * A lambda has been chosen over an interface for avoid to implement it for simple use cases
 * E.G. `fun someFunction(factory: PreferencesFactory) { ... }`
 * could be used in the following way `someFunction { mySharedPreferences }`
 * While a more complex use case could be
 * ```
class MultiUserPreferencesFactory(username: String) : PreferencesFactory {
    override fun invoke(): SharedPreferences {
        getPreferencesForUsername(username)
    }
}
someFunction(myMultiUserPreferencesFactory)
 * ```
 *
 *
 * @author Davide Farella
 */
typealias PreferencesFactory = () -> SharedPreferences

/** [PreferencesFactory] for create [SharedPreferences] with params */
abstract class ParametrizedPreferencesFactory<P : ParametrizedPreferencesFactory.Params>(
    protected open var params: P
) : PreferencesFactory {
    interface Params
}

/**
 * A SharedPreference Factory that generate [SharedPreferences] regarding a given Username
 * @see UsernameParam
 * Inherit from [ParametrizedPreferencesFactory]
 *
 * @constructor `x` param is used only for avoid signature clash between constructor because of
 * the usage of `inline class` [UsernameParam] in the secondary constructor ( which is compiled by
 * using its underlying [String] type )
 */
abstract class UsernamePreferencesFactory private constructor(
    usernameParam: UsernameParam,
    @Suppress("UNUSED_PARAMETER") x: Byte
) : ParametrizedPreferencesFactory<UsernameParam>(usernameParam) {

    /** Get and change username for current Factory */
    var username
        get() = params.s
        set(value) {
            params = UsernameParam(value)
        }

    /** @constructor for easily implementing this abstract class by [username] */
    constructor(username: String = EMPTY_STRING) : this(UsernameParam(username), 0)
}

/** [ParametrizedPreferencesFactory.Params] for [UsernamePreferencesFactory] */
@JvmInline
value class UsernameParam(val s: String) : ParametrizedPreferencesFactory.Params
