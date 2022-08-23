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

package me.proton.core.payment.presentation.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import me.proton.core.payment.presentation.R
import me.proton.core.payment.presentation.databinding.FragmentBillingBinding
import me.proton.core.presentation.ui.view.ProtonInput
import me.proton.core.presentation.utils.CardType
import me.proton.core.presentation.utils.InputValidationResult
import me.proton.core.presentation.utils.inTransaction
import me.proton.core.presentation.utils.validate
import me.proton.core.presentation.utils.validateCreditCard
import me.proton.core.presentation.utils.validateCreditCardCVC
import me.proton.core.presentation.utils.validateExpirationDate
import me.proton.core.util.kotlin.exhaustive

private const val TAG_BILLING_FRAGMENT = "billing_fragment"
private const val TAG_BILLING_IAP_FRAGMENT = "billing_iap_fragment"

internal const val MAX_CARD_LENGTH = 16
internal const val CARD_NUMBER_CHUNKS_LENGTH = 4
internal const val MAX_EXP_DATE_LENGTH = 4
internal const val EXP_DATE_CHUNKS_LENGTH = 2

internal class CardNumberWatcher(
    var cardNumber: String = ""
) {
    val watcher: ProtonInput.(Editable) -> Unit = { editable ->
        val inputString = editable.toString()
        if (inputString != cardNumber) {
            val digitsOnlyInput = inputString.replace(Regex("[^\\d]"), "")
            if (digitsOnlyInput.length <= MAX_CARD_LENGTH) {
                cardNumber = digitsOnlyInput.chunked(CARD_NUMBER_CHUNKS_LENGTH).joinToString(" ")
            }
            editable.replace(0, editable.length, cardNumber, 0, cardNumber.length)
            validateCreditCard().setCardIcon(context) {
                it?.let {
                    endIconMode = ProtonInput.EndIconMode.CUSTOM_ICON
                    endIconDrawable = it
                }
            }
        }
    }
}

internal class ExpirationDateWatcher(
    var expirationDate: String = ""
) {
    val watcher: ProtonInput.(Editable) -> Unit = { editable ->
        val inputString = editable.toString()
        if (inputString != expirationDate) {
            val digitsOnlyInput = inputString.replace(Regex("[^\\d]"), "")
            if (digitsOnlyInput.length <= MAX_EXP_DATE_LENGTH) {
                expirationDate = digitsOnlyInput.chunked(EXP_DATE_CHUNKS_LENGTH).joinToString("/")
            }
            editable.replace(0, editable.length, expirationDate, 0, expirationDate.length)
        }
    }
}

internal fun InputValidationResult.setCardIcon(context: Context, block: (Drawable?) -> Unit) {
    if (!isValid) {
        return
    }
    val drawable = when (cardType) {
        CardType.VISA -> R.drawable.ic_card_visa
        CardType.MASTERCARD -> R.drawable.ic_card_master
        CardType.AMEX -> R.drawable.ic_card_amex
        CardType.DISCOVER -> R.drawable.ic_card_discover
        else -> R.drawable.ic_proton_credit_card
    }.exhaustive
    block(ContextCompat.getDrawable(context, drawable))
}

/**
 * Returns the list of billing input fields (views) validation result.
 * Every validation also marks the appropriate field as invalid.
 */
internal fun FragmentBillingBinding.billingInputFieldsValidationList(context: Context): List<InputValidationResult> =
    listOf(
        cardNameInput.validate().also {
            if (!it.isValid) cardNameInput.setInputError(context.getString(R.string.payments_error_card_name))
        },
        cardNumberInput.validateCreditCard().also {
            if (!it.isValid) cardNumberInput.setInputError(context.getString(R.string.payments_error_card_number))
        },
        cvcInput.validateCreditCardCVC().also {
            if (!it.isValid) cvcInput.setInputError(context.getString(R.string.payments_error_cvc))
        },
        expirationDateInput.validateExpirationDate().also {
            if (!it.isValid) expirationDateInput.setInputError()
        },
        postalCodeInput.validate().also {
            if (!it.isValid) postalCodeInput.setInputError()
        },
        countriesText.validate().also {
            if (!it.isValid) countriesText.setInputError()
            else countriesText.clearInputError()
        }
    )

internal fun FragmentManager.showBillingFragment(
    containerId: Int = android.R.id.content
) {
    removeBillingIAPFragment()
    val fragment = findFragmentByTag(TAG_BILLING_FRAGMENT) ?: run { BillingFragment() }
    inTransaction {
        setCustomAnimations(0, 0)
        replace(containerId, fragment, TAG_BILLING_FRAGMENT)
        addToBackStack(TAG_BILLING_FRAGMENT)
    }
}

internal fun FragmentManager.showBillingIAPFragment(
    containerId: Int = android.R.id.content
) {
    removeBillingFragment()
    val fragment = findFragmentByTag(TAG_BILLING_IAP_FRAGMENT) ?: run {
        Class.forName(
            "me.proton.core.paymentiap.presentation.ui.BillingIAPFragment",
            false,
            this::class.java.classLoader
        ).newInstance() as Fragment
    }
    inTransaction {
        setCustomAnimations(0, 0)
        replace(containerId, fragment, TAG_BILLING_IAP_FRAGMENT)
        addToBackStack(TAG_BILLING_IAP_FRAGMENT)
    }
}

internal fun FragmentManager.removeBillingFragment() = findFragmentByTag(TAG_BILLING_FRAGMENT)?.let { billing ->
    inTransaction {
        setCustomAnimations(0, 0)
        remove(billing)
    }
    popBackStack()
}

internal fun FragmentManager.removeBillingIAPFragment() =
    findFragmentByTag(TAG_BILLING_IAP_FRAGMENT)?.let { billingIAP ->
        inTransaction {
            setCustomAnimations(0, 0)
            remove(billingIAP)
        }
        popBackStack()
    }
