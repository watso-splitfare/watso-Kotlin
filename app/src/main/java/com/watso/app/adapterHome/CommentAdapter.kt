package com.watso.app.adapterHome

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.watso.app.API.Comment
import com.watso.app.databinding.LytCommentBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CommentAdapter(val context: Context, val comments: MutableList<Comment>, val userId: Long) : RecyclerView.Adapter<CommentAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val comment = comments[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    inner class CustomViewHolder(var binding: LytCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(comment: Comment) {
            if (comment.subComments != null) {
                binding.ivReply.visibility = View.GONE

                binding.rvSubComment.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                binding.rvSubComment.setHasFixedSize(true)
                binding.rvSubComment.adapter = CommentAdapter(context, comment.subComments, userId)
            }
            else binding.btnReply.visibility = View.GONE

            binding.tvNickname.text = comment.nickname
            binding.tvContent.text = comment.content
            val createdAt = LocalDateTime.parse(comment.createdAt, DateTimeFormatter.ISO_DATE_TIME)
            binding.tvCreatedAt.text = createdAt.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))


        }
    }

}