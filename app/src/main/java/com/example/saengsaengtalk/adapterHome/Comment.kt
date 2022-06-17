package com.example.saengsaengtalk.adapterHome

import java.time.LocalDateTime

class Comment(
    val nickname: String,
    val comment: String,
    val createdAt: LocalDateTime,
    // val postNum: Int,
    val order: Int,
    val depth: Int,
    val bundleId: Int,
    val writerId: String
    ) {}