package com.watso.app.fragmentTaxi.adapterTaxi

import java.time.LocalDateTime

class TaxiTableRow(
    val postId: String,
    val depart: String,
    val dest: String,
    val time: LocalDateTime,
    val member: Int,
    //val price: Int
    ) {}