package com.example.saengsaengtalk.APIS

/** 계정 API 관련 데이터 모델 */

data class OverlapResult(
    val result: Boolean
)

data class SignUpResult(
    val result: Boolean
)

data class SignUpModel(
    val user_name: String,
    val pw: String,
    val student_num: Int,
    val nick_name: String
)

data class LoginModel(
    val user_name: String,
    val pw: String
)

data class LoginResult(
    val result: Boolean,
    val user_id: Long
)

data class LogoutResult(
    val result: Boolean,
    //val code: Int
)