package com.example.saengsaengtalk.APIS

import org.json.JSONArray
import org.json.JSONObject

data class TestModel(
    var testBody: String? = null
)

data class TestResult(
    var a: String? = null,
    var b: Int? = null,
    var c: Map<String, String>? = null,
    var d: List<Int>
)

/*data class PostModel(
    var name : String?=null,
    var nickname : String?=null,
    var id : String?=null,
    var pw : String?=null,
    var email : String?=null,
)

data class PostResult(
    var result:String? = null
)*/

data class OverlapResult(
    var result: Boolean
)

data class SignUpResult(
    var result: Boolean
)

data class SignUpModel(
    var user_name: String,
    var pw: String,
    var student_num: Int,
    var nick_name: String
)

data class LoginModel(
    var user_name: String,
    var pw: String
)

data class LoginResult(
    var result: Boolean,
    var code: Int
)

data class LogoutResult(
    var result: Boolean,
    var code: Int
)

/** 배달 API 관련 데이터 모델 */

data class StoreListModel(
    var store_id: Long,
    var store_name: String,
    var fee: Int
)

data class MenuModel(
    var menu_id: Int,
    var menu_name: String,
    var menu_price: Int
)

data class SectionMenuModel(
    var section_id: Int,
    var section_name: String,
    var menu_list: List<MenuModel>
)

data class OptionModel(
    var option_id: Int,
    var option_name: String,
    var option_price: Int
)

data class GroupOptionModel(
    var group_id: Int,
    var group_name: String,
    var min_orderable_quantity: Int,
    var max_orderable_quantity: Int,
    var option_list: List<OptionModel>
)