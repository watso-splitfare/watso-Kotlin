package com.example.saengsaengtalk.fragmentTaxi

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.databinding.FragTaxiBinding
import com.example.saengsaengtalk.fragmentBaedal.FragmentBaedalOpt
import com.example.saengsaengtalk.fragmentBaedal.adapterBaedal.BaedalMenuSectionAdapter
import com.example.saengsaengtalk.fragmentTaxi.adapterTaxi.TaxiTable
import com.example.saengsaengtalk.fragmentTaxi.adapterTaxi.TaxiTableAdapter
import com.example.saengsaengtalk.fragmentTaxi.adapterTaxi.TaxiTableRow
import com.example.saengsaengtalk.fragmentTaxi.adapterTaxi.TaxiTableRowAdapter
import java.time.LocalDate
import java.time.LocalDateTime

class FragmentTaxi :Fragment() {

    private var mBinding: FragTaxiBinding? = null
    private val binding get() = mBinding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragTaxiBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        val taxiTable = mutableListOf(
            TaxiTable(LocalDate.parse("2022-06-22"), mutableListOf(
                TaxiTableRow(1,"생자대", "밀양역", LocalDateTime.parse("2022-06-22T15:10:00"), 3, 6600),
                TaxiTableRow(2,"밀양역", "생자대", LocalDateTime.parse("2022-06-22T16:10:00"), 2, 6600),
                TaxiTableRow(3,"생자대", "삼문동", LocalDateTime.parse("2022-06-22T17:10:00"), 1, 6600)
            )), TaxiTable(LocalDate.parse("2022-06-23"), mutableListOf(
                TaxiTableRow(4,"생자대", "밀양역", LocalDateTime.parse("2022-06-23T15:10:00"), 3, 6600),
                TaxiTableRow(5,"생자대", "밀양역", LocalDateTime.parse("2022-06-23T16:10:00"), 2, 6600),
                TaxiTableRow(6,"생자대", "밀양역", LocalDateTime.parse("2022-06-23T17:10:00"), 1, 6600),
                TaxiTableRow(7,"생자대", "밀양역", LocalDateTime.parse("2022-06-23T17:10:00"), 2, 6600),
                TaxiTableRow(8,"생자대", "밀양역", LocalDateTime.parse("2022-06-23T17:10:00"), 3, 6600)
            )), TaxiTable(LocalDate.parse("2022-06-24"), mutableListOf(
                TaxiTableRow(9,"생자대", "밀양역", LocalDateTime.parse("2022-06-24T15:10:00"), 3, 6600),
                TaxiTableRow(10,"생자대", "밀양역", LocalDateTime.parse("2022-06-24T16:10:00"), 2, 6600),
                TaxiTableRow(11,"생자대", "밀양역", LocalDateTime.parse("2022-06-24T17:10:00"), 1, 6600)
            )), TaxiTable(LocalDate.parse("2022-06-25"), mutableListOf(
                TaxiTableRow(12,"생자대", "밀양역", LocalDateTime.parse("2022-06-25T15:10:00"), 3, 6600),
                TaxiTableRow(13,"생자대", "밀양역", LocalDateTime.parse("2022-06-25T16:10:00"), 2, 6600),
                TaxiTableRow(14,"생자대", "밀양역", LocalDateTime.parse("2022-06-25T17:10:00"), 1, 6600)
            )))

        binding.rvTaxiTable.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val adapter = TaxiTableAdapter(requireContext(), taxiTable)
        binding.rvTaxiTable.adapter = adapter

        adapter.addListener(object: TaxiTableAdapter.OnItemClickListener{
            override fun onClick(postNum: Int) {
                setFrag(FragmentTaxiPost(), mapOf("postNum" to postNum.toString()))
            }
        })
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }
}