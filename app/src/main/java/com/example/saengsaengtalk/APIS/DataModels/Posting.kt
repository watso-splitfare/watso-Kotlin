package com.example.saengsaengtalk.APIS

import java.util.*

/** 게시글 API 관련 데이터 모델 */

data class User(
    val user_id: Int,
    val nick_name: String
)

data class Comment(
    val comment_id: Int,
    val user_id: Int,
    val nick_name: String,
    val content: String,
    val depth: Int,
    val group: Int,
    val reg_date: String
)