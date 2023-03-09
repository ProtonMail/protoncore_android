package me.proton.core.humanverification.test.robot

import me.proton.test.fusion.Fusion

/** Corresponds to HV Code WebView. */
public object HvCodeRobot {

    public fun fillCode(): HvCodeRobot = apply {
        // Workaround to input HV code.
        Thread.sleep(6000)
        repeat(7) { Fusion.device.pressKeyCode(13) }
    }

    public fun clickVerify() {
        Fusion.device.pressEnter()
    }
}
