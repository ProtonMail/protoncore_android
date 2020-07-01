package me.proton.core.util.testutil

/**
 * For some reason some tests ( only one at the time this function is created ) can fail JDK different from revision 242.
 * Test of this kind run correctly hitting the run / debug button with any Java version set system-wide
 *
 * TODO: inspect for find the reason
 *
 * @author Davide Farella
 */
fun `run only on Java 1_8-242`(block: () -> Unit) {
    if (System.getProperty("java.version") == "1.8.0_242-release") block()
}
