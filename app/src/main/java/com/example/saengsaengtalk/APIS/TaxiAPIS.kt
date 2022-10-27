package com.example.saengsaengtalk.APIS

import com.example.saengsaengtalk.APIS.DataModels.TaxiPostPreviewModel
import retrofit2.Call
import retrofit2.http.GET

interface TaxiAPIS {
    @GET("taxi/posts")                      // 213 택시 게시글 리스트 조회
    fun getTaxiPostListPreview(
    ): Call<List<TaxiPostPreviewModel>>
}