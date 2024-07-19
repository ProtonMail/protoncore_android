## Proton Rule: Streamlined Test Setup for Android

**Proton Rule** is an Android Kotlin library designed to simplify and organize test setup in your Android tests, focusing on common use cases for activity and Compose UI testing.

**Key Features:**

* **Reduced Boilerplate:** Streamline common tasks like Activity/Compose test setup, test data injection, and environment configuration.
* **Improved Organization:** Centralize configuration for cleaner and more maintainable tests.
* **Customization:** Define custom annotations and rules for specific test requirements.

**Basic Usage:**

The `protonRule` function provides a central entry point for common test setups:

```kotlin
@HiltAndroidTest
class TestClass {

    private val testSubscriptionData = TestSubscriptionData(plan = Plan.Unlimited).annotationTestData

    @get:Rule
    val rule: ProtonRule = protonRule(
        testSubscriptionData,
        envConfig = EnvironmentConfig(host = "proton.black"),
        logoutBefore = true,
        logoutAfter = true,
        activityRule = activityScenarioRule<MainActivity>(),
        setUp = { }
    )

    @Test
    @PrepareUser
    fun defaultExample() {
        // MainActivity is started with a seeded user. Application uses proton.black host.
    }

    @Test
    @EnvironmentConfig(host = "curie.proton.black")
    @PrepareUser(userData = TestUserData(name = "name", password = "password"), loginBefore = true)
    fun overrideExample0() {
        // MainActivity is started with a user seeded with given name and password, authenticated on 'curie.proton.black' environment.
    }

    @Test
    @PrepareUser(loginBefore = true)
    fun overrideExample1() {
        // MainActivity is started with a seeded user with MailPlus plan logged in on 'proton.black' environment. 
        val mainTestUser = rule.testDataRule.mainTestUser
    }

    @Test
    @PrepareUser(userData = TestUserData(name = "name", password = "password"), loginBefore = true)
    fun overrideExample2() {
        // MainActivity is started with authenticated exiting user credentials. 
        val mainTestUser = rule.testDataRule.mainTestUser
    }

    @Test
    @PrepareUser(userData = TestUserData(email = "email@proton.black", password = "password"), loginBefore = true)
    fun overrideExample3() {
        // MainActivity is started with authenticated exiting user credentials. 
        val mainTestUser = rule.testDataRule.mainTestUser
    }
    
    @Test
    @PrepareUser(
        withTag = "mailPlusUser",
        userData = TestUserData(genKeys = TestUserData.GenKeys.Curve25519),
        subscriptionData = TestSubscriptionData(plan = Plan.MailPlus),
        loginBefore = true
    )
    fun overrideExample4() {
        // MainActivity is started with a seeded user with MailPlus plan logged in on 'proton.black' environment. 
        // User is tagged with 'mailPlusUser' tag and can be accessed in tests as shown below.
        val mainPlusUser = rule.testDataRule.seededUsers["mainPlusUser"]
    }

    @Test
    @PrepareUser(withTag = "sender", loginBefore = true)
    @PrepareUser(withTag = "recipient")
    fun overrideExample5() {
        // MainActivity is started with two seeded users where one of them with tag 'sender' is authenticated.
        // Second user with tag 'recipient' is seeded on selected environment and can be used in tests. 
        // By default, annotation without given tag, like @PrepareUser would seed user with 'main' tag.
        // Tags should be unique per test otherwise exception will be thrown.
        val sender = rule.testDataRule.seededUsers["sender"]
        val recipient = rule.testDataRule.seededUsers["recipient"]
    }
}
```

**3. Helper Functions:**

* `protonActivityScenarioRule<A>()`: Sets up an `ActivityScenarioRule` for a specific activity type with optional setup logic.
* `protonAndroidComposeRule<A>()`: Sets up a `ComposeTestRule` for a Compose activity with optional setup logic.

These functions are useful for most common case scenarios:
```kotlin
@HiltAndroidTest
class TestClass {

    // If using Views
    @get:Rule
    val protonRule: ProtonRule = protonActivityScenarioRule<MainActivity>(
        val customTestData = TestFeatureEnabled(false).annotationTestData
    )

    // If using Compose
    @get:Rule
    val protonRule: ProtonRule = protonAndroidComposeRule<MainActivity>(
        val customTestData = TestFeatureEnabled(false).annotationTestData
    )

    @Test
    @EnvironmentConfig(host = "scientist.proton.black")
    fun example() {
        // MainActivity is started with following configuration:
        // 1. Default environment configuration provided by the application
        // 2. Hilt test dependencies injected 
        // 3. All users are logged out before and after the test (to always assure clean state)
        // 4. Default environment configuration is overriden by annotation
        // 5. Test feature is disabled by default, but can be overriden by annotation @TestFeatureEnabled
    }
}
```

**4. Custom Annotations:**

Define custom annotations to capture test-specific configurations:

```kotlin
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class TestFeatureEnabled(val enabled: Boolean = true)

public val TestFeatureEnabled.annotationTestData: AnnotationTestData<TestFeatureEnabled>
    get() = AnnotationTestData(
        this,
        implementation = { data ->
            // Enable/disable the feature based on data.enabled
        },
        tearDown = { _ ->
            // Reset feature state to default
        }
    )
```

**5. Using Annotations with Rules:**

Combine annotations and rules to tailor test setups:

```kotlin
val customTestData = TestFeatureEnabled(false).annotationTestData, // Disable specific feature
val otherTestData = OtherTestData().annotationTestData

@Rule
override val protonRule: ProtonRule = protonActivityScenarioRule<MainActivity>(
    customData, otherTestData, // provided using 'vararg'
    logoutAfter = false
)

@Test
fun example() {
    assertFalse(customTestData.enabled)
}

@Test
@TestFeatureEnabled(enabled = true)
fun exampleOverride() {
    assertTrue(customTestData.enabled)
}
```

**Advanced Usage:**

For advanced setup you can use underlying test rules directly and/or combine them into your own rules:
```kotlin
class AdvancedTestSetup {

    private val environmentConfig = EnvironmentConfig(host = "proton.black")

    private val userConfig = ProtonRule.UserConfig(
        logoutBefore = true,
        logoutAfter = true
    )

    private val testConfig = ProtonRule.TestConfig(
        envConfig = environmentConfig,
        annotationTestData = arrayOf(TestSubscriptionData(plan = Plan.Unlimited).annotationTestData),
        activityRule = activityScenarioRule<MainActivity>()
    )

    @get:Rule
    val envConfigRule = EnvironmentConfigRule(
        defaultConfig = environmentConfig
    )

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val testDataRule = QuarkTestDataRule(
        annotationTestData = testConfig.annotationTestData,
        environmentConfig = { envConfigRule.config }
    )
    
    @get:Rule
    val setupRule: ExternalResource = before {
        // Here code will be executed to set up an external resource before a test (a file, socket, 
        // server, database connection, etc.), and guarantee to tear it down afterward.
        // See JUnit ExternalResource class for more details.
    }

    @get:Rule
    val activityRule = testConfig.activityRule

    @Test
    @PrepareUser(subscriptionData = TestSubscriptionData(plan = Plan.MailPlus), loginBefore = true)
    fun example() {
        // Test will run under the same conditions as using protonActivityScenarioRule().
    }
}
```
