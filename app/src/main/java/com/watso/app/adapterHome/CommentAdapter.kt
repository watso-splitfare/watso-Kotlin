package com.watso.app.adapterHome

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
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
import com.watso.app.R
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
    interface OnReplyListener { fun makeReply(parentComment: Comment) }

    fun setDeleteListener(onDeleteListener: OnDeleteListener) { this.deleteListener = onDeleteListener }
    fun setReplyListener(onReplyListener: OnReplyListener) { this.replyListener = onReplyListener }

    private lateinit var deleteListener : OnDeleteListener
    private lateinit var replyListener : OnReplyListener

    override fun getItemCount(): Int {
        return comments.size
    }

    inner class CustomViewHolder(var binding: LytCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(comment: Comment) {
            if (adapterPosition == 0) binding.divider.visibility = View.GONE
            /**
             * 부모댓글 일 때
             *      내 댓글         -> 답글, 삭제
             *      내 댓글 아님     -> 답글
             * 대댓글 일 때
             *      내 댓글         -> 삭제
             *      내 댓글 아님     ->
             *
             */

            if (comment.status == "created") {
                binding.tvNickname.text = comment.nickname
                binding.tvContent.text = comment.content
                val createdAt = LocalDateTime.parse(comment.createdAt, DateTimeFormatter.ISO_DATE_TIME)
                binding.tvCreatedAt.text = createdAt.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
                binding.btnReply.setOnClickListener { replyListener.makeReply(comment) }
                binding.btnDelete.setOnClickListener { alertDelete(comment) }

                if (comment.parentId == null) {         // 부모 댓글일 때
                    binding.ivReply.visibility = View.GONE
                    if (comment.userId != userId) {                 // 내 댓글이 아니면
                        binding.btnDelete.visibility = View.GONE
                        binding.btnReply.setBackgroundResource(R.drawable.btn_area_alone)
                        binding.divCommentArea.visibility = View.GONE
                    }
                } else {                                // 대댓글일 때
                    binding.btnReply.visibility = View.GONE
                    binding.divCommentArea.visibility = View.GONE
                    if (comment.userId == userId ) {                // 내 댓글이면
                        binding.btnDelete.setBackgroundResource(R.drawable.btn_area_alone)
                    } else binding.btnDelete.visibility = View.GONE // 내 댓글이 아니면
                }
            } else {        // 삭제된 댓글
                binding.tvNickname.visibility = View.GONE
                binding.tvContent.text = "삭제된 댓글입니다."
                binding.tvCreatedAt.visibility = View.GONE
                binding.btnDelete.visibility = View.GONE
                binding.btnReply.visibility = View.GONE
            }
        }

        fun alertDelete(comment: Comment) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("댓글 삭제하기")
                .setMessage("댓글을 삭제하시겠습니까?")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    deleteComment(comment)  })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
            builder.show()
        }

        fun deleteComment(comment: Comment) {
            api.deleteComment(comment.postId, comment._id).enqueue(object : Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                    if (response.code() == 204) {
                        deleteListener.deleteComment()

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