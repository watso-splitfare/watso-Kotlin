package com.example.saengsaengtalk.fragmentBaedal.adapterBaedal

import java.time.LocalDateTime

class BaedalComment(
    val nickname: String,
    val comment: String,
    val createdAt: LocalDateTime,
    // val postNum: Int,
    val order: Int,
    val depth: Int,
    val bundleId: Int,
    val writerId: String
    ) {}