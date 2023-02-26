package com.example.saengsaengtalk.APIS

import retrofit2.Call
import retrofit2.http.*

interface BaedalAPIS {
    /** 배달 게시물 관련 api */

    /** 가게 리스트 조회 */
    @GET("delivery/store")
    fun getStoreList(): Call<List<Store>>

    /** 가게 상세정보(메뉴) 조회 */
    @GET("delivery/store/{store_id}")
    fun getStoreInfo(
        @Path("store_id") storeId: String
    ): Call<StoreInfo>

    /** 메뉴 상세정보(옵션) 조회 */
    @GET("delivery/store/{store_id}/{menu_name}")
    fun getMenuInfo(
        @Path("store_id") storeId: String,
        @Path("menu_name") menuName: String
    ): Call<MenuInfo>

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
    ): Call<UserOrder?>

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