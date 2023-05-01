package com.saengsaengtalk.app.API

import com.google.gson.annotations.SerializedName

/** Auth API 관련 데이터 모델 */

data class LoginModel(
    val username: String,
    val pw: String,
    @SerializedName("registration_token")
    val registrationToken: String
)