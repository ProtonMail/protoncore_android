package me.proton.core.payment.domain.usecase

import kotlinx.coroutines.delay
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.PaymentTokenResult
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.TokenDisapprovedException
import me.proton.core.payment.domain.entity.TokenPollingTimeoutException
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds


//region Constants

private const val MAX_ATTEMPTS: Int = 30
private const val POLL_INTERVAL: Int = 1

//endregion

/**
 *  For a given [ProtonPaymentToken], poll the token status endpoint, asking if the token has been
 *  approved. We try a maximum of [MAX_ATTEMPTS] times, with an interval of [POLL_INTERVAL] second
 *  between calls.
 *
 *  If we have not confirmed the approval status of the token by the maximum attempt count, then we
 *  must leave it up to the unredeemed purchase flow to handle a further retry.
 */
public class PollPaymentTokenStatus @Inject constructor(
    private val getPaymentTokenStatus: GetPaymentTokenStatus
) {

    public suspend operator fun invoke(
        userId: UserId?,
        paymentToken: ProtonPaymentToken
    ): PaymentTokenResult {
        repeat(MAX_ATTEMPTS) { attempt ->
            val result = getPaymentTokenStatus(userId, paymentToken)

            when (val status = result.status) {
                PaymentTokenStatus.PENDING -> {
                    if (attempt < MAX_ATTEMPTS - 1) {
                        delay(duration = POLL_INTERVAL.seconds)
                    }
                }
                PaymentTokenStatus.CHARGEABLE -> {
                    return result
                }
                PaymentTokenStatus.CONSUMED,
                PaymentTokenStatus.FAILED,
                PaymentTokenStatus.NOT_SUPPORTED -> {
                    throw TokenDisapprovedException("Payment token was not approved: $status")
                }
            }
        }

        throw TokenPollingTimeoutException("Failed to get approved payment token response after $MAX_ATTEMPTS tries.")
    }
}