package com.example.saengsaengtalk.APIS.DataModels

/** 213 배달 게시글 미리보기 모델 */
data class TaxiPostPreviewModel(
    val _id: String,
    val depart_time: String,
    val depart_name: String,
    val dest_name: String,
    val join_users: List<Long>,
    val fee: Int
)