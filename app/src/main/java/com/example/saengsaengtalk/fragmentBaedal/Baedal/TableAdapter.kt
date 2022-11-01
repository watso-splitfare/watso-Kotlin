package com.example.saengsaengtalk.fragmentBaedal.Baedal

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytBaedalTableBinding
import com.example.saengsaengtalk.databinding.LytTaxiTableBinding
import java.lang.ref.WeakReference
import java.time.format.DateTimeFormatter
import java.util.*

class TableAdapter(val context: Context, val tables: MutableList<Table>) : RecyclerView.Adapter<TableAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalTableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

        val table = tables[position]
        holder.bind(table)
    }

    interface OnItemClickListener {
        fun onClick(postId: String)
    }

    private var listener = WeakReference<OnItemClickListener>(null)

    fun itemClick(postId: String) {
        listener.get()?.onClick(postId)
    }

    fun addListener(listener: TableAdapter.OnItemClickListener) {
        this.listener = WeakReference(listener)
    }

    override fun getItemCount(): Int {
        return tables.size
    }

    inner class CustomViewHolder(var binding: LytBaedalTableBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(table: Table) {
            binding.tvDate.text = table.date.format(
                DateTimeFormatter.ofPattern("MM월 dd일(E)").withLocale(Locale.forLanguageTag("ko")))
            binding.rvDateTable.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            val adapter = TableRowAdapter(table.rows)
            binding.rvDateTable.adapter = adapter

            adapter.setItemClickListener(object: TableRowAdapter.OnItemClickListener{
                override fun onClick(postId: String) {
                    itemClick(postId)
                }
            })
        }
    }
}