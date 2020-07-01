package me.proton.core.util.android.sharedpreferences

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("unused") // Used for restrict the function's scope
@PublishedApi // Required for inline
internal fun <T : Any> SharedPreferencesDelegationExtensions.required(
    explicitKey: String?,
    getter: SharedPreferences.(key: String) -> T,
    setter: SharedPreferences.(key: String, T) -> Unit
): ReadWriteProperty<Any?, T> =
    RequiredPreferenceProperty(
        { preferences },
        explicitKey,
        getter,
        setter
    )

@Suppress("unused") // Used for restrict the function's scope
@PublishedApi // Required for inline
internal fun <T : Any?> SharedPreferencesDelegationExtensions.optional(
    explicitKey: String?,
    getter: SharedPreferences.(key: String) -> T?,
    setter: SharedPreferences.(key: String, T) -> Unit
): ReadWriteProperty<Any?, T?> =
    OptionalPreferenceProperty(
        { preferences },
        explicitKey,
        getter,
        setter
    )

/**
 * [ReadWriteProperty] for apply delegation to `SharedPreferences`
 *
 * @param explicitKey OPTIONAL [String] that will be used as Preference's key, if `null` [KProperty.name]
 * will be used
 *
 * @param getter Lambda for get the required value [T]
 *
 * @param setter Lambda for set the required value [T]
 */
private abstract class PreferencesProperty<T>(
    getSharedPreferences: () -> SharedPreferences,
    private val explicitKey: String?,
    private val getter: SharedPreferences.(key: String) -> T,
    private val setter: SharedPreferences.(key: String, T) -> Unit
) : ReadWriteProperty<Any?, T> {
    private val sharedPreferences by lazy(getSharedPreferences)

    /**
     * Returns the value of the property for the given object.
     * @param thisRef the object for which the value is requested.
     * @param property the metadata for the property.
     * @return the property value.
     */
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return sharedPreferences.getter(explicitKey ?: property.name)
    }

    /**
     * Sets the value of the property for the given object.
     * @param thisRef the object for which the value is requested.
     * @param property the metadata for the property.
     * @param value the value to set.
     */
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        sharedPreferences.setter(explicitKey ?: property.name, value)
    }
}

/** [PreferencesProperty] of non nullable T */
private class RequiredPreferenceProperty<T : Any>(
    getSharedPreferences: () -> SharedPreferences,
    explicitKey: String?,
    getter: SharedPreferences.(key: String) -> T,
    setter: SharedPreferences.(key: String, T) -> Unit
) : PreferencesProperty<T>(
    getSharedPreferences = getSharedPreferences,
    explicitKey = explicitKey,
    getter = getter,
    setter = setter
)

/** [PreferencesProperty] of nullable T */
private class OptionalPreferenceProperty<T : Any?>(
    getSharedPreferences: () -> SharedPreferences,
    explicitKey: String?,
    getter: SharedPreferences.(key: String) -> T?,
    setter: SharedPreferences.(key: String, T) -> Unit
) : PreferencesProperty<T?>(
    getSharedPreferences = getSharedPreferences,
    explicitKey = explicitKey,
    getter = getter,
    setter = { k, v -> setOrRemove(k, v, setter) }
)

/**
 * Executes [block]
 * @throws IllegalArgumentException if [value] is `null`
 */
private fun <T> SharedPreferences.setOrRemove(
    key: String,
    value: T?,
    block: SharedPreferences.(key: String, value: T) -> Unit
) {
    if (value == null) edit { remove(key) }
    else block(key, value)
}
