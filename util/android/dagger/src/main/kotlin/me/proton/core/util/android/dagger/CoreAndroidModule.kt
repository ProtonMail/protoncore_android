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

package me.proton.core.util.android.dagger

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.presentation.app.AppLifecycleObserver
import me.proton.core.presentation.app.AppLifecycleProvider
import me.proton.core.util.kotlin.CoroutineScopeProvider
import me.proton.core.util.kotlin.DefaultCoroutineScopeProvider
import me.proton.core.util.kotlin.DefaultDispatcherProvider
import me.proton.core.util.kotlin.DispatcherProvider
import java.time.Clock
import javax.inject.Singleton
import kotlin.time.TimeSource

@Module
@InstallIn(SingletonComponent::class)
public abstract class CoreAndroidModule {
    @Binds
    @Singleton
    internal abstract fun bindAppLifecycleProvider(impl: AppLifecycleObserver): AppLifecycleProvider

    @Binds
    @Singleton
    internal abstract fun bindCoroutineScopeProvider(impl: DefaultCoroutineScopeProvider): CoroutineScopeProvider

    @Binds
    @Singleton
    internal abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider

    public companion object {
        @Provides
        @UtcClock
        internal fun provideClock(): Clock = Clock.systemUTC()

        @Provides
        @Monotonic
        internal fun provideMonotonicTimeSource(): TimeSource = TimeSource.Monotonic
    }
}
