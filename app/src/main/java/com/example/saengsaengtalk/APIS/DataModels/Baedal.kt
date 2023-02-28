package com.example.saengsaengtalk.APIS
import com.google.gson.annotations.SerializedName
import java.util.*


data class Store(
    val _id: String,
    val name: String,
    val fee: Int,
    @SerializedName("min_order")
    val minOrder: Int
)

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
data class BaedalPost(          // 배달 게시글 조회 모델
    val _id: String,
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("join_users")
    val joinUsers: List<Long>,
    @SerializedName("nick_name")
    val nickName: String,
    val store: Store,
    val title: String,
    val content: String,
    val place: String,
    @SerializedName("order_time")
    val orderTime: String,
    @SerializedName("min_member")
    val minMember: Int,
    @SerializedName("max_member")
    val maxMember: Int,
    @SerializedName("update_date")
    val updateDate: String,
    val open: Boolean,
    @SerializedName("user_orders")
    val userOrders: List<UserOrder>
)

data class UserOrder(
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("nick_name")
    val nickName: String,
    val orders: List<Order>,
    val isMyOrder: Boolean?     // 데이터 전송 X, 어댑터 연결시 사용
)

data class Order(
    val _id: String,
    val quantity: Int,
    val menu_price: Int,
    val sum_price: Int,
    val menu: OrderMenu
)

data class OrderMenu(
    val name: String,
    val groups: List<OrderGroup>
)

data class OrderGroup(
    val _id: String,
    val name: String,
    val options: List<OrderOption>
)

data class OrderOption(
    val _id: String,
    val name: String,
    val price: Int
)

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
data class BaedalPostPreview(
    val _id: String,
    val title: String,
    //val user_id: Long,
    val join_users: List<Long>,
    //val nick_name: String,
    val store: Store,
    val order_time: String
)