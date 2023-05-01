package com.saengsaengtalk.app.API

import retrofit2.Call
import retrofit2.http.*

interface BaedalAPI {

    companion object
    {
        const val STORE = "delivery/store"
        const val POST = "delivery/post"
        const val ORDER = "delivery/order"
    }

    /** 가게 정보 */

    @GET(STORE)              // 가게 리스트 조회
    fun getStoreList(): Call<List<Store>>

    @GET("${STORE}/{store_id}")   // 가게 상세정보(메뉴) 조회
    fun getStoreInfo(
        @Path("store_id") storeId: String
    ): Call<StoreInfo>

    @GET("${STORE}/{store_id}/{menu_id}")   // 메뉴 상세정보(옵션) 조회
    fun getMenuInfo(
        @Path("store_id") storeId: String,
        @Path("menu_id") menuId: String
    ): Call<Menu>


    /** 게시글  */

    @GET(POST)                   // 배달 게시글 목록 조회
    fun getBaedalPostList(
        @Query("option") option: String
    ): Call<List<BaedalPost>>

    @POST(POST)                  // 배달 게시글 등록
    fun baedalPosting(
        @Body jsonparams: BaedalPosting
    ): Call<BaedalPostingResponse>

    @GET("${POST}/{post_id}")         // 배달 게시글 조회
    fun getBaedalPost(
        @Path("post_id") postId: String
    ): Call<BaedalPost>

    @DELETE("${POST}/{post_id}")     // 배달 게시글 삭제
    fun deleteBaedalPost(
        @Path("post_id") postId: String
    ): Call<VoidResponse>

    @PATCH("${POST}/{post_id}")      // 배달 게시글 수정
    fun updateBaedalPost(
        @Path("post_id") postId: String,
        @Body jsonparams: BaedalPostUpdate
    ): Call<VoidResponse>

    @PATCH("${POST}/{post_id}/status")    // 배달 상태 관련 메서드
    fun setBaedalStatus(
        @Path("post_id") postId: String,
        @Body jsonparams: BaedalStatus
    ): Call<VoidResponse>

    /** 주문  */

    @GET("${ORDER}/{post_id}")        // 주문 조회
    fun getAllOrders(
        @Path("post_id") postId: String
    ): Call<AllOrderInfo>

    @POST("${ORDER}/{post_id}")       // 주문 작성
    fun postOrders(
        @Path("post_id") postId: String,
        @Body jsonparams: UserOrder
    ): Call<VoidResponse>

    @GET("${ORDER}/{post_id}/me")     // 내 주문 조회
    fun getMyOrders(
        @Path("post_id") postId: String
    ): Call<MyOrderInfo>

    @DELETE("${ORDER}/{post_id}/me")  // 내 주문 삭제
    fun deleteOrders(
        @Path("post_id") postId: String
    ): Call<VoidResponse>
}