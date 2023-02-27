package com.example.saengsaengtalk.fragmentBaedal.Baedal

import com.example.saengsaengtalk.APIS.BaedalPostPreview
import java.time.LocalDate

class Table(
    val date: LocalDate,
    val rows: MutableList<BaedalPostPreview>
    ) {}