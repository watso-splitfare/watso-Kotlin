package com.saengsaengtalk.app.adapterHome

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.saengsaengtalk.app.APIS.*
import com.saengsaengtalk.app.databinding.LytTaxiPreBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class TaxiPreAdapter(val taxiPosts: List<TaxiPostPreviewModel>) : RecyclerView.Adapter<TaxiPreAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaxiPreAdapter.CustomViewHolder {
        val binding = LytTaxiPreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: TaxiPreAdapter.CustomViewHolder, position: Int) {
        val post = taxiPosts[position]
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
        return taxiPosts.size
    }

    inner class CustomViewHolder(var binding: LytTaxiPreBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var text:String
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(post: TaxiPostPreviewModel) {

            if (post._id == "-1") { text = "등록된 게시물이 없어요.\n새로운 게시물을 작성해보세요!" }
            else {
                binding.tvRoute.text = post.dest_name + " -> " + post.depart_name
                binding.tvCurrentMember.text = "현인원 ${post.join_users.size}명"
                binding.tvDestTime.text = getDateTimeFormating(post.depart_time)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getDateTimeFormating(dateTimeStr: String): String {
            val dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
            val formatter = DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(Locale.forLanguageTag("ko"))
            return dateTime.format(formatter)
        }
    }

}