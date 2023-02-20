package com.example.saengsaengtalk.APIS

/** 계정 API 관련 데이터 모델 */

data class DuplicationResult(
    val is_duplicated: Boolean?,
    val message: String?
)

data class SignUpModel(
    val user_name: String,
    val pw: String,
    val nick_name: String,
    val student_num: String,
    val account_number: String,
    val email: String
)

data class SignUpResult(
    val message: String?
)

data class LoginModel(
    val user_name: String,
    val pw: String
)

data class LoginResult(
    val success: Boolean?,
    val id: String?,
    val nick_name: String?,
    val message: String?
)

data class LogoutResult(
    val message: String?
)