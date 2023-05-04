package com.watso.app.adapterHome

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.watso.app.API.BaedalPost
import com.watso.app.API.Comment
import com.watso.app.API.VoidResponse
import com.watso.app.databinding.LytCommentBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CommentAdapter(val context: Context, val comments: MutableList<Comment>, val userId: Long) : RecyclerView.Adapter<CommentAdapter.CustomViewHolder>() {

    val api= API.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val comment = comments[position]
        holder.bind(comment)
    }

    interface OnDeleteListener { fun deleteComment() }

    fun setItemDeleteListener(onItemDeleteListener: OnDeleteListener) { this.itemDeleteListener = onItemDeleteListener }

    private lateinit var itemDeleteListener : OnDeleteListener

    override fun getItemCount(): Int {
        return comments.size
    }

    inner class CustomViewHolder(var binding: LytCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(comment: Comment) {
            if (comment.userId == userId) {
                binding.btnDelete.setOnClickListener {
                    deleteComment(comment)
                }
            }
            if (comment.subComments != null) {
                binding.ivReply.visibility = View.GONE

                binding.rvSubComment.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                binding.rvSubComment.setHasFixedSize(true)
                binding.rvSubComment.adapter = CommentAdapter(context, comment.subComments, userId)
            }
            else binding.btnReply.visibility = View.GONE

            if (comment.status == "created") {
                binding.tvNickname.text = comment.nickname
                binding.tvContent.text = comment.content
                val createdAt = LocalDateTime.parse(comment.createdAt, DateTimeFormatter.ISO_DATE_TIME)
                binding.tvCreatedAt.text = createdAt.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
            } else {
                binding.tvNickname.visibility = View.GONE
                binding.tvCreatedAt.visibility = View.GONE
                binding.tvContent.text = "삭제된 댓글입니다."
                binding.btnDelete.visibility = View.GONE
                binding.btnReply.visibility = View.GONE
            }
        }

        fun deleteComment(comment: Comment) {
            api.deleteComment(comment.postId, comment._id).enqueue(object : Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                    if (response.code() == 204) {
                        itemDeleteListener.deleteComment()
                    } else {
                        Log.e("Adapter Comment - deleteComment", response.toString())
                    }
                }

                override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                    Log.e("Adapter Comment - deleteComment Failure", t.message.toString())
                }
            })
        }
    }

}