package com.watso.app.API

import com.google.gson.annotations.SerializedName

/** 유저 API 관련 데이터 모델 */

data class SignUpModel(
    @SerializedName("auth_token")
    val authToken: String,
    val name: String,
    val username: String,
    val password: String,
    val nickname: String,
    @SerializedName("account_number")
    val accountNumber: String,
    val email: String
)

data class VerificationResponse(
    @SerializedName("auth_token")
    val authToken: String
)

data class UserInfo(
    val _id: Long,
    val name: String,
    val username: String,
    val nickname: String,
    @SerializedName("account_number")
    val accountNumber: String,
    val email: String
)

/** 프로필 API 관련 데이터 모델 */

data class UpdateAccountNumber(
    @SerializedName("account_number")
    val accountNumber: String
)
data class UpdateNickname(
    @SerializedName("nickname")
    val nickname: String
)
data class UpdatePassword(
    @SerializedName("current_password")
    val currentPassword: String,
    @SerializedName("new_password")
    val newPassword: String
)

/** 찾기 */

data class ForgotPassword(
    val username: String,
    val email: String
)

data class DuplicationCheckResult(
    @SerializedName("is_duplicated")
    val isDuplicated: Boolean
)