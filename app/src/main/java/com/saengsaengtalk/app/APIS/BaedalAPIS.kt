package com.saengsaengtalk.app.APIS

import retrofit2.Call
import retrofit2.http.*

interface BaedalAPIS {
    /** 배달 게시물 관련 api */

    @GET("api/delivery/post")                 // 배달 게시글 목록 조회
    fun getBaedalPostList(
    ): Call<List<BaedalPostPreview>>

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

    @PATCH("api/delivery/post/{post_id}/join")  // 배달 그룹 참여
    fun baedalJoin(
        @Path("post_id") postId: String
    ): Call<VoidResponse>

    @POST("api/delivery/post/{post_id}/order")                 // 배달 주문 등록
    fun baedalOrdering(
        @Path("post_id") postId: String,
        @Body jsonparams: Ordering
    ): Call<VoidResponse>

    @GET("api/delivery/post/{post_id}/order")          // 배달 주문 수정용 데이터 요청
    fun getOrders(
        @Path("post_id") postId: String
    ): Call<UserOrder>

    @PATCH("api/delivery/post/{post_id}/order")                     // 참여자 주문 확정
        fun baedalOrderConfirm(
        @Path("post_id") postId: String
    ): Call<VoidResponse>

    @DELETE("api/delivery/post/{post_id}/order/{order_id}")        // 주문 삭제
        fun baedalOrderDelete(
        @Path("post_id") postId: String,
        @Path("order_id") orderId: String
    ): Call<VoidResponse>

    @PATCH("api/delivery/post/{post_id}/order/{order_id}")         // 배달 주문 수정
    fun baedalOrderUpdate(
        @Path("post_id") postId: String,
        @Body jsonparams: Ordering
    ): Call<VoidResponse>

    @PATCH("api/delivery/post/{post_id}/quit")    // 그룹 탈퇴
    fun baedalLeaveGroup(
        @Path("post_id") postId: String
    ): Call<VoidResponse>

    @PATCH("api/delivery/post/{post_id}/status/open")    // 주문 가능 여부 변경
    fun baedalSwitchStatus(
        @Path("post_id") postId: String,
        @Body jsonparams: SwitchStatus
    ): Call<VoidResponse>

    @PATCH("api/delivery/post/{post_id}/status/orderCompleted") // 대표자 주문 완료
    fun baedalOrderCompleted(
        @Path("post_id") postId: String,
        @Body() jsonparams: OrderCompleted
    ): Call<VoidResponse>
}