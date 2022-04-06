package com.example.saengsaengtalk.adapterOthers

import java.time.LocalDateTime

class BaedalList(
    val like: Int,
    val liked: Boolean,
    val viewed: Int,
    val title: String,
    val datetime: LocalDateTime,
    val shop: String,
    val member: Int,
    val fee:Int
    ) {}