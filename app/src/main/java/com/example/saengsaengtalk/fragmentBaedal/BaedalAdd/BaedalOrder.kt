package com.example.saengsaengtalk.fragmentBaedal.BaedalAdd

import com.example.saengsaengtalk.fragmentBaedal.BaedalPost.BaedalOrderOpt

class BaedalOrder(
    val menuName: String,
    val count: Int,
    val opt: MutableList<BaedalOrderOpt>
    ) {}