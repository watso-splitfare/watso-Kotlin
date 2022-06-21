package com.example.saengsaengtalk.fragmentTaxi.adapterTaxi

import java.time.LocalDateTime

class TaxiTableRow(
    val postNum: Int,
    val depart: String,
    val dest: String,
    val time: LocalDateTime,
    val member: Int,
    val price: Int
    ) {}