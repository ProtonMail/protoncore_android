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

package me.proton.core.configuration.configurator.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.configuration.configurator.BuildConfig
import me.proton.core.configuration.configurator.domain.ConfigurationUseCase
import me.proton.core.test.quark.data.Plan
import me.proton.core.test.quark.data.User
import me.proton.core.test.quark.response.CreateUserQuarkResponse
import me.proton.core.test.quark.response.FixtureDoctrineLoadResponse
import me.proton.core.test.quark.response.FixtureLoadResponse
import me.proton.core.test.quark.v2.QuarkCommand
import me.proton.core.test.quark.v2.command.enableEarlyAccess
import me.proton.core.test.quark.v2.command.loadDoctrineFixture
import me.proton.core.test.quark.v2.command.loadFixture
import me.proton.core.test.quark.v2.command.seedSubscriber
import me.proton.core.test.quark.v2.command.userCreate
import me.proton.core.util.kotlin.startsWith
import okhttp3.Response
import okhttp3.internal.toLongOrDefault
import javax.inject.Inject

@HiltViewModel
class CreateUserViewModel @Inject constructor(
    private val quarkCommand: QuarkCommand,
    internal val sharedData: SharedData,
    private val configurationUseCase: ConfigurationUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    private val _userResponse = MutableStateFlow<String?>(null)
    val userResponse: StateFlow<String?> = _userResponse.asStateFlow()

    val selectedDomain: StateFlow<String> = configurationUseCase.configState.map { set ->
        val hostField = set.first { field -> field.name == "host" }
        hostField.value as String
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialValue = "")

    private fun extractFourDigitId(input: String): String? {
        val regex = """\(ID (\d+)\)""".toRegex()
        val matchResult = regex.find(input)
        val userId = matchResult?.groupValues?.get(1) ?: "0"
        return userId
    }

    fun loadFixture(scenario: String) =
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            lateinit var fixtureDoctrineResponse: FixtureDoctrineLoadResponse
            lateinit var fixtureResponse: FixtureLoadResponse

            try {
                quarkCommand.baseUrl("https://${selectedDomain.value}/api/internal")

                if (scenario.startsWith("qa-mail-web")) {
                    fixtureDoctrineResponse = quarkCommand.loadDoctrineFixture(scenario)
                    sharedData.setUser(
                        fixtureDoctrineResponse.name,
                        fixtureDoctrineResponse.password,
                        fixtureDoctrineResponse.decryptedUserId
                    )
                    _userResponse.value =
                        "User name: ${fixtureDoctrineResponse.name}, " +
                                "password: ${fixtureDoctrineResponse.password}, " +
                                "id: ${fixtureDoctrineResponse.decryptedUserId} seeded successfully."
                } else {
                    fixtureResponse = quarkCommand.loadFixture(scenario, BuildConfig.NEXUS_URL)

                    sharedData.setUser(
                        fixtureResponse.users[0].name,
                        fixtureResponse.users[0].password,
                        fixtureResponse.users[0].id.raw
                    )
                    _userResponse.value = "User name: ${fixtureResponse.users[0].name}, " +
                            "password: ${fixtureResponse.users[0].password}, " +
                            "id: ${fixtureResponse.users[0].id.raw} seeded successfully."

                }
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.localizedMessage
                _userResponse.value = null
            } finally {
                _isLoading.value = false
            }
        }

    fun createUser(
        username: String,
        password: String,
        plan: Plan,
        cycle: String,
        currency: String = "USD",
        coupon: String = "",
        isEnableEarlyAccess: Boolean
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            lateinit var response: Response
            lateinit var quarkResponse: CreateUserQuarkResponse
            try {
                val user = User(name = username, password = password, plan = plan)
                quarkCommand.baseUrl("https://${selectedDomain.value}/api/internal")
                if (plan.planName != "free") {
                    response = quarkCommand.seedSubscriber(
                        user,
                        cycleDurationMonths = cycle.toInt(),
                        currency = currency,
                        coupon = coupon
                    )
                    val responseBody = response.body!!.string()
                    _userResponse.value = responseBody
                    val userId =
                        extractFourDigitId(responseBody)?.toLongOrDefault(0L) ?: 0L

                    if (userId != 0L) {
                        sharedData.setUser(
                            username,
                            password,
                            extractFourDigitId(responseBody)?.toLongOrDefault(0L) ?: 0L
                        )
                    }
                } else {
                    try {
                        quarkResponse = quarkCommand.userCreate(user)
                        sharedData.setUser(
                            quarkResponse.name!!,
                            quarkResponse.password,
                            quarkResponse.decryptedUserId
                        )
                        _userResponse.value =
                            "User name: ${quarkResponse.name!!}, password: ${quarkResponse.password}, " +
                                    "id: ${quarkResponse.decryptedUserId} seeded successfully."
                    } catch (e: Error) {
                        _userResponse.value = null
                        _errorState.value = e.message
                    }
                }

                if (isEnableEarlyAccess) quarkCommand.enableEarlyAccess(user.name)
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.localizedMessage
                _userResponse.value = null
            } finally {
                _isLoading.value = false
            }
        }
}
