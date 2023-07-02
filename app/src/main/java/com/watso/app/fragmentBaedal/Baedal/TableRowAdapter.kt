package com.watso.app.fragmentBaedal.Baedal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.watso.app.API.BaedalPost
import com.watso.app.R
import com.watso.app.databinding.LytBaedalTableRowBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TableRowAdapter(val context: AppCompatActivity) : RecyclerView.Adapter<TableRowAdapter.CustomViewHolder>() {

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

    inner class CustomViewHolder(var binding: LytBaedalTableRowBinding) : RecyclerView.ViewHolder(binding.root) {
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

            val defaultImage = R.drawable.delivery
            Glide.with(context)
                .load(post.store.logoImgUrl) // 불러올 이미지 url
                .placeholder(defaultImage) // 이미지 로딩 시작하기 전 표시할 이미지
                .error(defaultImage) // 로딩 에러 발생 시 표시할 이미지
                .fallback(defaultImage) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                .into(binding.ivStoreLogo) // 이미지를 넣을 뷰

            binding.tvTime.text = orderTime.format(
                    DateTimeFormatter.ofPattern("HH시 mm분", Locale.KOREAN)
                    )
            binding.tvStoreName.text = "[${post.place}] ${post.store.name}"
            binding.tvStatus.text = status
            binding.tvMember.text = currentMember + " / " + maxMember + "명"
            binding.tvNickname.text = "모임장 : " + post.nickname
        }
    }
}