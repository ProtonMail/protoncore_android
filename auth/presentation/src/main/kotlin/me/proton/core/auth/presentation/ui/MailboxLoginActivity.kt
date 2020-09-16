/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.auth.presentation.ui

import android.os.Bundle
import me.proton.android.core.presentation.utils.onClick
import me.proton.android.core.presentation.utils.openLinkInBrowser
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityMailboxloginBinding

/**
 * Mailbox Login Activity which allows users to unlock their Mailbox.
 * Note that this is only valid for accounts which are 2 password accounts (they use separate password for login and
 * mailbox).
 * @author Dino Kadrikj.
 */
class MailboxLoginActivity : ProtonAuthActivity<ActivityMailboxloginBinding>() {
    override fun layoutId(): Int = R.layout.activity_mailboxlogin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            closeButton.onClick {
                finish()
            }

            forgotPasswordButton.onClick {
                openLinkInBrowser(getString(R.string.forgot_password_link))
            }
        }
    }
}
