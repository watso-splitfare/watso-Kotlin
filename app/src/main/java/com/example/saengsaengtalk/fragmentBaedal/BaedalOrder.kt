package com.example.saengsaengtalk.fragmentBaedal


data class BaedalOrder (
    var count: Int,
    //val menuId: Int?,
    val menuName: String,
    val menuPrice: Int,
    val sumPrice: Int,
    val groups: List<Group>
)

data class Group(
    val groupId: Long?,
    val groupName: String,
    val options: List<Option>
)

data class Option(
    val optionId: Long?,
    val optionName: String,
    val optionPrice: Int
)