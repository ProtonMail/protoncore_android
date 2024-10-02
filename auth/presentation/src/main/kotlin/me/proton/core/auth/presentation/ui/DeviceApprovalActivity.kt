/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.auth.presentation.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.compose.DeviceApprovalRoutes.Route
import me.proton.core.auth.presentation.compose.DeviceApprovalRoutes.addMemberSelfApprovalScreen
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.errorToast

@AndroidEntryPoint
class DeviceApprovalActivity : ProtonActivity() {
    private val memberUserId: UserId? get() = intent.getStringExtra(KEY_MEMBER_USER_ID)?.let { UserId(it) }
    private val userId: UserId get() = UserId(requireNotNull(intent.getStringExtra(KEY_USER_ID)))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProtonTheme {
                NavHost(
                    navController = rememberNavController(),
                    startDestination = getStartDestination()
                ) {
                    addMemberSelfApprovalScreen(
                        userId = userId,
                        onClose = this@DeviceApprovalActivity::onClose,
                        onError = this@DeviceApprovalActivity::onError
                    )
                    // TODO: addAdminApprovalScreen(userId)
                }
            }
        }
    }

    private fun onClose() {
        finish()
    }

    private fun onError(message: String?) {
        errorToast(message ?: getString(R.string.presentation_error_general))
    }

    private fun getStartDestination(): String {
        val memberUserId = memberUserId
        return when {
            memberUserId != null -> Route.AdminApproval.get(userId, memberUserId)
            else -> Route.SelfApproval.get(userId)
        }
    }

    companion object {
        private const val KEY_MEMBER_USER_ID = "MemberUserId"
        private const val KEY_USER_ID = "UserId"

        private fun getIntent(
            context: Context,
            userId: UserId,
            memberUserId: UserId?
        ) = Intent(context, DeviceApprovalActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(KEY_MEMBER_USER_ID, memberUserId?.id)
            putExtra(KEY_USER_ID, userId.id)
        }

        /**
         * @param userId The ID of a user for whom the approval is requested,
         *  or (if [memberUserId] is non-null) the ID of an admin user.
         * @param memberUserId If non-null, it's the ID of a member user,
         *  for whom the admin can approve the request.
         */
        fun start(
            context: Context,
            userId: UserId,
            memberUserId: UserId?
        ) = context.startActivity(getIntent(context, userId, memberUserId))
    }
}
