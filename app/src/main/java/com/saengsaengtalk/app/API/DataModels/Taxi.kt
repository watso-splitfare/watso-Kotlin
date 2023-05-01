package com.saengsaengtalk.app.API

/** 302 택시 게시글 등록 모델 */
data class TaxiPostingModel(
    val user_id: Long,
    val depart_name: String,
    val dest_name: String,
    val title: String,
    val content: String?,
    val depart_time: String,
    val min_member: Int?,
    val max_member: Int?
)

/** 303 택시 게시글 조회 모델 */
data class TaxiPostModel(
    val user_id: Long,
    val nick_name: String,
    val title: String,
    val content: String?,
    val depart_time: String,
    val min_member: Int?,
    val max_member: Int?,
    val views: Int,
    val update_date: String,
    val is_closed: Boolean,
    val join_users: List<Long>,
    val depart_name: String,
    val dest_name: String
)

/** 305 동승 가능 여부 변경 응답 모델 */
/*data class TaxiSwitchConditionResponse(
    val post_id: String,
    val success: Boolean,
    val condition: Boolean
)*/

/** 306 동승 신청/취소 응답 모델 */
/*data class TaxiJoinResponse(
    val post_id: String,
    val success: Boolean,
    val join: Boolean
)*/

/** 307 택시 게시글 미리보기 모델 */
data class TaxiPostPreviewModel(
    val _id: String,
    val depart_time: String,
    val depart_name: String,
    val dest_name: String,
    val join_users: List<Long>
)