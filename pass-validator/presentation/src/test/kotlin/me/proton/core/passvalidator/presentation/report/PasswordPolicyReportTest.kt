/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.passvalidator.presentation.report

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import me.proton.core.compose.theme.ProtonTheme
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import kotlin.test.Test

@RunWith(Parameterized::class)
class PasswordPolicyReportTest(deviceConfig: DeviceConfig) {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = deviceConfig)

    @Test
    fun `loading state`() {
        testCase {
            PasswordPolicyReport(PasswordPolicyReportState.Loading)
        }
    }

    @Test
    fun `hidden state`() {
        testCase {
            PasswordPolicyReport(PasswordPolicyReportState.Hidden)
        }
    }

    @Test
    fun `idle state`() {
        testCase {
            PasswordPolicyReport(
                PasswordPolicyReportState.Idle(
                    listOf(
                        PasswordPolicyReportMessage.Error("First error"),
                        PasswordPolicyReportMessage.Error("Second error"),
                        PasswordPolicyReportMessage.Hint("Hint message", success = false),
                        PasswordPolicyReportMessage.Requirement(
                            "Requirement success",
                            success = true
                        ),
                        PasswordPolicyReportMessage.Requirement(
                            "Requirement pending",
                            success = false
                        )
                    )
                )
            )
        }
    }

    private fun testCase(content: @Composable () -> Unit) {
        paparazzi.snapshot {
            ProtonTheme {
                Surface {
                    content()
                }
            }
        }
    }

    companion object {
        @Parameters
        @JvmStatic
        fun parameters() = listOf(
            DeviceConfig.NEXUS_4,
            DeviceConfig.NEXUS_4.copy(nightMode = NightMode.NIGHT)
        )
    }
}
