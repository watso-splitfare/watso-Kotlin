package com.watso.app.fragmentBaedal.BaedalHistory

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.watso.app.API.BaedalPost
import com.watso.app.databinding.LytBaedalHistoryBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class HistoryAdapter(val posts: MutableList<BaedalPost>) : RecyclerView.Adapter<HistoryAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    interface OnOrderBtnListener { fun showOrder(postId: String) }
    interface OnPostBtnListener { fun showPost(postId: String) }

    fun setShowOrderListener(onOrderListener: OnOrderBtnListener) { this.orderListener = onOrderListener }
    fun setShowPostListener(onPostListener: OnPostBtnListener) { this.postListener = onPostListener }

    private lateinit var orderListener : OnOrderBtnListener
    private lateinit var postListener : OnPostBtnListener

    override fun getItemCount(): Int {
        return posts.size
    }

    inner class CustomViewHolder(var binding: LytBaedalHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(post: BaedalPost) {
            binding.tvStatus.text = when (post.status) {
                "recruiting" ->  "모집중"
                "closed" -> "모집마감"
                "ordered" -> "주문완료"
                "delivered" -> "배달완료"
                "canceled" -> "취소"
                else -> "모집마감"
            }
            val orderTime = LocalDateTime.parse(post.orderTime, DateTimeFormatter.ISO_DATE_TIME)
            binding.tvDatetime.text = orderTime.format(
                DateTimeFormatter.ofPattern("M월 d일(E) H시 m분",Locale.KOREAN)
            )
            binding.tvStoreName.text = post.store.name
            binding.btnShowOrder.setOnClickListener { orderListener.showOrder(post._id) }
            binding.btnShowPost.setOnClickListener { postListener.showPost(post._id) }
            binding.lytBaedalHistory.setOnClickListener { postListener.showPost(post._id) }
        }
    }
}