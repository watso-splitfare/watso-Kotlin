package com.watso.app.fragmentFreeBoard.adapterFB

import java.time.LocalDateTime

class PostInList(
    val postNum: Int,
    val title: String,
    val writer: String,
    val likeUserList: Array<String>,
    val commentCount: Int,
    val createdAt: LocalDateTime
    ) { }