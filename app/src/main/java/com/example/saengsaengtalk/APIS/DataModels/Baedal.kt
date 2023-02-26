package com.example.saengsaengtalk.APIS
import com.google.gson.annotations.SerializedName
import java.util.*


data class Store(
    val _id: String,
    val name: String,
    val fee: Int,
    @SerializedName("min_order")
    val minOrder: Int,
    val message: String?
)

/** 201 배달 가게 목록 모델 */
/*data class StoreListModel(
    val store_id: String,
    val store_name: String,
    val fee: Int
)*/

/** 202 섹션 및 메뉴 모델 */
/*data class SectionMenuModel(
    //val section_id: Int,
    val section_name: String,
    val menus: List<MenuModel>
)

data class MenuModel(
    //val menu_id: Int,
    val menu_name: String,
    val menu_price: Int
)*/
data class StoreInfo(
    val _id: String,
    val name: String,
    @SerializedName("min_order")
    val minOrder: Int,
    val fee: Int,
    val menus: List<Menu>
)

data class Menu(
    val section: String,
    val name: String,
    val price: Int
)

/** 203 그룹 및 옵션 모델 */
/*data class GroupOptionModel(
    val group_id: String,
    val group_name: String,
    val min_orderable_quantity: Int,
    val max_orderable_quantity: Int,
    val options: List<OptionModel>
)

data class OptionModel(
    val option_id: String,
    val option_name: String,
    val option_price: Int
)*/

data class MenuInfo(
    val section: String,
    val name: String,
    val price: Int,
    val groups: List<Group>
)

data class Group(
    val _id: String,
    val name: String,
    val min_order_quantity: Int,
    val max_order_quantity: Int,
    val options: List<Option>
)

data class Option(
    val _id: String,
    val name: String,
    val price: Int
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
    val success: Boolean,
    val post_id: String
)

/** 205 배달 게시글 조회 모델 */
data class BaedalPostModel(
    val user_id: Long,
    val nick_name: String,
    val _id: String,
    val join_users: List<Long>,
    val store: Store,
    val title: String,
    val content: String?,
    val order_time: String,
    val place: String,
    //val current_member: Int,
    val min_member: Int?,
    val max_member: Int?,
    val views: Int,
    val update_date: String,
    val is_closed: Boolean,
    //val is_member: Boolean,
    val user_orders: List<UserOrder>?,
    //val comments: List<Comment>
)


data class UserOrder(
    val user_id: Long,
    val nick_name: String,
    val orders: List<Order>
)

data class Order(
    val menu_name: String,
    val quantity: Int,
    val menu_price: Int,
    val sum_price: Int,
    val groups: List<Group>
)

/*data class Group(
    val group_id: String,
    val group_name: String,
    val options: List<Option>
)

data class Option(
    val option_id: String,
    val option_name: String,
    val option_price: Int
)*/

/** 206 배달 게시글 수정 모델 */
data class BaedalUpdateModel(
    val post_id: String,
    val title: String,
    val content: String?,
    val order_time: String,
    val place: String,
    val min_member: Int?,
    val max_member: Int?
)

/** 207, 305 마감 여부 응답 모델 */
data class IsClosedResponse(
    val success: Boolean,
    val post_id: String,
    val is_closed: Boolean
)

/** 209 배달 주문 등록 모델 */
data class OrderingModel(
    val store_id: String,
    val post_id: String?,
    val orders: List<OrderingOrder>
)

data class OrderingOrder(
    val quantity: Int,
    val menu_name: String,
    val groups: List<OrderingGroup>
)

data class OrderingGroup(
    val group_id: String,
    val options: List<String>
)

data class OrderingResponse(
    val success: Boolean,
    val post_id: String
)

/** 211, 306 그룹 참가 응답 모델 */
data class JoinResponse(
    val post_id: String,
    val success: Boolean,
    val join: Boolean
)

/** 213 배달 게시글 미리보기 모델 */
data class BaedalPostPreviewModel(
    val _id: String,
    //val user_id: Long,
    val join_users: List<Long>,
    //val nick_name: String,
    val store: Store,
    val title: String,
    //val place: String,
    val order_time: String,
    //val min_member: Int,
    //val max_member: Int,
    //val update_date: String,
    //val views: Int,
    val is_closed: Boolean
)