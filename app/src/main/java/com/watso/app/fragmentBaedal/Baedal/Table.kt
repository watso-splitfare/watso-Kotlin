package com.watso.app.fragmentBaedal.Baedal

import com.watso.app.API.BaedalPost
import java.time.LocalDate

class Table(
    val date: LocalDate,
    val rows: MutableList<BaedalPost>
    ) {}