package com.saengsaengtalk.app.APIS

import retrofit2.Call
import retrofit2.http.*

interface BaedalAPIS {

    /** 가게 정보 */

    @GET("api/delivery/store")              // 가게 리스트 조회
    fun getStoreList(): Call<List<Store>>

    @GET("api/delivery/store/{store_id}")   // 가게 상세정보(메뉴) 조회
    fun getStoreInfo(
        @Path("store_id") storeId: String
    ): Call<StoreInfo>

    @GET("api/delivery/store/{store_id}/{menu_id}")   // 메뉴 상세정보(옵션) 조회
    fun getMenuInfo(
        @Path("store_id") storeId: String,
        @Path("menu_id") menuId: String
    ): Call<Menu>


    /** 게시글  */

    @GET("api/delivery/post")                   // 배달 게시글 목록 조회
    fun getBaedalPostList(
        @Query("option") option: String
    ): Call<List<BaedalPost>>

    @POST("api/delivery/post")                  // 배달 게시글 등록
    fun baedalPosting(
        @Body jsonparams: BaedalPosting
    ): Call<BaedalPostingResponse>

    @GET("api/delivery/post/{post_id}")         // 배달 게시글 조회
    fun getBaedalPost(
        @Path("post_id") postId: String
    ): Call<BaedalPost>

    @DELETE("api/delivery/post/{post_id}")     // 배달 게시글 삭제
    fun deleteBaedalPost(
        @Path("post_id") postId: String
    ): Call<VoidResponse>

    @PATCH("api/delivery/post/{post_id}")      // 배달 게시글 수정
    fun updateBaedalPost(
        @Path("post_id") postId: String,
        @Body jsonparams: BaedalPostUpdate
    ): Call<VoidResponse>

    @PATCH("api/delivery/post/{post_id}/delivered") // 배달 완료 명시
    fun baedalComplete(
        @Path("post_id") postId: String
    ): Call<VoidResponse>

    @PATCH("api/delivery/post/{post_id}/status")    // 배달 상태 관련 메서드
    fun setBaedalStatus(
        @Path("post_id") postId: String,
        @Body jsonparams: BaedalStatus
    ): Call<VoidResponse>

    /** 주문  */

    @GET("api/delivery/order/{post_id}")        // 주문 조회
    fun getOrders(
        @Path("post_id") postId: String
    ): Call<OrderInfo>

    @POST("api/delivery/order/{post_id}")       // 주문 작성
    fun postOrders(
        @Path("post_id") postId: String,
        @Body jsonparams: UserOrder
    ): Call<VoidResponse>

    @GET("api/delivery/order/{post_id}/me")     // 내 주문 조회
    fun getMyOrders(
        @Path("post_id") postId: String
    ): Call<OrderInfo>

    @DELETE("api/delivery/order/{post_id}/me")  // 내 주문 삭제
    fun deleteOrders(
        @Path("post_id") postId: String
    ): Call<VoidResponse>
}