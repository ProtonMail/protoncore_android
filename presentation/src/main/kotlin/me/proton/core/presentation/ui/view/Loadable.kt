package me.proton.core.presentation.ui.view

/**
 * Provides interface function to signalize the loading is complete and the class implementing this interface should
 * do some action on this event.
 * Used for Custom Views that support 'loading' state within them.
 * Currently it is used for [ProtonButton].
 * @author Dino Kadrikj.
 */
interface Loadable {

    fun loadingComplete()
}
