/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.test.android.uitests.tests.medium.plans

import android.content.Context
import androidx.core.text.HtmlCompat
import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.android.core.coreexample.MainActivity
import me.proton.core.domain.entity.AppStore
import me.proton.core.plan.presentation.R
import me.proton.core.plan.test.robot.SubscriptionRobot
import me.proton.core.test.android.robot.CoreexampleRobot
import me.proton.core.test.rule.annotation.PrepareUser
import me.proton.core.test.rule.annotation.payments.TestPaymentMethods
import me.proton.core.test.rule.annotation.payments.annotationTestData
import me.proton.core.test.rule.extension.protonActivityScenarioRule
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

@Ignore("Outdated")
@HiltAndroidTest
class DynamicCurrentPlanTests {

    @get:Rule
    val protonRule = protonActivityScenarioRule<MainActivity>(
        setOf(
            TestPaymentMethods(
                AppStore.GooglePlay,
                card = true,
                paypal = false,
                inApp = false
            ).annotationTestData
        )
    )

    @Ignore("Quark command does not support cycle")
    @Test
    @PrepareUser(loginBefore = true)
    @TestPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = true)
    fun userWithPaidPlanCardAndIAPPayment1month() {
        CoreexampleRobot().plansCurrent()
        val (cycleText, cycleTime) = getCycleData(1)

        SubscriptionRobot.apply {
            verifyPlanRenewalDisplayed(cycleTime)
            verifyPlanCycleDisplayed(cycleText)
        }
    }

    @Ignore("Quark command does not support cycle")
    @Test
    @PrepareUser(loginBefore = true)
    @TestPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = true)
    fun userWithPaidPlanCardAndIAPPayment12months() {
        CoreexampleRobot().plansCurrent()
        val (cycleText, cycleTime) = getCycleData(12)

        SubscriptionRobot.apply {
            SubscriptionRobot.apply {
                verifyPlanRenewalDisplayed(cycleTime)
                verifyPlanCycleDisplayed(cycleText)
            }
        }
    }

    @Ignore("Quark command does not support cycle")
    @Test
    @PrepareUser(loginBefore = true)
    @TestPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = true)
    fun userWithPaidPlanCardAndIAPPayment15months() {
        CoreexampleRobot().plansCurrent()
        val (cycleText, cycleTime) = getCycleData(15)

        SubscriptionRobot.apply {
            verifyPlanRenewalDisplayed(cycleTime)
            verifyPlanCycleDisplayed(cycleText)
        }
    }

    @Ignore("Quark command does not support cycle")
    @Test
    @PrepareUser(loginBefore = true)
    @TestPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = true)
    fun userWithPaidPlanCardAndIAPPayment30months() {
        CoreexampleRobot().plansCurrent()
        val (cycleText, cycleTime) = getCycleData(30)

        SubscriptionRobot.apply {
            verifyPlanRenewalDisplayed(cycleTime)
            verifyPlanCycleDisplayed(cycleText)
        }
    }

    private fun getCycleData(months: Int): Pair<String, String> {
        val context = protonRule.targetContext
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.MONTH, months)
        }

        return context.resources.getQuantityString(
            R.plurals.plans_billing_other_period,
            months,
            months
        ) to context.calendarToDateFormat(calendar.time).toString()
    }

    private fun Context.calendarToDateFormat(date: Date) = HtmlCompat.fromHtml(
        String.format(
            getString(R.string.plans_renewal_date),
            DateFormat.getDateInstance().format(date)
        ),
        HtmlCompat.FROM_HTML_MODE_LEGACY
    )
}
