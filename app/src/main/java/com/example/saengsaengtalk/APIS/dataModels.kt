package com.example.saengsaengtalk.APIS


data class OverlapResult(
    val result: Boolean
)

data class SignUpResult(
    val result: Boolean
)

data class SignUpModel(
    val user_name: String,
    val pw: String,
    val student_num: Int,
    val nick_name: String
)

data class LoginModel(
    val user_name: String,
    val pw: String
)

data class LoginResult(
    val result: Boolean,
    val code: Int
)

data class LogoutResult(
    val result: Boolean,
    val code: Int
)

/** 배달 API 관련 데이터 모델 */

data class StoreListModel(
    val store_id: Int,
    val store_name: String,
    val fee: Int
)

data class MenuModel(
    val menu_id: Int,
    val menu_name: String,
    val menu_price: Int
)

data class SectionMenuModel(
    val section_id: Int,
    val section_name: String,
    val menu_list: List<MenuModel>
)

data class OptionModel(
    val option_id: Int,
    val option_name: String,
    val option_price: Int
)

data class GroupOptionModel(
    val group_id: Int,
    val group_name: String,
    val min_orderable_quantity: Int,
    val max_orderable_quantity: Int,
    val option_list: List<OptionModel>
)