package com.example.saengsaengtalk.APIS
import java.util.*

/** 201 배달 가게 목록 모델 */
data class StoreListModel(
    val store_id: String,
    val store_name: String,
    val fee: Int
)

/** 202 섹션 및 메뉴 모델 */
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

/** 203 그룹 및 옵션 모델 */
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

/** 204 배달 게시글 등록 모델 */
data class BaedalPostingModel(
    val store_id: String,
    val title: String,
    val content: String?,
    val order_time: String,
    val place: String,
    val min_member: Int?,
    val max_member: Int?
)

data class BaedalPostingResponse(
    val sucess: Boolean,
    val post_id: String
)

/** 205 배달 게시글 조회 모델 */
data class BaedalPostModel(
    val user: User,
    val _id: String,
    val store: Store,
    val title: String,
    val content: String?,
    val order_time: String,
    val place: String,
    val current_member: Int,
    val min_member: Int?,
    val max_member: Int?,
    val views: Int,
    val reg_date: String,
    val is_closed: Boolean,
    val order_users: List<OrderUser>,
    val comments: List<Comment>
)

data class Store(
    val _id: String,
    val store_name: String,
    val fee: Int,
    val min_order: Int
)

data class OrderUser(
    val user_id: Int,
    val nick_name: String,
    val orders: List<Order>
)

data class Order(
    val menu_name: String,
    val count: Int,
    val menu_price: Int,
    val sum_price: Int,
    val groups: List<Group>
)

data class Group(
    val group_name: String,
    val options: List<Option>
)

data class Option(
    val option_name: String,
    val option_price: Int
)

/** 209 배달 주문 등록 모델 */
data class OrderingModel(
    val store_id: String,
    val post_id: String,
    val orders: List<OrderingOrder>
)

data class OrderingOrder(
    val quantity: Int,
    val menu_name: String,
    val groups: List<OrderingGroup>
)

data class OrderingGroup(
    val group_id: Int,
    val options: List<Int>
)

data class OrderingResponse(
    val sucess: Boolean,
    val post_id: String
)

/** 213 배달 게시글 미리보기 모델 */
data class BaedalOrderListPreviewModel(
    val post_id: String,
    val views: Int,
    val liked: Int,
    val title: String,
    val order_time: Date,
    val store_name: String,
    val current_member: Int,
    val fee: Int,
    val is_closed: Boolean
)

data class TestModel(
    val a: Int,
    val inner1: List<Inner1>,
    val inner2: Inner2
)

data class Inner1(
    val b: Int
)

data class Inner2(
    val c: Int
)