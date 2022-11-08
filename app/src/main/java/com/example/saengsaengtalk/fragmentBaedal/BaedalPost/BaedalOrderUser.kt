package com.example.saengsaengtalk.fragmentBaedal.BaedalPost

import com.example.saengsaengtalk.fragmentBaedal.BaedalOrder

class BaedalOrderUser(
    val nickName: String,
    val price: String,
    val menuList: MutableList<BaedalOrder>,
    val isMyOrder: Boolean=false
    ) {}