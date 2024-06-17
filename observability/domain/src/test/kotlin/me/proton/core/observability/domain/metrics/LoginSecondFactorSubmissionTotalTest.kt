package me.proton.core.observability.domain.metrics

import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.network.domain.ResponseCodes
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginSecondFactorSubmissionTotalTest {
    @Test
    fun `result to http401PasswordWrong`() {
        val data = LoginSecondFactorSubmissionTotal(
            makeHttpFailure(
                HttpResponseCodes.HTTP_UNAUTHORIZED,
                "Unauthorized",
                ApiResult.Error.ProtonData(code = ResponseCodes.PASSWORD_WRONG, error = "Password wrong")
            ),
            LoginSecondFactorSubmissionTotal.SecondFactorProofType.totp
        )
        assertEquals(
            LoginSecondFactorSubmissionTotal.ApiStatus.http401PasswordWrong,
            data.Labels.status
        )
    }

    @Test
    fun `result to http422PasswordWrong`() {
        val data = LoginSecondFactorSubmissionTotal(
            makeHttpFailure(
                HttpResponseCodes.HTTP_UNPROCESSABLE,
                "Unprocessable",
                ApiResult.Error.ProtonData(code = ResponseCodes.PASSWORD_WRONG, error = "Password wrong")
            ),
            LoginSecondFactorSubmissionTotal.SecondFactorProofType.securityKey
        )
        assertEquals(
            LoginSecondFactorSubmissionTotal.ApiStatus.http422PasswordWrong,
            data.Labels.status
        )
    }

    @Test
    fun `result to http4xx`() {
        val data = LoginSecondFactorSubmissionTotal(
            makeHttpFailure(
                HttpResponseCodes.HTTP_UNAUTHORIZED,
                "Unauthorized"
            ),
            LoginSecondFactorSubmissionTotal.SecondFactorProofType.totp
        )
        assertEquals(
            LoginSecondFactorSubmissionTotal.ApiStatus.http4xx,
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
