package com.example.saengsaengtalk.APIS

import retrofit2.Call
import retrofit2.http.*

interface AuthAPIS {
    /** 계정 관련 API */

    @GET("auth/check/overlap/username")     // 100 아이디 중복조회
    fun usernameOverlapCheck(
        @Query("user_name") username: String
    ): Call<OverlapResult>

    @GET("auth/check/overlap/studentnum")   // 101 학번 중복조회
    fun studentnumOverlapCheck(
        @Query("studen_tnum") studentnum: Int
    ): Call<OverlapResult>

    @GET("auth/check/overlap/nickname")     // 102 닉네임 중복조회
    fun nicknameOverlapCheck(
        @Query("nick_name") nickname: String
    ): Call<OverlapResult>

    @POST("auth/signup")                    // 103 회원가입
    //@Headers("accept: application/json", "content-type: application/json")
    fun signup(
        @Body jsonparams: SignUpModel
    ): Call<SignUpResult>

    @POST("auth/login")                     // 104 로그인
    //@Headers("accept: application/json", "content-type: application/json")
    fun login(
        @Body jsonparams: LoginModel
    ): Call<LoginResult>

    @POST("auth/logout")                    // 105 로그아웃
    fun logout(
    ): Call<LogoutResult>
}