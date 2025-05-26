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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionHint
import me.proton.core.compose.theme.captionWeak
import me.proton.core.compose.viewmodel.hiltViewModelOrNull
import me.proton.core.domain.entity.UserId
import me.proton.core.passvalidator.presentation.R

public data class PasswordPolicyReportStyle(
    val brandNorm: Color,
    val iconHint: Color,
    val notificationError: Color,
    val captionHint: TextStyle,
    val captionRegular: TextStyle,
    val captionWeak: TextStyle
)

@Composable
public fun LegacyPasswordPolicyReportStyle(): PasswordPolicyReportStyle = PasswordPolicyReportStyle(
    brandNorm = LocalColors.current.brandNorm,
    iconHint = LocalColors.current.iconHint,
    notificationError = LocalColors.current.notificationError,
    captionHint = LocalTypography.current.captionHint,
    captionRegular = LocalTypography.current.captionRegular,
    captionWeak = LocalTypography.current.captionWeak,
)

@Composable
public fun PasswordPolicyReport(
    passwordFlow: Flow<String>,
    userId: UserId?,
    onResult: (isPasswordValid: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    style: PasswordPolicyReportStyle = LegacyPasswordPolicyReportStyle(),
    viewModel: PasswordPolicyReportViewModel? = hiltViewModelOrNull(key = userId?.id)
) {
    val password by passwordFlow.collectAsStateWithLifecycle(initialValue = "")

    PasswordPolicyReport(
        password = password,
        userId = userId,
        onResult = onResult,
        modifier = modifier,
        style = style,
        viewModel = viewModel
    )
}

@Composable
public fun PasswordPolicyReport(
    password: String,
    userId: UserId?,
    onResult: (isPasswordValid: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    style: PasswordPolicyReportStyle = LegacyPasswordPolicyReportStyle(),
    viewModel: PasswordPolicyReportViewModel? = hiltViewModelOrNull(key = userId?.id)
) {
    val state by viewModel?.state?.collectAsStateWithLifecycle() ?: return

    LaunchedEffect(password) {
        viewModel?.perform(PasswordPolicyReportAction.Validate(password, userId))
    }

    PasswordPolicyReport(
        state = state,
        modifier = modifier,
        onResult = onResult,
        style = style
    )
}

@Composable
internal fun PasswordPolicyReport(
    state: PasswordPolicyReportState,
    modifier: Modifier = Modifier,
    onResult: (isPasswordValid: Boolean) -> Unit = {},
    style: PasswordPolicyReportStyle = LegacyPasswordPolicyReportStyle()
) {
    LaunchedEffect(state) {
        onResult(state is PasswordPolicyReportState.Idle && state.allPassed())
    }

    when (state) {
        is PasswordPolicyReportState.Loading -> Unit
        is PasswordPolicyReportState.Hidden -> Unit
        is PasswordPolicyReportState.Idle -> PasswordPolicyReport(
            messages = state.messages,
            modifier = modifier,
            style = style
        )
    }
}

@Composable
private fun PasswordPolicyReport(
    messages: List<PasswordPolicyReportMessage>,
    modifier: Modifier = Modifier,
    style: PasswordPolicyReportStyle = LegacyPasswordPolicyReportStyle()
) {
    val errors = remember(messages) { messages.filterIsInstance<PasswordPolicyReportMessage.Error>() }
    val hints = remember(messages) { messages.filterIsInstance<PasswordPolicyReportMessage.Hint>() }
    val requirements = remember(messages) { messages.filterIsInstance<PasswordPolicyReportMessage.Requirement>() }

    Column(modifier = modifier) {
        errors.forEach { msg ->
            PasswordPolicyError(
                msg,
                modifier = Modifier.padding(bottom = ProtonDimens.ExtraSmallSpacing),
                captionRegular = style.captionRegular,
                notificationError = style.notificationError
            )
        }

        hints.forEach { hint ->
            PasswordPolicyHint(
                hint,
                captionWeak = style.captionWeak,
                modifier = Modifier.padding(bottom = ProtonDimens.ExtraSmallSpacing)
            )
        }

        if (requirements.isNotEmpty()) {
            Text(
                text = stringResource(R.string.password_policy_must_have_hint),
                style = style.captionWeak,
                modifier = Modifier.padding(bottom = ProtonDimens.ExtraSmallSpacing)
            )
        }

        requirements.forEach { msg ->
            PasswordPolicyRequirement(
                msg = msg,
                style = style,
                modifier = Modifier.padding(bottom = ProtonDimens.ExtraSmallSpacing)
            )
        }
    }
}

@Composable
private fun PasswordPolicyError(
    msg: PasswordPolicyReportMessage.Error,
    captionRegular: TextStyle,
    notificationError: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = msg.message,
        style = captionRegular,
        color = notificationError,
        modifier = modifier
    )
}

@Composable
private fun PasswordPolicyHint(
    msg: PasswordPolicyReportMessage.Hint,
    captionWeak: TextStyle,
    modifier: Modifier = Modifier
) {
    Text(
        text = msg.message,
        style = captionWeak,
        modifier = modifier
    )
}

@Composable
private fun PasswordPolicyRequirement(
    msg: PasswordPolicyReportMessage.Requirement,
    style: PasswordPolicyReportStyle,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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
                msg.success -> style.brandNorm
                else -> style.iconHint
            }
        )
        Text(
            text = msg.message,
            style = when {
                msg.success -> style.captionHint
                else -> style.captionWeak
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

@Composable
@Preview
private fun PasswordPolicyReportPreview() {
    ProtonTheme {
        PasswordPolicyReport(
            messages = listOf(
                PasswordPolicyReportMessage.Error("First error"),
                PasswordPolicyReportMessage.Error("Second error"),
                PasswordPolicyReportMessage.Hint("Hint message", success = false),
                PasswordPolicyReportMessage.Requirement("First requirement", success = true),
                PasswordPolicyReportMessage.Requirement("Second requirement", success = false)
            )
        )
    }
}
