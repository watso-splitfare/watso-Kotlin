package com.example.saengsaengtalk.fragmentFreeBoard.adapterFB

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.databinding.LytPostInListBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PostInListAdapter(val postInList: MutableList<PostInList>) : RecyclerView.Adapter<PostInListAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytPostInListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = postInList[position]
        holder.bind(item)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(position)
        }
    }

    interface OnItemClickListener {
        fun onClick(position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return postInList.size
    }

    inner class CustomViewHolder(var binding: LytPostInListBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(post: PostInList) {
            binding.tvTitle.text = post.title
            binding.tvWriter.text = post.writer

            val likeUserList = post.likeUserList
            if ("주넝이" in likeUserList) binding.ivLike.setImageResource(R.drawable.heart_red)
            else binding.ivLike.setImageResource(R.drawable.heart)
            binding.tvLike.text = likeUserList.size.toString()

            binding.tvComment.text = post.commentCount.toString()

            val today = LocalDate.now().atTime(0,0)
            val createdAt = post.createdAt
            binding.tvCreatedAt.text = when(createdAt.isBefore(today)) {
                true -> createdAt.format(DateTimeFormatter.ofPattern("MM/dd"))
                else -> createdAt.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
        }
    }


}