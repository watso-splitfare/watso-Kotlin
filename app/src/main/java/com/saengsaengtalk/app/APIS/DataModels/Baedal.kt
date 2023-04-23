package com.saengsaengtalk.app.APIS
import com.google.gson.annotations.SerializedName

/** 가게 목록 조회 */
data class Store(
    val _id: String,
    val name: String,
    val fee: Int,
    @SerializedName("min_order")
    val minOrder: Int
)

/** 가게 상세 정보(메뉴) 조회  */
data class StoreInfo(
    val _id: String,
    val name: String,
    @SerializedName("min_order")
    val minOrder: Int,
    val fee: Int,
    val sections: MutableList<Section>
)

data class Section(
    @SerializedName("section_name")
    val name: String,
    val menus: List<SectionMenu>
)

data class SectionMenu(
    val _id: String,
    val name: String,
    val price: Int
)

/** 메뉴 상세 정보(옵션) 조회 */
data class Menu(
    val _id: String,
    val name: String,
    val price: Int,
    val groups: List<Group>?
)

data class Group(
    val _id: String,
    val name: String,
    @SerializedName("min_order_quantity")
    val minOrderQuantity: Int,
    @SerializedName("max_order_quantity")
    val maxOrderQuantity: Int,
    val options: List<Option>?
)

data class Option(
    val _id: String,
    val name: String,
    val price: Int
)

/** 게시글 조회  */
data class BaedalPost(
    val _id: String,
    val title: String,
    @SerializedName("user_id")
    val userId: Long,
    val nickname: String,
    val place: String,
    @SerializedName("min_member")
    val minMember: Int,
    @SerializedName("max_member")
    val maxMember: Int,
    @SerializedName("order_time")
    val orderTime: String,
    val store: Store,
    val status: String,
    val users: List<Long>
)

/** 게시글 등록 */
data class BaedalPosting(
    @SerializedName("store_id")
    val storeId: String,
    @SerializedName("order_time")
    val orderTime: String,
    val place: String,
    @SerializedName("min_member")
    val minMember: Int,
    @SerializedName("max_member")
    val maxMember: Int,
    var order: UserOrder?
)

data class BaedalPostingResponse(
    @SerializedName("post_id")
    val postId: String
)

/** 게시글 업데이트 */
data class BaedalPostUpdate(
    @SerializedName("order_time")
    val orderTime: String,
    val place: String,
    @SerializedName("min_member")
    val minMember: Int,
    @SerializedName("max_member")
    val maxMember: Int
)

/** 게시글 상태 설정 */
data class BaedalStatus(
    val status: String
)

/** 주문 조회 */
data class OrderInfo(
    @SerializedName("orders")
    val userOrders: MutableList<UserOrder>
)

data class UserOrder(
    @SerializedName("user_id")
    val userId: Long?,
    val nickname: String,
    @SerializedName("request_comment")
    var requestComment: String,
    @SerializedName("order_lines")
    val orders: MutableList<Order>,
    var isMyOrder: Boolean?
)

/** 주문 등록 */
data class PostOrder(
    @SerializedName("order_lines")
    val orders: MutableList<Order>
)

data class Order(
    var quantity: Int,
    var price: Int?,
    val menu: Menu
)

data class VoidResponse(
    val message: String?
)