package com.example.saengsaengtalk.adapterHome

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.APIS.BaedalPostPreviewModel
import com.example.saengsaengtalk.databinding.LytBaedalPreBinding
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class BaedalPreAdapter(val baedalPosts: List<BaedalPostPreviewModel>) : RecyclerView.Adapter<BaedalPreAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaedalPreAdapter.CustomViewHolder {
        val binding = LytBaedalPreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: BaedalPreAdapter.CustomViewHolder, position: Int) {
        val post = baedalPosts[position]
        holder.bind(post)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return baedalPosts.size
    }

    inner class CustomViewHolder(var binding: LytBaedalPreBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(post: BaedalPostPreviewModel) {

            val dec = DecimalFormat("#,###")
            val text = getDateTimeFormating(post.order_time) + ("\n${post.store.store_name} ${post.current_member}팀" +
                    "\n예상 배달비 ${dec.format(post.store.fee/post.current_member)}원")
            binding.tvBaedalPre.text = text
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getDateTimeFormating(dateTimeStr: String): String {
            val dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            val formatter = DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(Locale.forLanguageTag("ko"))
            return dateTime.format(formatter)
        }
    }

}