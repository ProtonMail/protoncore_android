package me.proton.core.test.rule.annotation

import me.proton.core.test.rule.annotation.payments.TestSubscriptionData
import me.proton.core.test.rule.extension.seedTestUserData


@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
public annotation class PrepareUser(
    // Default user tag is "main". Exception will be thrown in case of identical tags.
    val withTag: String = "main",
    val userData: TestUserData = TestUserData(),
    val subscriptionData: TestSubscriptionData = TestSubscriptionData(),
    val loginBefore: Boolean = false
)

public val PrepareUser.annotationTestData: AnnotationTestData<PrepareUser>
    get() = AnnotationTestData.forTestUserData(
        default = this,
        implementation = { _: PrepareUser, userData: TestUserData ->
            seedTestUserData(userData)
        }
    )

public const val MAIN_USER_TAG: String = "main"