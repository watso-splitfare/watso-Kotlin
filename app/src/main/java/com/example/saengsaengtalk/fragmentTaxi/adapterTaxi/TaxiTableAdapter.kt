package com.example.saengsaengtalk.fragmentTaxi.adapterTaxi

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytTaxiTableBinding
import com.example.saengsaengtalk.fragmentBaedal.adapterBaedal.BaedalConfirmAdapter
import java.time.format.DateTimeFormatter

class TaxiTableAdapter(val context: Context, val taxiTable: MutableList<TaxiTable>) : RecyclerView.Adapter<TaxiTableAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytTaxiTableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

        val table = taxiTable[position]
        holder.bind(table)
    }

    override fun getItemCount(): Int {
        return taxiTable.size
    }

    inner class CustomViewHolder(var binding: LytTaxiTableBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(table: TaxiTable) {
            binding.tvDate.text = table.date.format(DateTimeFormatter.ofPattern("MM/dd"))
            binding.rvDateTable.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            val adapter = TaxiTableRowAdapter(table.rows)
            binding.rvDateTable.adapter = adapter
        }
    }
}