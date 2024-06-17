package me.proton.core.observability.domain.metrics

import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.network.domain.ResponseCodes
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfirmPasswordSubmissionTotalTest {
    @Test
    fun `result to http401PasswordWrong`() {
        val data = ConfirmPasswordSubmissionTotal(
            makeHttpFailure(
                HttpResponseCodes.HTTP_UNAUTHORIZED,
                "Unauthorized",
                ApiResult.Error.ProtonData(code = ResponseCodes.PASSWORD_WRONG, error = "Password wrong")
            ),
            ConfirmPasswordSubmissionTotal.SecondFactorProofType.totp
        )
        assertEquals(
            ConfirmPasswordSubmissionTotal.Status.http401PasswordWrong,
            data.Labels.status
        )
    }

    @Test
    fun `result to http422PasswordWrong`() {
        val data = ConfirmPasswordSubmissionTotal(
            makeHttpFailure(
                HttpResponseCodes.HTTP_UNPROCESSABLE,
                "Unprocessable",
                ApiResult.Error.ProtonData(code = ResponseCodes.PASSWORD_WRONG, error = "Password wrong")
            ),
            ConfirmPasswordSubmissionTotal.SecondFactorProofType.securityKey
        )
        assertEquals(
            ConfirmPasswordSubmissionTotal.Status.http422PasswordWrong,
            data.Labels.status
        )
    }

    @Test
    fun `result to http4xx`() {
        val data = ConfirmPasswordSubmissionTotal(
            makeHttpFailure(
                HttpResponseCodes.HTTP_UNAUTHORIZED,
                "Unauthorized"
            ),
            ConfirmPasswordSubmissionTotal.SecondFactorProofType.totp
        )
        assertEquals(
            ConfirmPasswordSubmissionTotal.Status.http4xx,
            data.Labels.status
        )
    }

    private fun makeHttpFailure(
        httpCode: Int,
        httpMessage: String,
        protonData: ApiResult.Error.ProtonData? = null
    ): Result<Any> = Result.failure(
        ApiException(
            ApiResult.Error.Http(httpCode, httpMessage, protonData)
        )
    )
}