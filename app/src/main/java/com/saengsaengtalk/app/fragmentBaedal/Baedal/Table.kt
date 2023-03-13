package com.saengsaengtalk.app.fragmentBaedal.Baedal

import com.saengsaengtalk.app.APIS.BaedalPostPreview
import java.time.LocalDate

class Table(
    val date: LocalDate,
    val rows: MutableList<BaedalPostPreview>
    ) {}