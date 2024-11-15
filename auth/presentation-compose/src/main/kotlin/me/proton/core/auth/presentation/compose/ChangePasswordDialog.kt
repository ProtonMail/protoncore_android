package me.proton.core.auth.presentation.compose

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import me.proton.core.compose.component.ProtonAlertDialog
import me.proton.core.compose.component.ProtonAlertDialogButton
import me.proton.core.compose.component.ProtonAlertDialogText
import me.proton.core.compose.theme.ProtonTheme

// context.openBrowserLink(context.getString(R.string.login_link))

@Composable
public fun ChangePasswordDialog(
    modifier: Modifier = Modifier,
    onChangeClicked: () -> Unit = { },
) {
    ProtonAlertDialog(
        modifier = modifier,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            securePolicy = SecureFlagPolicy.Inherit
        ),
        title = stringResource(R.string.auth_password_chooser_title),
        text = { ProtonAlertDialogText(stringResource(R.string.auth_password_chooser_description)) },
        onDismissRequest = { Unit },
        confirmButton = {
            Column {
                ProtonAlertDialogButton(
                    modifier = Modifier.align(Alignment.End),
                    title = stringResource(R.string.auth_password_chooser_change),
                    onClick = onChangeClicked,
                    loading = false
                )
            }
        },
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = false)
@Composable
internal fun ChangePasswordDialogPreview() {
    ProtonTheme {
        ChangePasswordDialog()
    }
}
