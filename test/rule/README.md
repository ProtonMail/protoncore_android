## Proton Rule: Streamlined Test Setup for Android

**Proton Rule** is an Android Kotlin library designed to simplify and organize test setup in your Android tests, focusing on common use cases for activity and Compose UI testing.

**Key Features:**

* **Reduced Boilerplate:** Streamline common tasks like activity/Compose test setup, test data injection, and environment configuration.
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
        userData = TestUserData(name = randomUsername()),
        loginBefore = true,
        logoutBefore = true,
        logoutAfter = true,
        activityRule = activityScenarioRule<MainActivity>(),
        setUp = { }
    )

    @Test
    fun defaultExample() {
        // MainActivity is started with a seeded user with Unlimited plan logged in on 'proton.black' environment.
    }

    @Test
    @EnvironmentConfig(host = "proton.me")
    @TestUserData(name = "pro", password = "pro", shouldSeed = false)
    fun overrideExample() {
        // MainActivity is started with a user 'pro' logged in on 'proton.me' environment. User will not be seeded beforehand
    }


    @Test
    @TestSubscriptionData(plan = Plan.MailPlus)
    fun overrideExample2() {
        // MainActivity is started with a seeded user with MailPlus plan logged in on 'proton.black' environment.
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
        // 3. Logged in with freshly created user with random username 
        //    Can be accessed by protonRule.testDataRule.testUserData
        // 4. All users are logged out before and after the test (to always assure clean state)
        // 5. Default environment configuration is overriden by annotation
        // 6. Test feature is disabled by default, but can be overriden by annotation @TestFeatureEnabled
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
    private val testUserData = TestUserData(
        name = randomUsername(),
        password = "password",
        genKeys = TestUserData.GenKeys.RSA4096
    )

    private val environmentConfig = EnvironmentConfig(host = "proton.black")

    private val userConfig = ProtonRule.UserConfig(
        userData = testUserData,
        loginBefore = true,
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
        initialTestUserData = userConfig.userData,
        environmentConfig = { envConfigRule.config }
    )

    @get:Rule
    val authenticationRule: AuthenticationRule = AuthenticationRule(
        userConfig = userConfig
    )

    @get:Rule
    val setupRule: ExternalResource = before {
        // setup
    }

    @get:Rule
    val activityRule = testConfig.activityRule

    @Test
    @TestSubscriptionData(plan = Plan.Unlimited)
    fun example() {
        // Test will run under the same conditions as using protonActivityScenarioRule()
    }
}
```
