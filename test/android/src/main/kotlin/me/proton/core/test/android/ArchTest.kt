package me.proton.core.test.android

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule

/**
 * An interface meant to be implemented by a Test Suite that uses `androidx.arch` components.
 * It applies [InstantTaskExecutorRule]
 * Example:
```
class MyClassTest : ArchTest {
    // test cases
}
```
 *
 * @author Davide Farella
 */
interface ArchTest {

    @get:Rule
    val archRule get() = InstantTaskExecutorRule()
}
