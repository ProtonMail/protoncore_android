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

package me.proton.core.test.rule.annotation.payments

import me.proton.core.domain.entity.AppStore
import me.proton.core.test.quark.v2.command.setPaymentMethods
import me.proton.core.test.rule.annotation.AnnotationTestData

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class TestPaymentMethods(
    val appStore: AppStore = AppStore.GooglePlay,
    val card: Boolean = true,
    val paypal: Boolean = true,
    val inApp: Boolean = true
)

public val TestPaymentMethods.annotationTestData: AnnotationTestData<TestPaymentMethods>
    get() = AnnotationTestData.forDefault(
        this,
        implementation = { data: TestPaymentMethods ->
            setPaymentMethods(
                data.appStore,
                data.card,
                data.paypal,
                data.inApp
            )
        },
        tearDown = { _ ->
            setPaymentMethods(
                card = true,
                paypal = true,
                inApp = true
            )
        }
    )
