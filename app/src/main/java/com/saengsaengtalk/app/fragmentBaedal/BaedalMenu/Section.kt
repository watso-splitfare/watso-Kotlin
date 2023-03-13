package com.saengsaengtalk.app.fragmentBaedal.BaedalMenu

import com.saengsaengtalk.app.APIS.Menu

data class Section (
    val name: String,
    val menus: MutableList<Menu>
)