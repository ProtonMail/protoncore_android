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

package me.proton.core.passvalidator.data.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import me.proton.core.auth.domain.feature.IsCommonPasswordCheckEnabled
import me.proton.core.domain.entity.UserId
import me.proton.core.passvalidator.data.LogTag
import me.proton.core.passvalidator.data.entity.PasswordValidatorTokenImpl
import me.proton.core.passvalidator.data.validator.CommonPasswordValidator
import me.proton.core.passvalidator.data.validator.MinLengthPasswordValidator
import me.proton.core.passvalidator.data.validator.PasswordValidator
import me.proton.core.passvalidator.domain.usecase.ValidatePassword
import me.proton.core.presentation.utils.InvalidPasswordProvider
import me.proton.core.util.kotlin.DispatcherProvider
import me.proton.core.util.kotlin.catchAll
import me.proton.core.util.kotlin.takeIfNotEmpty
import javax.inject.Inject

private const val MIN_PASSWORD_LENGTH = 8

@ActivityRetainedScoped
public class ValidatePasswordImpl internal constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val observePasswordPolicyValidators: ObservePasswordPolicyValidators,
    private val isCommonPasswordCheckEnabled: IsCommonPasswordCheckEnabled,
    private val invalidPasswordProvider: InvalidPasswordProvider,
    private val commonPasswordValidator: CommonPasswordValidator,
    private val defaultValidator: MinLengthPasswordValidator
) : ValidatePassword {

    @Inject
    internal constructor(
        @ApplicationContext context: Context,
        dispatcherProvider: DispatcherProvider,
        observePasswordPolicyValidators: ObservePasswordPolicyValidators,
        isCommonPasswordCheckEnabled: IsCommonPasswordCheckEnabled,
        invalidPasswordProvider: InvalidPasswordProvider,
    ) : this(
        dispatcherProvider = dispatcherProvider,
        observePasswordPolicyValidators = observePasswordPolicyValidators,
        isCommonPasswordCheckEnabled = isCommonPasswordCheckEnabled,
        invalidPasswordProvider = invalidPasswordProvider,
        commonPasswordValidator = CommonPasswordValidator(
            context = context,
            hideIfValid = true,
            isPasswordCommon = invalidPasswordProvider::isPasswordCommon,
            isOptional = false
        ),
        defaultValidator = MinLengthPasswordValidator(
            context = context,
            hideIfValid = false,
            isOptional = false,
            minLength = MIN_PASSWORD_LENGTH
        )
    )

    override fun invoke(
        password: String,
        userId: UserId?
    ): Flow<ValidatePassword.Result> = observePasswordPolicyValidators(userId)
        .catchAll(LogTag.FETCH_PASS_POLICY) { emit(emptyList()) }
        .map { policyValidators -> policyValidators.takeIfNotEmpty() ?: listOf(defaultValidator) }
        .combine(getRequiredValidators(userId), Collection<PasswordValidator>::plus)
        .map { validators -> validators.map { it.validate(password) } }
        .map { results ->
            ValidatePassword.Result(
                results = results,
                token = when {
                    results.all { it.isValid != false || it.isOptional } -> PasswordValidatorTokenImpl()
                    else -> null
                }
            )
        }
        .flowOn(dispatcherProvider.Comp)

    private fun getRequiredValidators(userId: UserId?): Flow<List<PasswordValidator>> {
        if (!isCommonPasswordCheckEnabled(userId)) return flowOf(emptyList())
        return flow {
            invalidPasswordProvider.init(MIN_PASSWORD_LENGTH)
            emit(listOf(commonPasswordValidator))
        }.catchAll(LogTag.LOAD_COMMON_PASSWORDS) {
            emit(emptyList())
        }
    }
}
