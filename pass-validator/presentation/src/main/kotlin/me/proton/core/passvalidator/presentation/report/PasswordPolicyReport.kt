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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionHint
import me.proton.core.compose.theme.captionWeak
import me.proton.core.compose.viewmodel.hiltViewModelOrNull
import me.proton.core.domain.entity.UserId
import me.proton.core.passvalidator.presentation.R

@Composable
public fun PasswordPolicyReport(
    passwordFlow: Flow<String>,
    userId: UserId?,
    onResult: (isPasswordValid: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PasswordPolicyReportViewModel? = hiltViewModelOrNull(key = userId?.id)
) {
    val password by passwordFlow.collectAsStateWithLifecycle(initialValue = "")

    PasswordPolicyReport(
        password = password,
        userId = userId,
        onResult = onResult,
        modifier = modifier,
        viewModel = viewModel
    )
}

@Composable
public fun PasswordPolicyReport(
    password: String,
    userId: UserId?,
    onResult: (isPasswordValid: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PasswordPolicyReportViewModel? = hiltViewModelOrNull(key = userId?.id)
) {
    val state by viewModel?.state?.collectAsStateWithLifecycle() ?: return

    LaunchedEffect(password) {
        viewModel?.perform(PasswordPolicyReportAction.Validate(password, userId))
    }

    PasswordPolicyReport(
        state = state,
        modifier = modifier,
        onResult = onResult
    )
}

@Composable
internal fun PasswordPolicyReport(
    state: PasswordPolicyReportState,
    modifier: Modifier = Modifier,
    onResult: (isPasswordValid: Boolean) -> Unit = {}
) {
    LaunchedEffect(state) {
        onResult(state is PasswordPolicyReportState.Idle && state.allPassed())
    }

    when (state) {
        is PasswordPolicyReportState.Loading -> Unit
        is PasswordPolicyReportState.Hidden -> Unit
        is PasswordPolicyReportState.Idle -> PasswordPolicyReport(messages = state.messages, modifier = modifier)
    }
}

@Composable
internal fun PasswordPolicyReport(
    messages: List<PasswordPolicyReportMessage>,
    modifier: Modifier = Modifier,
) {
    val errors = remember(messages) { messages.filterIsInstance<PasswordPolicyReportMessage.Error>() }
    val requirements = remember(messages) { messages.filterIsInstance<PasswordPolicyReportMessage.Requirement>() }

    Column(modifier = modifier) {
        errors.forEach { msg ->
            Text(
                text = msg.message,
                style = ProtonTheme.typography.captionRegular,
                color = ProtonTheme.colors.notificationError,
                modifier = Modifier.padding(bottom = ProtonDimens.ExtraSmallSpacing)
            )
        }

        if (errors.isNotEmpty() && requirements.isNotEmpty()) {
            Spacer(modifier = Modifier.height(ProtonDimens.ExtraSmallSpacing))
        }

        if (requirements.isNotEmpty()) {
            Text(
                text = stringResource(R.string.password_policy_must_have_hint),
                style = ProtonTheme.typography.captionWeak
            )
        }

        requirements.forEach { msg ->
            Row(
                modifier = Modifier.padding(top = ProtonDimens.ExtraSmallSpacing)
            ) {
                Icon(
                    painter = when {
                        msg.success -> painterResource(R.drawable.ic_proton_circle_checkmark)
                        else -> painterResource(R.drawable.ic_proton_circle)
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .size(ProtonDimens.SmallIconSize)
                        .padding(if (msg.success) 0.dp else 2.dp)
                        .align(Alignment.CenterVertically),
                    tint = when {
                        msg.success -> ProtonTheme.colors.brandNorm
                        else -> ProtonTheme.colors.iconHint
                    }
                )
                Text(
                    text = msg.message,
                    style = when {
                        msg.success -> ProtonTheme.typography.captionHint
                        else -> ProtonTheme.typography.captionWeak
                    },
                    textDecoration = when {
                        msg.success -> TextDecoration.LineThrough
                        else -> null
                    },
                    modifier = Modifier
                        .padding(start = ProtonDimens.ExtraSmallSpacing)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
@Preview
private fun PasswordPolicyReportPreview() {
    ProtonTheme {
        PasswordPolicyReport(
            messages = listOf(
                PasswordPolicyReportMessage.Error("First error"),
                PasswordPolicyReportMessage.Error("Second error"),
                PasswordPolicyReportMessage.Requirement("First requirement", success = true),
                PasswordPolicyReportMessage.Requirement("Second requirement", success = false)
            )
        )
    }
}
