package ch.protonmail.libs.lint.detectors

import ch.protonmail.libs.lint.detectors.testUtils.LintTest
import org.junit.Test

/**
 * Test suite for [NotConstantStringDetector]
 * @author Davide Farella
 */
internal class NotConstantStringDetectorTest : LintTest(NotConstantStringDetector) {

    @Test fun `constant usage for empty string local variable`() {
        """
        |class TestClass {
        |   fun main() {
        |       val a = EMPTY_STRING
        |       println(a)
        |   }
        |}
        |"""
            .setupTest()
            .run()
            .expectClean()
    }

    @Test fun `NO constant usage for empty string local variable`() {
        """
        |class TestClass {
        |   fun main() {
        |       val a = ""
        |       println(a)
        |   }
        |}
        |"""
            .setupTest()
            .run()
            .expectMatches("Use 'EMPTY_STRING' constant instead")
    }

    @Test fun `NO constant usage for empty string global variable`() {
        """
        |val a = ""
        |"""
            .setupTest()
            .run()
            .expectMatches("Use 'EMPTY_STRING' constant instead")
    }

    @Test fun `NO constant usage for empty string const`() {
        """
        |const val A = ""
        |"""
            .setupTest()
            .run()
            .expectMatches("Use 'EMPTY_STRING' constant instead")
    }
}
