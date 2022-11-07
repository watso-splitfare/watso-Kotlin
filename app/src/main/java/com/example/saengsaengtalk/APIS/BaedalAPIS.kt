package com.example.saengsaengtalk.APIS

import retrofit2.Call
import retrofit2.http.*

interface BaedalAPIS {
    /** 배달 게시물 관련 api */

    @GET("order/store/list")                // 201 가게 리스트 조회
    fun getStoreList(): Call<List<StoreListModel>>

    @GET("order/menu/list")                 // 202 메뉴 조회
    fun getSectionMenu(
        @Query("store_id") storeId: String
    ): Call<List<SectionMenuModel>>

    @GET("order/menu/detail")               // 203 옵션 조회
    fun getGroupOption(
        //@Query("menu_id") menuId: Int,      // 테스트용(실제 사용 x)
        @Query("menu_name") menuName: String,
        @Query("store_id") storeId: String
    ): Call<List<GroupOptionModel>>

    @POST("order/post/posting")             // 204 배달 게시글 등록
    fun baedalPosting(
        @Body jsonparams: BaedalPostingModel
    ): Call<BaedalPostingResponse>

    @GET("order/post/detail/{post_id}")     // 205 배달 게시글 조회
    fun getBaedalPost(
        @Path("post_id") postId: String
        //@Query("post_id") postId: Int,
    ): Call<BaedalPostModel>

    @PATCH("order/post/update")             // 206 배달 게시글 수정
    fun updateBaedalPost(
        @Body jsonparams: BaedalUpdateModel
    ): Call<BaedalPostingResponse>

    @PATCH("order/post/isClosed/condition-switch")    // 207 배달 주문 가능 여부 변경
    fun switchBaedalIsClosed(
        @Body jsonparams: Map<String, String>
    ): Call<IsClosedResponse>

    @POST("order/ordering")                 // 209 배달 주문 등록
    fun baedalOrdering(
        @Body jsonparams: OrderingModel
    ): Call<OrderingResponse>

    @GET("/order/ordering/update")          // 210 배달 주문 수정용 데이터 요청
    fun getOrders(
        @Query("post_id") postId: String
    ): Call<UserOrder>

    @PATCH("order/ordering/update")         // 210 배달 주문 수정
    fun baedalOrderUpdate(
        @Body jsonparams: OrderingModel
    ): Call<OrderingResponse>

    @PATCH("order/post/join/condition-switch")// 211 배달 그룹 참여 및 탈퇴
    fun switchBaedalJoin(
        @Body jsonparams: Map<String, String>
    ): Call<JoinResponse>

    @GET("order/post/list")                 // 213 배달 게시글 리스트 조회
    fun getBaedalOrderListPreview(
        //@Query("from_index") fromIndex: Int,
    ): Call<List<BaedalPostPreviewModel>>
}