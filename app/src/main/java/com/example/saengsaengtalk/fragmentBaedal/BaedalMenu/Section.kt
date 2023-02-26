package com.example.saengsaengtalk.fragmentBaedal.BaedalMenu

import com.example.saengsaengtalk.APIS.Menu

data class Section (
    val name: String,
    val menus: MutableList<Menu>
)