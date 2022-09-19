package com.example.saengsaengtalk.fragmentBaedal.Baedal

import java.time.LocalDateTime

class BaedalList(
    val likeUserList: ArrayList<String>,
    val viewed: Int,
    val title: String,
    val baedaltime: LocalDateTime,
    val shop: String,
    val member: Int,
    val fee:Int,
    val postNum: Int
    ) {}