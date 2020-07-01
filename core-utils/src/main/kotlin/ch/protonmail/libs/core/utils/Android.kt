@file:Suppress("unused") // Public APIs

package ch.protonmail.libs.core.utils

import android.os.Build

/**
 * An object for check Android version and other generic utilities
 * @author Davide Farella
 */
object Android {

    /** @return `true` if the current SDK if equals of greater that Android *JellyBean MR2* */
    val JELLYBEAN_MR2 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2

    /** @return `true` if the current SDK if equals of greater that Android *Lollipop* */
    val LOLLIPOP get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    /** @return `true` if the current SDK if equals of greater that Android *Marshmallow* */
    val MARSHMALLOW get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    /** @return `true` if the current SDK if equals of greater that Android *Oreo* */
    val OREO get() =  Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    /** @return `true` if the current SDK if equals of greater that Android *Pie* */
    val PIE get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
}
