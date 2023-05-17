package com.watso.app.API

import retrofit2.Call
import retrofit2.http.*

interface UserAPI {

    companion object
    {
        const val SIGNUP = "user/signup"
        const val PROFILE = "user/profile"
        const val FORGOT = "user/forgot"
    }

    /** 회원 가입 */

    @POST(SIGNUP)                    // 회원가입
    fun signup(
        @Body jsonparams: SignUpModel
    ): Call<VoidResponse>

    @GET(SIGNUP)                     // 메일 인증코드 발송 요청
    fun sendVerificationCode(
        @Query("email") email: String
    ): Call<VoidResponse>

    @GET("$SIGNUP/validation-check")        // 인증코드 유효성 검사
    fun checkVerificationCode(
        @Query("email") email: String,
        @Query("auth-code") authCode: String
    ): Call<VerificationResponse>

    /** 프로필 */

    @GET(PROFILE)                       // 유저 정보 조회
    fun getUserInfo(): Call<UserInfo>

    @DELETE(PROFILE)                    // 회원 탈퇴
    fun deleteAccount(): Call<VoidResponse>

    @PATCH("$PROFILE/account-number")   // 계좌번호 변경
    fun updateAccountNumber(
        @Body jsonparams: UpdateAccountNumber
    ): Call<VoidResponse>

    @PATCH("$PROFILE/nickname")         // 닉네임 변경
    fun updateNickname(
        @Body jsonparams: UpdateNickname
    ): Call<VoidResponse>

    @PATCH("$PROFILE/password")         // 비밀번호 변경
    fun updatePassword(
        @Body jsonparams: UpdatePassword
    ): Call<VoidResponse>

    /** 찾기 */

    @POST("$FORGOT/password")           // 비밀번호 찾기
    fun issueTempPassword(
        @Body jsonparams: ForgotPassword
    ): Call<VoidResponse>

    @GET("$FORGOT/username")            // 계정 찾기
    fun sendForgotUsername(
        @Query("email") email: String
    ): Call<VoidResponse>

    /** 중복 체크 */

    @GET("user/duplicate-check")        // 정보 중복조회
    fun checkDuplication(
        @Query("field") field: String,
        @Query("value") value: String
    ): Call<DuplicationCheckResult>
}