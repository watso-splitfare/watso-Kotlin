package com.example.saengsaengtalk

/*data class HTTP_GET_Model(
    var something : String? =null ,
    var users : ArrayList<UserModel>? =null
)


data class UserModel(
    var idx : Int? =null ,
    var id : String?=null,
    var nick : String? =null
)*/


data class PostModel(
    var name : String?=null,
    var nickname : String?=null,
    var id : String?=null,
    var pw : String?=null,
    var email : String?=null,
)

data class PostResult(
    var result:String? = null
)