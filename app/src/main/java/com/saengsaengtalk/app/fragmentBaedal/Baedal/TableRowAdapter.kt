package com.saengsaengtalk.app.fragmentBaedal.Baedal

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.saengsaengtalk.app.APIS.BaedalPostPreview
import com.saengsaengtalk.app.databinding.LytBaedalTableRowBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TableRowAdapter(val tableRows: List<BaedalPostPreview>) : RecyclerView.Adapter<TableRowAdapter.CustomViewHolder>() {

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
        fun bind(post: BaedalPostPreview) {
            val currentMember = post.userOrders.size.toString()
            val maxMember = post.maxMember

            binding.tvTitle.text = post.title
            binding.tvPlace.text = post.place
            binding.tvMember.text = currentMember + " / " + maxMember + "명"
            binding.tvNickname.text = post.nickname

        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getDateTimeFormating(dateTimeStr: String): String {
            val orderTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
            //val dateTime = LocalDateTime.parse(dateTimeStr.substring(0 until 16), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            //val formatter = DateTimeFormatter.ofPattern("HH시 mm분").withLocale(Locale.forLanguageTag("ko"))
            return orderTime.format(
                DateTimeFormatter.ofPattern("HH시 mm분",Locale.KOREAN)) //dateTime.format(formatter)
        }
    }
}