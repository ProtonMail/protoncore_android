package me.proton.core.util.android.sharedpreferences

import android.content.SharedPreferences

/**
 * Entity that can provide [SharedPreferences]
 * Use this interface to be able to call Preferences's delegates
 * I.E. >
 class MyClass : PreferencesProvider {
    override val preferences = ...

    val someIntFromPrefs by int()
 }
 *
 * @author Davide Farella
 */
interface PreferencesProvider :
    SharedPreferencesDelegationExtensions
