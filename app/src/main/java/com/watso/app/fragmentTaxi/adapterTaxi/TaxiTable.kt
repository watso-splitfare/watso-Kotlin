package com.watso.app.fragmentTaxi.adapterTaxi

import java.time.LocalDate

class TaxiTable(
    val date: LocalDate,
    val rows: MutableList<TaxiTableRow>
    ) {}