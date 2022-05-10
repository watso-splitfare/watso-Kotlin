package com.example.saengsaengtalk.adapterBaedal

import java.time.LocalDateTime

class BaedalList(
    val like: Int,
    val liked: Boolean,
    val viewed: Int,
    val title: String,
    val datetime: LocalDateTime,
    val shop: String,
    val member: Int,
    val fee:Int,
    val postNum: Int
    ) {}