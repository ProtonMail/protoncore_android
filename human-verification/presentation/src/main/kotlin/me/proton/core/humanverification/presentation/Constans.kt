package me.proton.core.humanverification.presentation

/**
 * Created by dinokadrikj on 6/12/20.
 */
enum class TokenType(val tokenTypeValue: String) {
    SMS("sms"),
    EMAIL("email"),
    PAYMENT("payment"),
    CAPTCHA("captcha");

    companion object {
        fun fromString(tokenTypeValue: String): TokenType {
            return values().find {
                tokenTypeValue == it.tokenTypeValue
            } ?: SMS
        }
    }
}
