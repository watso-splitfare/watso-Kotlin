package com.saengsaengtalk.app.API

import retrofit2.Call
import retrofit2.http.*

interface AuthAPI {
    /** 인증 관련 API */

    @POST("auth/login")        // 로그인
    fun login(
        @Body jsonparams: LoginModel
    ): Call<VoidResponse>

    @DELETE("auth/logout")         // 로그아웃
    fun logout(
    ): Call<VoidResponse>
}