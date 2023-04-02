package com.saengsaengtalk.app.APIS
import com.google.gson.annotations.SerializedName

/** 가게 목록 조회 모델*/
data class Store(
    val _id: String,
    val name: String,
    val fee: Int,
    @SerializedName("min_order")
    val minOrder: Int
)

/** 가게 상세 정보(메뉴) 조회 모델 */
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

/** 메뉴 상세 정보(옵션) 조회 모델 */
data class MenuInfo(
    val section: String,
    val name: String,
    val price: Int,
    val groups: List<Group>
)

data class Group(
    val _id: String,
    val name: String,
    @SerializedName("min_order_quantity")
    val minOrderQuantity: Int,
    @SerializedName("max_order_quantity")
    val maxOrderQuantity: Int,
    val options: List<Option>
)

data class Option(
    val _id: String,
    val name: String,
    val price: Int
)

/** 배달 게시글 등록 모델 */
data class BaedalPosting(
    @SerializedName("store_id")
    val storeId: String?,
    @SerializedName("order_time")
    val orderTime: String,
    val place: String,
    @SerializedName("min_member")
    val minMember: Int,
    @SerializedName("max_member")
    val maxMember: Int,
    val orders: List<OrderingOrder>?
)

data class BaedalPostingResponse(
    @SerializedName("post_id")
    val postId: String
)

/** 배달 게시글 조회 모델 */
data class BaedalPost(
    val _id: String,
    val title: String,
    @SerializedName("user_id")
    val userId: Long,
    val nickname: String,
    val place: String,
    @SerializedName("order_time")
    val orderTime: String,
    val store: Store,
    @SerializedName("open")
    val isOpen: Boolean,
    @SerializedName("order_completed")
    val orderCompleted: Boolean,
    @SerializedName("min_member")
    val minMember: Int,
    @SerializedName("max_member")
    val maxMember: Int,
    @SerializedName("user_orders")
    val userOrders: List<UserOrder>
)


data class UserOrder(
    @SerializedName("user_id")
    val userId: Long?,          // 데이터 조회는 하지만 어댑터 연결시에는 사용 X
    val nickname: String,
    @SerializedName("order_confirmation")
    val orderConfirmation: Boolean,
    val orders: List<Order>,
    var isMyOrder: Boolean = false     // 데이터 조회 X, 어댑터 연결시에만 사용
)

data class Order(
    val _id: String?,           // 데이터 조회는 하지만 어댑터 연결시에는 사용 X
    var quantity: Int,
    val price: Int,
    val menu: OrderMenu
)

data class OrderMenu(
    val name: String,
    val price: Int,
    val groups: List<OrderGroup>?
)

data class OrderGroup(
    val _id: String,
    val name: String?,                  /** 현재 API에 조회안됨, 수정요청 필요함*/
    val options: List<OrderOption>
)

data class OrderOption(
    val _id: String,
    val name: String,
    val price: Int
)

/** 207, 305 마감 여부 응답 모델 */
data class IsClosedResponse(
    val success: Boolean,
    val post_id: String,
    val is_closed: Boolean
)

/** 배달 주문 등록 모델 */
data class Ordering(
    val orders: List<OrderingOrder>
)

data class UpdateOrder(
    val order: OrderingOrder
)

data class OrderingOrder(
    val quantity: Int,
    val menu: OrderingMenu
)

data class OrderingMenu(
    val name: String,
    val groups: List<OrderingGroup>?
)

data class OrderingGroup(
    val _id: String,
    val options: List<String>
)

data class VoidResponse(
    val message: String?
)

/** 배달 게시글 목록 조회 모델 */
data class BaedalPostPreview(
    val _id: String,
    val title: String,
    //val user_id: Long,
    //val nick_name: String,
    //val place: String,
    @SerializedName("order_time")
    val orderTime: String,
    val store: Store,
    //val open: Boolean,
    //val order_completed,
    @SerializedName("user_orders")
    val userOrders: List<User>
)

data class SwitchStatus(
    val open: Boolean
)

data class OrderCompleted(
    @SerializedName("order_completed")
    val orderCompleted: Boolean
)