package com.example.saengsaengtalk.fragmentBaedal.Baedal

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.APIS.BaedalPostPreviewModel
import com.example.saengsaengtalk.databinding.LytBaedalTableRowBinding
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TableRowAdapter(val tableRows: List<BaedalPostPreviewModel>) : RecyclerView.Adapter<TableRowAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalTableRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val row = tableRows.get(position)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(row._id)
        }
        holder.bind(row)
    }

    interface OnItemClickListener {
        fun onClick(postId: String)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return tableRows.size
    }

    class CustomViewHolder(var binding: LytBaedalTableRowBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(post: BaedalPostPreviewModel) {
            val currentMember = post.join_users.size

            binding.lytBaedalListLikeview.visibility = View.GONE

            binding.tvStoreName.text = post.store.name
            binding.tvOrderTime.text = "주문예정: " + getDateTimeFormating(post.order_time)
            binding.tvMember.text = "모인인원: " + currentMember + "팀"
            binding.tvTitle.text = post.title

        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getDateTimeFormating(dateTimeStr: String): String {
            val dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            val formatter = DateTimeFormatter.ofPattern("HH시 mm분").withLocale(Locale.forLanguageTag("ko"))
            return dateTime.format(formatter)
        }
    }
}