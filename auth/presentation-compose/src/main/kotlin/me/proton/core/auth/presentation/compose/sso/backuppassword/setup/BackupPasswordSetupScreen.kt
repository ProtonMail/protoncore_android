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

package me.proton.core.auth.presentation.compose.sso.backuppassword.setup

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import me.proton.core.auth.presentation.compose.R
import me.proton.core.compose.component.ProtonPasswordOutlinedTextFieldWithError
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.LocalShapes
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.util.formatBold
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.domain.entity.displayName
import me.proton.core.presentation.utils.StringBox

private val CompanyLogoSize = 56.dp
private val CompanyLogoFallbackIconSize = 32.dp
private val MaxFormWidth = 600.dp

public object BackupPasswordSetupScreen {
    public const val KEY_USERID: String = "UserId"
    public fun SavedStateHandle.getUserId(): UserId = UserId(get<String>(KEY_USERID)!!)
}

@Composable
public fun BackupPasswordSetupScreen(
    onClose: () -> Unit,
    onError: (Throwable) -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BackupPasswordSetupViewModel = hiltViewModel()
) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackupPasswordSetupScreen(
        data = data,
        state = state,
        modifier = modifier,
        onCloseClicked = onClose,
        onContinueClicked = { viewModel.submit(it) },
        onError = onError,
        onSuccess = onSuccess
    )
}

@Composable
public fun BackupPasswordSetupScreen(
    data: BackupPasswordSetupUiData,
    state: BackupPasswordSetupUiState,
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onContinueClicked: (BackupPasswordSetupAction.Submit) -> Unit = {},
    onError: (Throwable) -> Unit = {},
    onSuccess: () -> Unit = {},
) {
    val backupPasswordError =
        StringBox(R.string.backup_password_setup_password_too_short).takeIf { state.isPasswordTooShort() }
    val backupPasswordRepeatedError =
        StringBox(R.string.backup_password_setup_password_not_matching).takeIf { state.arePasswordsNotMatching() }

    LaunchedEffect(state) {
        when (state) {
            is BackupPasswordSetupUiState.Error -> onError(state.cause)
            is BackupPasswordSetupUiState.Success -> onSuccess()
            else -> Unit
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onCloseClicked) {
                        Icon(
                            painterResource(id = R.drawable.ic_proton_arrow_back),
                            contentDescription = stringResource(id = R.string.presentation_back)
                        )
                    }
                },
                backgroundColor = LocalColors.current.backgroundNorm
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .padding(ProtonDimens.DefaultSpacing),
            ) {
                SsoOrganizationAdminInfoHeader(
                    organizationAdminEmail = data.organizationAdminEmail,
                    organizationIcon = data.organizationIcon,
                    organizationName = data.organizationName,
                    product = data.product
                )
                Divider(
                    modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
                    color = LocalColors.current.separatorNorm
                )
                SsoBackupPasswordSetupForm(
                    backupPasswordError = backupPasswordError,
                    backupPasswordRepeatedError = backupPasswordRepeatedError,
                    onContinueClicked = onContinueClicked,
                    isLoading = state is BackupPasswordSetupUiState.Loading,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = ProtonDimens.MediumSpacing)
                )
            }
        }
    }
}

@Composable
private fun SsoOrganizationAdminInfoHeader(
    organizationAdminEmail: String?,
    organizationIcon: Any?,
    organizationName: String?,
    product: Product,
    modifier: Modifier = Modifier,
) {
    val defaultLogo = rememberAsyncImagePainter(R.drawable.ic_proton_users)

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(LocalShapes.current.large)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(organizationIcon)
                    .build(),
                fallback = defaultLogo,
                placeholder = defaultLogo,
                modifier = Modifier
                    .size(CompanyLogoSize)
                    .run {
                        if (organizationIcon == null) {
                            background(LocalColors.current.interactionNorm)
                                .padding(max(0.dp, CompanyLogoSize - CompanyLogoFallbackIconSize) / 2)
                        } else this
                    },
                contentDescription = null,
            )
        }

        if (organizationName != null) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = ProtonDimens.MediumSpacing),
                style = LocalTypography.current.headline,
                text = stringResource(id = R.string.backup_password_setup_title, organizationName)
            )
        }

        if (organizationAdminEmail != null) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = ProtonDimens.SmallSpacing),
                style = LocalTypography.current.body2Medium,
                textAlign = TextAlign.Center,
                text = stringResource(R.string.backup_password_setup_subtitle).formatBold(
                    organizationAdminEmail,
                    product.displayName()
                )
            )
        }
    }
}

@Composable
private fun SsoBackupPasswordSetupForm(
    backupPasswordError: StringBox?,
    backupPasswordRepeatedError: StringBox?,
    isLoading: Boolean,
    onContinueClicked: (BackupPasswordSetupAction.Submit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var backupPassword by remember { mutableStateOf("") }
    var repeatBackupPassword by remember { mutableStateOf("") }

    Column(
        modifier = modifier.widthIn(max = MaxFormWidth)
    ) {
        Text(
            color = LocalColors.current.textWeak,
            style = LocalTypography.current.body2Regular,
            text = stringResource(id = R.string.backup_password_setup_description)
        )
        ProtonPasswordOutlinedTextFieldWithError(
            text = backupPassword,
            onValueChanged = { backupPassword = it },
            enabled = !isLoading,
            singleLine = true,
            label = { Text(text = stringResource(id = R.string.backup_password_setup_password_label)) },
            errorText = backupPasswordError?.get(LocalContext.current),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.padding(top = ProtonDimens.MediumSpacing)
        )
        ProtonPasswordOutlinedTextFieldWithError(
            text = repeatBackupPassword,
            onValueChanged = { repeatBackupPassword = it },
            enabled = !isLoading,
            singleLine = true,
            label = { Text(text = stringResource(id = R.string.backup_password_setup_repeat_password_label)) },
            errorText = backupPasswordRepeatedError?.get(LocalContext.current),
            modifier = Modifier.padding(top = ProtonDimens.SmallSpacing)
        )
        ProtonSolidButton(
            contained = false,
            loading = isLoading,
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight),
            onClick = { onContinueClicked(BackupPasswordSetupAction.Submit(backupPassword, repeatBackupPassword)) }
        ) {
            Text(
                text = stringResource(id = R.string.backup_password_setup_continue_action)
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = Devices.TABLET)
@Composable
private fun BackupPasswordSetupScreenPreview() {
    ProtonTheme {
        BackupPasswordSetupScreen(
            data = BackupPasswordSetupUiData(
                organizationAdminEmail = "admin@company.test",
                organizationName = "The Company",
                product = Product.Mail
            ),
            state = BackupPasswordSetupUiState.Idle
        )
    }
}
