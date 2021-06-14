/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.test.android.robots.plans

import me.proton.core.plan.presentation.R
import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify

class SelectPlanRobot : CoreRobot() {

    inline fun <reified T> selectPlan(plan: User.Plan): T {
        view
            .withId(R.id.planListRecyclerView)
            .wait()
            .scrollTo()
            .withId(R.id.selectPlan)
            .hasSibling(
                view.withId(R.id.planNameText).withText(plan.name)
            )
            .wait()
            .click()
        return T::class.java.newInstance()
    }

    class Verify : CoreVerify() {
        fun planDetailsDisplayed(plan: User.Plan?) {
            view
                .withId(R.id.planListRecyclerView)
                .wait()
                .scrollTo()
                .withId(R.id.planContents).hasSibling(
                view.withId(R.id.planNameText).withText(plan!!.name)
            ).wait()
        }

        fun canSelectPlan(plan: User.Plan) {
            view
                .withId(R.id.selectPlan)
                .hasSibling(
                    view.withId(R.id.planNameText).withText(plan.name)
                )
                .wait()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}