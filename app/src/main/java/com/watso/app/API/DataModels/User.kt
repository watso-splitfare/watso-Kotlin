package com.watso.app.API

import com.google.gson.annotations.SerializedName


/** 유저 API 관련 데이터 모델 */

data class SignUpModel(
    @SerializedName("auth_token")
    val authToken: String,
    val name: String,
    val username: String,
    @SerializedName("pw")
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


/** 기기 관리 */

data class SendFcmToken(
    @SerializedName("key")
    val loginKey: String,
    @SerializedName("device_token")
    val fcmToken: String
)

data class setNotificationPermission(
    @SerializedName("device_token")
    val fcmToken: String,
    val allow: Boolean
)

data class getNotificationPermission(
    val allow: Boolean
)