package com.example.saengsaengtalk.fragmentTaxi.adapterTaxi

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytTaxiTableBinding
import java.lang.ref.WeakReference
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

    interface OnItemClickListener {
        fun onClick(postNum: Int)
    }

    private var listener = WeakReference<OnItemClickListener>(null)

    fun itemClick(postNum: Int) {
        listener.get()?.onClick(postNum)
    }

    fun addListener(listener: TaxiTableAdapter.OnItemClickListener) {
        this.listener = WeakReference(listener)
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

            adapter.setItemClickListener(object: TaxiTableRowAdapter.OnItemClickListener{
                override fun onClick(postNum:Int) {
                    itemClick(postNum)
                }
            })
        }
    }
}