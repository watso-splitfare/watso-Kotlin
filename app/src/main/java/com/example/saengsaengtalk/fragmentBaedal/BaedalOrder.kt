package com.example.saengsaengtalk.fragmentBaedal


data class BaedalOrder (
    val count: Int,
    val menuName: String,
    val menuId: Int,
    val menu_price: Int,
    val options: List<Options>
)

data class Options(
    val groupName: String,
    val groupId: Int,
    val option: List<Option>
)

data class Option(
    val optionName: String,
    val optionId: Int,
    val optionPrice: Int
)