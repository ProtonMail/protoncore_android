package ch.protonmail.libs.lint.detectors

import ch.protonmail.libs.lint.detectors.testUtils.LintTest
import org.junit.Test

/**
 * Test suite for [NoSerialNameAnnotationDetector]
 * @author Davide Farella
 */
class NoSerialNameAnnotationDetectorTest : LintTest(NoSerialNameAnnotationDetector) {

    @Test
    fun `NO Serializable should NOT warn`() {
        """
        |data class TestClass(
        |   val someVariable: Int
        |)
        |"""
            .setupTest()
            .run()
            .expectClean()
    }

    @Test
    fun `Serializable with NO SerialName should warn`() {
        """
        |import kotlinx.serialization.Serializable
        |
        |@Serializable
        |data class TestClass(
        |   val someVariable: Int
        |)
        |"""
            .setupTest()
            .run()
            .expectMatches("Missing 'SerialName' annotation")
    }

    @Test
    fun `Serializable with SerialName should NOT warn`() {
        """
        |import kotlinx.serialization.Serializable
        |import kotlinx.serialization.SerialName
        |
        |@Serializable
        |data class TestClass(
        |   @SerialName("someSerialName")
        |   val someVariable: Int
        |)
        |"""
            .setupTest()
            .run()
            .expectClean()
    }
}
