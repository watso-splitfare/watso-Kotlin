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

    @GET("api/delivery/store/{store_id}/{menu_name}")   // 메뉴 상세정보(옵션) 조회
    fun getMenuInfo(
        @Path("store_id") storeId: String,
        @Path("menu_name") menuName: String
    ): Call<MenuInfo>


    /** 배달 게시글  */

    @GET("api/delivery/post")                 // 배달 게시글 목록 조회
    fun getBaedalPostList(
        @Query("filter") filter: String
    ): Call<List<BaedalPostPreview>>

    @POST("api/delivery/post")             // 배달 게시글 등록
    fun baedalPosting(
        @Body jsonparams: BaedalPosting
    ): Call<BaedalPostingResponse>

    @GET("api/delivery/post/{post_id}")     // 배달 게시글 조회
    fun getBaedalPost(
        @Path("post_id") postId: String
    ): Call<BaedalPost>

    @PATCH("api/delivery/post/{post_id}")             // 배달 게시글 수정
    fun updateBaedalPost(
        @Path("post_id") postId: String,
        @Body jsonparams: BaedalPosting
    ): Call<VoidResponse>

    @DELETE("api/delivery/post/{post_id}")     // 배달 게시글 삭제
    fun deleteBaedalPost(
        @Path("post_id") postId: String
    ): Call<VoidResponse>


    /** 주문 상태  */

    @PATCH("api/delivery/post/{post_id}/status/deliveryCompleted")
    fun completeBaedal(                                                // 배달 완료
        @Path("post_id") postId: String
    ): Call<VoidResponse>

    @PATCH("api/delivery/post/{post_id}/status/open")               // 참가 가능 여부 변경
    fun switchStatusBaedal(
        @Path("post_id") postId: String,
        @Body jsonparams: SwitchStatus
    ): Call<VoidResponse>

    @PATCH("api/delivery/post/{post_id}/status/orderCompleted")     // 대표자 주문 완료
    fun completeBaedalOrder(
        @Path("post_id") postId: String,
        @Body() jsonparams: OrderCompleted
    ): Call<VoidResponse>

    @PATCH("api/delivery/post/{post_id}/status/orderConfirm")       // 참여자 주문 확정
    fun confirmBaedalOrder(
        @Path("post_id") postId: String
    ): Call<VoidResponse>


    /** 주문 */

    @DELETE("api/delivery/post/{post_id}/userOrder")        // 게시글 참가 취소 및 전체 주문 삭제
    fun leaveBaedalGroup(
        @Path("post_id") postId: String,
    ): Call<VoidResponse>

    @POST("api/delivery/post/{post_id}/userOrder")          // 게시글 참가 및 주문 등록
    fun joinBaedalGroup(
        @Path("post_id") postId: String,
        @Body jsonparams: Ordering
    ): Call<VoidResponse>

    @GET("api/delivery/post/{post_id}/userOrder/order")     // 주문 목록 반환
    fun getBaedalOrder(
        @Path("post_id") postId: String
    ): Call<List<Order>>

    @POST("api/delivery/post/{post_id}/userOrder/order")    // 주문 추가
    fun addBaedalOrder(
        @Path("post_id") postId: String,
        @Body jsonparams: Ordering
    ): Call<VoidResponse>

    @PATCH("api/delivery/post/{post_id}/userOrder/order/{order_id}")  // 개별 주문 수정
    fun updateBaedalOrder(
        @Path("post_id") postId: String,
        @Path("order_id") orderId: String,
        @Body jsonparams: UpdateOrder
    ): Call<VoidResponse>

    @DELETE("api/delivery/post/{post_id}/userOrder/order/{order_id}") // 개별 주문 삭제
    fun deleteBaedalOrder(
        @Path("post_id") postId: String,
        @Path("order_id") orderId: String
    ): Call<VoidResponse>
}