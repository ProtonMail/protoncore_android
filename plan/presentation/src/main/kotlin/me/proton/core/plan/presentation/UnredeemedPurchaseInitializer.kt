/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.plan.presentation

import android.content.Context
import android.content.Intent
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.plan.presentation.ui.StartUnredeemedPurchase
import me.proton.core.plan.presentation.usecase.CheckUnredeemedGooglePurchase
import me.proton.core.presentation.app.AppLifecycleProvider
import me.proton.core.util.kotlin.CoroutineScopeProvider
import me.proton.core.util.kotlin.DispatcherProvider
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage

@ExcludeFromCoverage
class UnredeemedPurchaseInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            UnredeemedPurchaseInitializerEntryPoint::class.java
        )

        entryPoint.coroutineScopeProvider().GlobalDefaultSupervisedScope.launch {
            performCheck(
                context,
                entryPoint.accountManager(),
                entryPoint.appLifecycleProvider(),
                entryPoint.checkUnredeemedGooglePurchase(),
                entryPoint.dispatcherProvider()
            )
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    private suspend fun performCheck(
        context: Context,
        accountManager: AccountManager,
        appLifecycleProvider: AppLifecycleProvider,
        checkUnredeemedGooglePurchase: CheckUnredeemedGooglePurchase,
        dispatcherProvider: DispatcherProvider
    ) {
        // Wait for app to come to foreground:
        appLifecycleProvider.state.first { it == AppLifecycleProvider.State.Foreground }

        val userId = accountManager.getPrimaryUserId().first() ?: return
        val unredeemed = checkUnredeemedGooglePurchase.invoke(userId)

        if (unredeemed != null && appLifecycleProvider.state.value == AppLifecycleProvider.State.Foreground) {
            withContext(dispatcherProvider.Main) {
                val intent = StartUnredeemedPurchase.createIntent(context, Unit)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface UnredeemedPurchaseInitializerEntryPoint {
        fun accountManager(): AccountManager
        fun appLifecycleProvider(): AppLifecycleProvider
        fun checkUnredeemedGooglePurchase(): CheckUnredeemedGooglePurchase
        fun coroutineScopeProvider(): CoroutineScopeProvider
        fun dispatcherProvider(): DispatcherProvider
    }
}
