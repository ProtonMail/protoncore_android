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

package me.proton.core.challenge.dagger

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.challenge.data.ChallengeManagerImpl
import me.proton.core.challenge.data.db.ChallengeDatabase
import me.proton.core.challenge.data.repository.ChallengeRepositoryImpl
import me.proton.core.challenge.domain.ChallengeManager
import me.proton.core.challenge.domain.repository.ChallengeRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public object ChallengeModule {

    @Provides
    @Singleton
    public fun provideChallengeRepository(
        db: ChallengeDatabase
    ): ChallengeRepository = ChallengeRepositoryImpl(db)

    @Provides
    @Singleton
    public fun provideChallengeManager(
        challengeRepository: ChallengeRepository
    ): ChallengeManager = ChallengeManagerImpl(challengeRepository)
}
