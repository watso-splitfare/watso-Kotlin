package com.example.saengsaengtalk.APIS

import java.util.*

/** 게시글 API 관련 데이터 모델 */

data class User(
    val user_id: Long,
    val nick_name: String
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