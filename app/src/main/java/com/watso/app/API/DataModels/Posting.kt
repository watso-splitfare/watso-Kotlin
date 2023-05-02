package com.watso.app.API

import com.google.gson.annotations.SerializedName

/** 게시글 API 관련 데이터 모델 */

data class User(
    @SerializedName("user_id")
    val userId: Long,
    val nickname: String
)

data class Comment(
    val comment_id: Long,
    val user_id: Long,
    val nick_name: String,
    val content: String,
    val depth: Int,
    val group: Int,
    val update_date: String
)

data class PostingResponse(
    val post_id: String,
    val success: Boolean
)