package com.example.saengsaengtalk.APIS

import com.example.saengsaengtalk.APIS.DataModels.*
import retrofit2.Call
import retrofit2.http.*

interface TaxiAPIS {
    @POST("taxi/post")                      // 302 택시 게시글 등록
    fun taxiPosting(
        @Body jsonparams: TaxiPostingModel
    ): Call<PostingResponse>

    @GET("taxi/post")                       // 303 택시 게시글 조회
    fun getTaxiPost(
        //@Path("post_id") postId: String
        @Query("post_id") postId: String
    ): Call<TaxiPostModel>

    @GET("taxi/post/delete")                // 304 택시 게시글 삭제
    fun delTaxiPost(
        @Query("post_id") postId: String
    ): Call<PostingResponse>

    @PATCH("taxi/post")                     // 305 동승 가능 여부 변경
    fun switchCondition(
        @Body jsonparams: Map<String, String>
    ): Call<TaxiSwitchConditionResponse>

    @GET("taxi/post/join")                  // 306 동승 신청/취소
    fun taxiJoin(
        @Query("post_id") postId: String
    ): Call<TaxiJoinResponse>

    @GET("taxi/posts")                      // 307 택시 게시글 리스트 조회
    fun getTaxiPostListPreview(
    ): Call<List<TaxiPostPreviewModel>>
}