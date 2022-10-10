package com.example.saengsaengtalk.APIS

import com.example.saengsaengtalk.fragmentBaedal.BaedalOrder
import java.util.*

/** 배달 가게 목록 모델 */
data class StoreListModel(
    val store_id: Int,
    val store_name: String,
    val fee: Int
)

/** 섹션 및 메뉴 모델 */
data class SectionMenuModel(
    val section_id: Int,
    val section_name: String,
    val menu_list: List<MenuModel>
)

data class MenuModel(
    val menu_id: Int,
    val menu_name: String,
    val menu_price: Int
)

/** 그룹 및 옵션 모델 */
data class GroupOptionModel(
    val group_id: Int,
    val group_name: String,
    val min_orderable_quantity: Int,
    val max_orderable_quantity: Int,
    val option_list: List<OptionModel>
)

data class OptionModel(
    val option_id: Int,
    val option_name: String,
    val option_price: Int
)

/** 배달 게시글 모델 */
data class BaedalPost(
    val user: User,
    val post: BaedalPostContent,
    val comments: List<Comment>
)

data class BaedalPostContent(
    val post_id: Int,
    val store_id: Int,
    val store_name: String,
    val fee: Int,
    val title: String,
    val content: String?,
    val order_time: Date,
    val place: String,
    val current_member: Int,
    val min_member: Int?,
    val max_member: Int?,
    val views: Int,
    val reg_date: Date,
    val is_closed: Boolean,
    val order_users: List<OrderUser>
)

data class OrderUser(
    val user_id: Int,
    val nick_name: String,
    val orders: List<BaedalOrder>
)

/** 배달 게시글 미리보기 모델 */
data class BaedalOrderListPreviewModel(
    val post_id: Int,
    val views: Int,
    val liked: Int,
    val title: String,
    val order_time: Date,
    val store_name: String,
    val current_member: Int,
    val fee: Int,
    val is_closed: Boolean
)