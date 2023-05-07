package com.watso.app.fragmentBaedal.Baedal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.watso.app.API.BaedalPost
import com.watso.app.databinding.LytBaedalTableRowBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TableRowAdapter() : RecyclerView.Adapter<TableRowAdapter.CustomViewHolder>() {

    private val tableRows = mutableListOf<BaedalPost>()

    fun setData(tableData: List<BaedalPost>) {
        tableRows.clear()
        tableRows.addAll(tableData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalTableRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val row = tableRows.get(position)

        holder.itemView.setOnClickListener {
            postClickListener.onClick(row._id)
        }
        holder.bind(row)
    }

    interface OnPostClickListener { fun onClick(postId: String) }

    fun setPostClickListener(onPostClickListener: OnPostClickListener) { this.postClickListener = onPostClickListener }

    private lateinit var postClickListener : OnPostClickListener

    override fun getItemCount(): Int {
        return tableRows.size
    }

    class CustomViewHolder(var binding: LytBaedalTableRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: BaedalPost) {
            val currentMember = post.users.size.toString()
            val maxMember = post.maxMember
            val orderTime = LocalDateTime.parse(post.orderTime, DateTimeFormatter.ISO_DATE_TIME)
            val status = when (post.status) {
                "recruiting" -> "모집중"
                "closed" -> "모집 마감"
                "ordered" -> "주문 완료"
                "delivered" -> "배달 완료"
                else -> "마감"
            }
            binding.tvTime.text = orderTime.format(
                    DateTimeFormatter.ofPattern("HH시 mm분", Locale.KOREAN)
                    )
            binding.tvStoreName.text = post.store.name
            binding.tvStatus.text = status
            binding.tvPlace.text = post.place
            binding.tvMember.text = currentMember + " / " + maxMember + "명"
            binding.tvNickname.text = post.nickname
        }
    }
}