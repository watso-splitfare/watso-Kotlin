package com.saengsaengtalk.app.APIS

import com.google.gson.annotations.SerializedName

/** 계정 API 관련 데이터 모델 */

data class DuplicationResult(
    val is_duplicated: Boolean,
)

data class SignUpModel(
    @SerializedName("user_name")
    val userName: String,
    val pw: String,
    @SerializedName("nick_name")
    val nickName: String,
    @SerializedName("student_num")
    val studentNum: String,
    @SerializedName("account_number")
    val accountNumber: String,
    val email: String
)

data class SignUpResult(
    val message: String?
)

data class LoginModel(
    @SerializedName("user_name")
    val userName: String,
    val pw: String,
    @SerializedName("registration_token")
    val registrationToken: String
)

data class LoginResult(
    val id: String,
    val nick_name: String
)

data class LogoutResult(
    val message: String?
)
data class RefreshResult(
    val message: String?
)