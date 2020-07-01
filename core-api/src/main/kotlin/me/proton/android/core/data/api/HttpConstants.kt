package me.proton.android.core.data.api

/*
 * Constants for HTTP calls
 * Author: Davide Farella
 */

const val BEARER_TOKEN_TYPE = "Bearer"
const val PM_ACCEPT_HEADER_V1 = "Accept: application/vnd.protonmail.v1+json"
const val JSON_CONTENT_TYPE = "Content-Type: application/json;charset=utf-8"
const val REFRESH_PATH = "/auth/refresh"
const val AUTH_PATH = "auth"
const val AUTH_INFO_PATH = "auth/info"
const val RESPONSE_CODE_TOO_MANY_REQUESTS = 429