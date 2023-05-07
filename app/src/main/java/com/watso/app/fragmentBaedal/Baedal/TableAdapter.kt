package com.watso.app.fragmentBaedal.Baedal

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.watso.app.databinding.LytBaedalTableBinding
import java.time.format.DateTimeFormatter
import java.util.*

class TableAdapter(val context: Context) : RecyclerView.Adapter<TableAdapter.CustomViewHolder>() {

    private val tables = mutableListOf<Table>()

    fun setData(tableData: List<Table>) {
        tables.clear()
        tables.addAll(tableData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalTableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

        val table = tables[position]
        holder.bind(table)
    }

    interface OnPostClickListener { fun onClick(postId: String) }

    fun setPostClickListener(onPostClickListener: OnPostClickListener) { this.postClickListener = onPostClickListener }

    private lateinit var postClickListener: OnPostClickListener

    override fun getItemCount(): Int {
        return tables.size
    }

    inner class CustomViewHolder(var binding: LytBaedalTableBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(table: Table) {
            if (layoutPosition == 0) { binding.divider.visibility = View.GONE }
            binding.tvDate.text = table.date.format(
                DateTimeFormatter.ofPattern("MM월 dd일(E)").withLocale(Locale.forLanguageTag("ko")))
            binding.rvDateTable.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            val adapter = TableRowAdapter()
            binding.rvDateTable.adapter = adapter
            adapter.setData(table.rows)

            adapter.setPostClickListener(object: TableRowAdapter.OnPostClickListener{
                override fun onClick(postId: String) {
                    postClickListener.onClick(postId)
                }
            })
        }
    }
}