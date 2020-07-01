package ch.protonmail.libs.lint.detectors

import ch.protonmail.libs.lint.detectors.testUtils.LintTest
import org.junit.Test

/**
 * Test suite for [HardcodedCoroutineDispatcherDetector]
 * @author Davide Farella
 */
internal class HardcodedCoroutineDispatcherDetectorTest : LintTest(HardcodedCoroutineDispatcherDetector) {

    @Test fun `NO warn if dispatcher is injected in the constructor`() {
        """
        |import kotlinx.coroutines.CoroutineDispatcher
        |import kotlinx.coroutines.CoroutineScope
        |import kotlinx.coroutines.Dispatchers.IO
        |import kotlinx.coroutines.Job
        |import kotlinx.coroutines.launch
        |
        |class TestClass(val ioDispatcher: CoroutineDispatcher) {
        |   val scope: CoroutineScope = CoroutineScope(Job())
        |   
        |   fun main() {
        |       scope.launch(ioDispatcher) { }
        |   }
        |}
        |
        |fun main() {
        |   TestClass(IO)
        |}
        |"""
            .setupTest()
            .run()
            .expectClean()
    }

    @Test fun `WARN if dispatcher is hardcoded by short import`() {
        """
        |import kotlinx.coroutines.CoroutineDispatcher
        |import kotlinx.coroutines.CoroutineScope
        |import kotlinx.coroutines.Dispatchers.IO
        |import kotlinx.coroutines.Job
        |import kotlinx.coroutines.launch
        |
        |class TestClass {
        |   val scope: CoroutineScope = CoroutineScope(Job())
        |   
        |   fun main() {
        |       scope.launch(IO) { }
        |   }
        |}
        |"""
            .setupTest()
            .run()
            .expectMatches("Inject the Dispatcher in the constructor instead")
    }

    @Test fun `WARN if dispatcher is hardcoded by long import`() {
        """
        |import kotlinx.coroutines.CoroutineDispatcher
        |import kotlinx.coroutines.CoroutineScope
        |import kotlinx.coroutines.Dispatchers
        |import kotlinx.coroutines.Job
        |import kotlinx.coroutines.launch
        |
        |class TestClass {
        |   val scope: CoroutineScope = CoroutineScope(Job())
        |   
        |   fun main() {
        |       scope.launch(Dispatchers.IO) { }
        |   }
        |}
        |"""
            .setupTest()
            .run()
            .expectMatches("Inject the Dispatcher in the constructor instead")
    }

    @Test fun `WARN if dispatcher is hardcoded by full qualifier`() {
        """
        |import kotlinx.coroutines.CoroutineDispatcher
        |import kotlinx.coroutines.CoroutineScope
        |import kotlinx.coroutines.Job
        |import kotlinx.coroutines.launch
        |
        |class TestClass {
        |   val scope: CoroutineScope = CoroutineScope(Job())
        |   
        |   fun main() {
        |       scope.launch(kotlinx.coroutines.Dispatchers.IO) { }
        |   }
        |}
        |"""
            .setupTest()
            .run()
            .expectMatches("Inject the Dispatcher in the constructor instead")
    }
}
