package com.saengsaengtalk.app.fragmentBaedal.Baedal

import com.saengsaengtalk.app.API.BaedalPost
import java.time.LocalDate

class Table(
    val date: LocalDate,
    val rows: MutableList<BaedalPost>
    ) {}