package com.saengsaengtalk.app.APIS

import com.google.gson.annotations.SerializedName

/** 계정 API 관련 데이터 모델 */

data class DuplicationResult(
    val is_duplicated: Boolean,
)

data class SignUpModel(
    @SerializedName("auth_code")
    val authCode: String,
    val name: String,
    val username: String,
    val pw: String,
    val nickname: String,
    @SerializedName("account_number")
    val accountNumber: String,
    val email: String
)

data class LoginModel(
    val username: String,
    val pw: String,
    @SerializedName("registration_token")
    val registrationToken: String
)

data class LoginResult(
    val id: String,
    @SerializedName("nickname")
    val nickname: String
)

data class UserInfo(
    val id: Long,
    val name: String,
    val username: String,
    val nickname: String,
    @SerializedName("account_number")
    val accountNumber: String,
    val email: String
)