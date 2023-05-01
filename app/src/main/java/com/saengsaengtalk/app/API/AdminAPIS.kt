package com.saengsaengtalk.app.API

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

interface AdminAPIS {
    /** 어드민 API */

    @POST("admin/delivery/add/store")        // 가게 추가
    fun addStore(
        @Body jsonparams: StoreAdd
    ): Call<JsonObject>

    @POST("admin/delivery/add/menu")         // 메뉴 추가
    fun addMenu(
        @Body jsonparams: MenuAdd
    ): Call<JsonObject>

    @POST("admin/delivery/add/group")        // 그룹 추가
    fun addGroup(
        @Body jsonparams: GroupsAdd
    ): Call<JsonObject>

    @POST("admin/delivery/add/option")      // 옵션 추가
    fun addOption(
        @Body jsonparams: OptionsAdd
    ): Call<JsonObject>

}