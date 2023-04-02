package com.saengsaengtalk.app.APIS

import retrofit2.Call
import retrofit2.http.*

interface AuthAPIS {
    /** 계정 관련 API */

    @POST("auth/signup")                    // 회원가입
    fun signup(
        @Body jsonparams: SignUpModel
    ): Call<String>//Call<VoidResponse>

    @GET("auth/signup/duplicate-check")     // 정보 중복조회
    fun duplicateCheck(
        @Query("field") field: String,
        @Query("value") value: String
    ): Call<DuplicationResult>

    @POST("auth/signin")                     // 로그인
    fun login(
        @Body jsonparams: LoginModel
    ): Call<LoginResult>

    @GET("auth/logout")                    // 로그아웃
    fun logout(
    ): Call<String>//Call<VoidResponse>

    @GET("auth/user")                       // 유저 정보
    fun userInfo(): Call<UserInfo>
}