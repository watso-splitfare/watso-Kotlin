package com.example.saengsaengtalk.APIS

import retrofit2.Call
import retrofit2.http.*

interface AuthAPIS {
    /** 계정 관련 API */

    @GET("auth/signup/duplication/username")     // 100 아이디 중복조회
    fun usernameDuplicationCheck(
        @Query("username") username: String
    ): Call<DuplicationResult>

    @GET("auth/signup/duplication/studentnum")   // 101 학번 중복조회
    fun studentnumDuplicationCheck(
        @Query("studentnum") studentnum: String
    ): Call<DuplicationResult>

    @GET("auth/signup/duplication/nickname")     // 102 닉네임 중복조회
    fun nicknameDuplicationCheck(
        @Query("nickname") nickname: String
    ): Call<DuplicationResult>

    @POST("auth/signup")                    // 103 회원가입
    fun signup(
        @Body jsonparams: SignUpModel
    ): Call<SignUpResult>

    @POST("auth/signin")                     // 104 로그인
    fun login(
        @Body jsonparams: LoginModel
    ): Call<LoginResult>

    @GET("auth/logout")                    // 105 로그아웃
    fun logout(
        @Header("Authorization") refreshToken: String
    ): Call<LogoutResult>
}