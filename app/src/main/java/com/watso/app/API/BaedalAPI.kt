package com.watso.app.API

import retrofit2.Call
import retrofit2.http.*

interface BaedalAPI {

    companion object
    {
        const val STORE = "delivery/store"
        const val POST = "delivery/post"
    }


    /** 가게  */

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

    @GET(POST)                   // 게시글 목록 조회
    fun getBaedalPostList(
        @Query("option") option: String
    ): Call<List<BaedalPost>>

    @POST(POST)                  // 게시글 등록
    fun baedalPosting(
        @Body jsonparams: BaedalPosting
    ): Call<BaedalPostingResponse>

    @GET("${POST}/{post_id}")         // 게시글 조회
    fun getBaedalPost(
        @Path("post_id") postId: String
    ): Call<BaedalPost>

    @DELETE("${POST}/{post_id}")     // 게시글 삭제
    fun deleteBaedalPost(
        @Path("post_id") postId: String
    ): Call<VoidResponse>

    @PATCH("${POST}/{post_id}")      // 게시글 수정
    fun updateBaedalPost(
        @Path("post_id") postId: String,
        @Body jsonparams: BaedalPostUpdate
    ): Call<VoidResponse>

    @GET("${POST}/{post_id}/account-number")    // 대표자 계좌번호 조회
    fun getAccountNumber(
        @Path("post_id") postId: String,
    ): Call<AccountNumber>

    @PATCH("${POST}/{post_id}/fee")     // 배달비 수정
    fun updateBaedalFee(
        @Path("post_id") postId: String,
        @Body jsonparams: Fee
    ): Call<VoidResponse>

    @PATCH("${POST}/{post_id}/status")    // 게시글 상태 변경
    fun setBaedalStatus(
        @Path("post_id") postId: String,
        @Body jsonparams: BaedalStatus
    ): Call<VoidResponse>


    /** 주문  */

    @GET("${POST}/{post_id}/orders")        // 주문 조회
    fun getAllOrders(
        @Path("post_id") postId: String
    ): Call<AllOrderInfo>

    @POST("${POST}/{post_id}/orders")       // 주문 작성
    fun postOrders(
        @Path("post_id") postId: String,
        @Body jsonparams: UserOrder
    ): Call<VoidResponse>

    @GET("${POST}/{post_id}/orders/me")     // 내 주문 조회
    fun getMyOrders(
        @Path("post_id") postId: String
    ): Call<MyOrderInfo>

    @DELETE("${POST}/{post_id}/orders/me")  // 내 주문 삭제
    fun deleteOrders(
        @Path("post_id") postId: String
    ): Call<VoidResponse>


    /** 댓글 */

    @POST("${POST}/{post_id}/comments")         // 댓글 작성
    fun postComment(
        @Path("post_id") postId: String,
        @Body jsonparams: PostComment
    ): Call<VoidResponse>

    @GET("${POST}/{post_id}/comments")          // 댓글 조회
    fun getComments(
        @Path("post_id") postId: String
    ): Call<GetComments>

    @POST("${POST}/{post_id}/comments/{comment_id}")    // 대댓글 작성
    fun postSubComment(
        @Path("post_id") postId: String,
        @Path("comment_id") commentId: String,
        @Body jsonparams: PostComment
    ): Call<VoidResponse>

    @DELETE("${POST}/{post_id}/comments/{comment_id}")  // 댓글 삭제
    fun deleteComment(
        @Path("post_id") postId: String,
        @Path("comment_id") commentId: String
    ): Call<VoidResponse>
}