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

package me.proton.core.challenge.presentation

import android.content.Context
import android.util.AttributeSet
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.challenge.domain.ChallengeFrameType
import me.proton.core.challenge.domain.ChallengeManagerConfig
import me.proton.core.challenge.domain.ChallengeManagerProvider
import javax.inject.Inject

@AndroidEntryPoint
class UsernameInput @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ProtonMetadataInput(context, attrs, defStyleAttr) {

    @Inject
    lateinit var challengeManagerProvider: ChallengeManagerProvider

    suspend fun flush() {
        val config = ChallengeManagerConfig.SignUp
        val challengeManager = challengeManagerProvider.get(config)
        challengeManager.addOrUpdateFrame(
            challengeType = ChallengeFrameType.Username,
            focusTime = calculateFocus(),
            clicks = clicksCounter,
            copies = copies,
            pastes = pastes,
            keys = keys
        )
    }
}
