package com.example.saengsaengtalk.APIS

import java.util.*

/** 게시글 API 관련 데이터 모델 */

data class StoreAdd(
    val store_name: String,
    val fee: Int,
    val min_order: Int
)

data class MenuAdd(
    val store_id: String,
    val section_name: String,
    val menu_name: String,
    val menu_price: Int
)

data class GroupsAdd(
    val store_id: String,
    val section_name: String,
    val menu_name: String,
    val groups: List<GroupAdd>
)

data class GroupAdd(
    val group_name: String,
    val min_orderable_quantity: Int,
    val max_orderable_quantity: Int
)

data class OptionsAdd(
    val store_id: String,
    val section_name: String,
    val menu_name: String,
    val group_id: Long,
    val options: List<OptionAdd>
)

data class OptionAdd(
    val option_name: String,
    val option_price: Int
)