/*
 * Copyright (c) 2023 Proton AG
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
import androidx.test.core.app.ApplicationProvider
import me.proton.core.domain.entity.AppStore
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.entity.PlanCycle
import me.proton.core.test.quark.data.Plan
import me.proton.core.test.quark.data.User
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.android.uitests.robot.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

class CurrentPlanTests : BaseTest() {
    private fun navigateUserToCurrentPlans(user: User): SelectPlanRobot {
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = false)
        login(user)

        return CoreexampleRobot()
            .plansCurrent()
    }

    @Before
    override fun setUp() {
        super.setUp()
        quark.jailUnban()
    }

    @After
    fun setDefaults() {
        quark.setDefaultPaymentMethods()
    }

    @Test
    fun userWithFreePlan() {
        val user = quark.userCreate().first
        navigateUserToCurrentPlans(user)
            .scrollToPlan(Plan.Dev)
            .toggleExpandPlan(Plan.Dev)
            .verify {
                planDetailsDisplayedInsideRecyclerView(Plan.Dev)
                canUpgradeToPlan(Plan.Dev)
            }
    }

    @Test
    fun userWithPaidPlan() {
        val paidUser = User(plan = Plan.Unlimited)
        quark.seedNewSubscriberWithCycle(paidUser, PlanCycle.YEARLY.cycleDurationMonths)
        navigateUserToCurrentPlans(paidUser)
            .verify {
                planDetailsDisplayed(paidUser.plan)
            }
    }

    @Test
    fun userWithPaidPlanCardAndIAPPayment1month() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = true)
        val paidUserCycle1 = User(plan = Plan.MailPlus)
        val cycle1 = PlanCycle.OTHER.apply {
            cycleDurationMonths = 1
        }
        val user = quark.seedNewSubscriberWithCycle(paidUserCycle1, cycle1.cycleDurationMonths)
        login(user)

        CoreexampleRobot()
            .plansCurrent()
            .verify {
                val context = ApplicationProvider.getApplicationContext<Context>()
                val cycleText = context.resources.getQuantityString(
                    R.plurals.plans_billing_other_period,
                    cycle1.cycleDurationMonths,
                    cycle1.cycleDurationMonths
                )
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.MONTH, 1)

                planRenewalDisplayed(context.calendarToDateFormat(calendar.time).toString())
                planCycleDisplayed(cycleText)
            }
    }

    @Test
    fun userWithPaidPlanCardAndIAPPayment12months() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = true)
        val paidUserCycle12 = User(plan = Plan.MailPlus)
        val cycle12 = PlanCycle.OTHER.apply {
            cycleDurationMonths = 12
        }
        val user = quark.seedNewSubscriberWithCycle(paidUserCycle12, cycle12.cycleDurationMonths)
        login(user)

        CoreexampleRobot()
            .plansCurrent()
            .verify {
                val context = ApplicationProvider.getApplicationContext<Context>()
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.YEAR, 1)

                planRenewalDisplayed(context.calendarToDateFormat(calendar.time).toString())
                planCycleDisplayed(context.getString(R.string.plans_billing_yearly))
            }
    }

    @Test
    fun userWithPaidPlanCardAndIAPPayment15months() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = true)
        val paidUserCycle15 = User(plan = Plan.VpnPlus)
        val cycle15 = PlanCycle.OTHER.apply {
            cycleDurationMonths = 15
        }
        val user = quark.seedNewSubscriberWithCycle(paidUserCycle15, cycle15.cycleDurationMonths)
        login(user)

        CoreexampleRobot()
            .plansCurrent()
            .verify {
                val context = ApplicationProvider.getApplicationContext<Context>()
                val cycleText = context.resources.getQuantityString(
                    R.plurals.plans_billing_other_period,
                    cycle15.cycleDurationMonths,
                    cycle15.cycleDurationMonths
                )
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.MONTH, 15)

                planRenewalDisplayed(context.calendarToDateFormat(calendar.time).toString())
                planCycleDisplayed(cycleText)
            }
    }

    @Test
    fun userWithPaidPlanCardAndIAPPayment30months() {
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = true)
        val paidUserCycle30 = User(plan = Plan.VpnPlus)
        val cycle30 = PlanCycle.OTHER.apply {
            cycleDurationMonths = 30
        }
        val user = quark.seedNewSubscriberWithCycle(paidUserCycle30, cycle30.cycleDurationMonths)
        login(user)

        CoreexampleRobot()
            .plansCurrent()
            .verify {
                val context = ApplicationProvider.getApplicationContext<Context>()
                val cycleText = context.resources.getQuantityString(
                    R.plurals.plans_billing_other_period,
                    cycle30.cycleDurationMonths,
                    cycle30.cycleDurationMonths
                )
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.MONTH, 30)

                planRenewalDisplayed(context.calendarToDateFormat(calendar.time).toString())
                planCycleDisplayed(cycleText)
            }
    }

    private fun Context.calendarToDateFormat(date: Date) = HtmlCompat.fromHtml(
        String.format(
            getString(R.string.plans_renewal_date),
            DateFormat.getDateInstance().format(date)
        ),
        HtmlCompat.FROM_HTML_MODE_LEGACY
    )
}
