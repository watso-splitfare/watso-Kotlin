package com.example.saengsaengtalk.fragmentTaxi.adapterTaxi

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytTaxiTableRowBinding
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter

class TaxiTableRowAdapter(val taxiTableRow: MutableList<TaxiTableRow>) : RecyclerView.Adapter<TaxiTableRowAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytTaxiTableRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val row = taxiTableRow.get(position)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(row.postNum)
        }
        holder.bind(row)
    }

    interface OnItemClickListener {
        fun onClick(postNum: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return taxiTableRow.size
    }

    class CustomViewHolder(var binding: LytTaxiTableRowBinding) : RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(row: TaxiTableRow) {
            val dec = DecimalFormat("#,###")

            binding.tvDepart.text = row.depart
            binding.tvDest.text = row.dest
            binding.tvTime.text = row.time.format(DateTimeFormatter.ofPattern("HH:mm"))
            binding.tvMember.text = "${row.member}명"
            binding.tvPrice.text = "${dec.format(row.price/row.member)}원"
        }
    }
}