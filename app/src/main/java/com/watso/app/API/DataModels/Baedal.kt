package com.watso.app.API
import com.google.gson.annotations.SerializedName

/** 가게 목록 조회 */
data class Store(
    val _id: String,
    val name: String,
    val fee: Int,
    @SerializedName("min_order")
    val minOrder: Int,
    @SerializedName("phone_number")
    val telNum: String,
    @SerializedName("logo_img_url")
    val logoImgUrl: String,
    val note: List<String>
)

/** 가게 상세 정보(메뉴) 조회  */
data class StoreInfo(
    val _id: String,
    val name: String,
    @SerializedName("min_order")
    val minOrder: Int,
    val fee: Int,
    @SerializedName("phone_number")
    val telNum: String,
    @SerializedName("logo_img_url")
    val logiImgUrl: String,
    val note: List<String>,
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
    val fee: Int,
    val store: Store,
    var status: String,
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

data class AccountNumber(
    @SerializedName("account_number")
    val AccountNumber: String
)

data class Fee(
    val fee: Int
)

/** 게시글 상태 설정 */
data class BaedalStatus(
    val status: String
)

/** 주문 조회 */
data class AllOrderInfo(
    @SerializedName("orders")
    val userOrders: MutableList<UserOrder>
)

data class MyOrderInfo(
    @SerializedName("order")
    val userOrder: UserOrder
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
) {
    fun getTotalPrice(): Int {
        var totalPrice = 0
        orders.forEach {
            it.setPrice()
            totalPrice += it.price!! * it.quantity
        }
        return totalPrice
    }
}

/** 주문 등록 */
data class PostOrder(
    @SerializedName("order_lines")
    val orders: MutableList<Order>
)

data class Order(
    var quantity: Int,
    var price: Int?,
    val menu: Menu
) {
    fun setPrice(){
        var tempPrice = menu.price
        menu.groups?.forEach { it.options?.forEach { tempPrice += it.price } }
        price = tempPrice
    }
}

data class VoidResponse(
    val message: String?
)