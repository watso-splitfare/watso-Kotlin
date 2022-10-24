package com.example.saengsaengtalk.fragmentBaedal.Baedal

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.APIS.BaedalPostPreviewModel
import com.example.saengsaengtalk.databinding.LytBaedalListBinding
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class BaedalListAdapter(val baedalPosts: List<BaedalPostPreviewModel>) : RecyclerView.Adapter<BaedalListAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaedalListAdapter.CustomViewHolder {
        val binding = LytBaedalListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: BaedalListAdapter.CustomViewHolder, position: Int) {
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

    inner class CustomViewHolder(var binding: LytBaedalListBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(post: BaedalPostPreviewModel) {
            binding.imgBaedalListLike.visibility = View.GONE
            binding.imgBaedalListViewed.visibility = View.GONE
            binding.tvBaedalListLike.visibility = View.GONE
            binding.tvBaedalListViewed.visibility = View.GONE

            val dec = DecimalFormat("#,###")
            val text = "%s\n".format(post.title) + getDateTimeFormating(post.order_time) +
                    "\n%s\n%d팀\n예상 배달비 %s원".format(post.store.store_name, post.current_member, dec.format(post.store.fee/post.current_member))
            binding.tvBaedalListContent.text = text

        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getDateTimeFormating(dateTimeStr: String): String {
            val dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            val formatter = DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(Locale.forLanguageTag("ko"))
            return dateTime.format(formatter)
        }
    }
}