package au.com.codeka.podcreep.ui

/**
 * Options that [Screen] knows about itself.
 */
data class ScreenOptions (
    /**
     * Whether the action bar should be enabled while on this screen. Default is true. This is
     * ignored if the screen is not the root (because we need to show the up button).
     */
    val enableActionBar: Boolean = true,

    /**
     * Whether the screen expects to be a root screen. If it's a root screen, then pushing this
     * screen onto the stack will clear all other screens.
     */
    val isRootScreen: Boolean = false
)
