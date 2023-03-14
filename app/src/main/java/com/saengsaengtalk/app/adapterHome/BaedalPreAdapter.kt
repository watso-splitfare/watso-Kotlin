package com.saengsaengtalk.app.adapterHome

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.saengsaengtalk.app.APIS.BaedalPostPreview
import com.saengsaengtalk.app.databinding.LytBaedalPreBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class BaedalPreAdapter(val baedalPosts: List<BaedalPostPreview>) : RecyclerView.Adapter<BaedalPreAdapter.CustomViewHolder>() {

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
        lateinit var text:String
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(post: BaedalPostPreview) {
            if (post._id == "-1") { text = "등록된 게시물이 없어요.\n새로운 게시물을 작성해보세요!" }
            else {
                binding.tvStoreName.text = post.store.name
                binding.tvCurrentMember.text = "현인원 ${post.users}팀"
                binding.tvOrderTime.text = getDateTimeFormating(post.orderTime)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getDateTimeFormating(dateTimeStr: String): String {
            val dateTime = LocalDateTime.parse(dateTimeStr.substring(0 until 16), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            val formatter = DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(Locale.forLanguageTag("ko"))
            return dateTime.format(formatter)
        }
    }

}