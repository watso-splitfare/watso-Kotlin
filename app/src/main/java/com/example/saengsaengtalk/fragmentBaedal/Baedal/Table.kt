package com.example.saengsaengtalk.fragmentBaedal.Baedal

import com.example.saengsaengtalk.APIS.BaedalPostPreviewModel
import java.time.LocalDate

class Table(
    val date: LocalDate,
    val rows: MutableList<BaedalPostPreviewModel>
    ) {}