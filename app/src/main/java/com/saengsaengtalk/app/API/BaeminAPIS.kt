package com.saengsaengtalk.app.API

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


interface BaeminAPIS {
    @GET("https://shopdp-api.baemin.com/v2/BAEMIN/shops?latitude=35.4831&longitude=128.753")
    fun getShopList(

    ): Call<JsonObject>

    @GET("v1/shops/{shop_id}/menus/{menu_id}")
    fun getMenuDetail(
        @Path("shop_id") shopId: Int,
        @Path("menu_id") menuId: Int
    ): Call<JsonObject>

    companion object {
        private const val BASE_URL = "https://shopdp-api.baemin.com/"

        fun create(): BaeminAPIS {

            val gson :Gson = GsonBuilder().setLenient().create();

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(BaeminAPIS::class.java)
        }
    }
}
