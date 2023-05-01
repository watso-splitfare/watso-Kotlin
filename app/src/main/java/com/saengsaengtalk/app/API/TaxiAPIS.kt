package com.saengsaengtalk.app.API

import retrofit2.Call
import retrofit2.http.*

interface TaxiAPIS {
    @POST("taxi/post/posting")                      // 302 택시 게시글 등록
    fun taxiPosting(
        @Body jsonparams: TaxiPostingModel
    ): Call<PostingResponse>

    @GET("taxi/post/detail/{post_id}")                       // 303 택시 게시글 조회
    fun getTaxiPost(
        @Path("post_id") postId: String
        //@Query("post_id") postId: String
    ): Call<TaxiPostModel>

    @GET("taxi/post/delete")                // 304 택시 게시글 삭제
    fun delTaxiPost(
        @Query("post_id") postId: String
    ): Call<PostingResponse>

    /*@PATCH("/taxi/post/isClosed/condition-switch")                     // 305 동승 가능 여부 변경
    fun switchTaxiIsClosed(
        @Body jsonparams: Map<String, String>
    ): Call<IsClosedResponse>*/

    @PATCH("/taxi/post/join/condition-switch")                  // 306 동승 신청/취소
    fun switchTaxiJoin(
        @Body jsonparams: Map<String, String>
    //@Query("post_id") postId: String
    ): Call<VoidResponse>

    @GET("/taxi/post/list")                      // 307 택시 게시글 리스트 조회
    fun getTaxiPostListPreview(
    ): Call<List<TaxiPostPreviewModel>>
}