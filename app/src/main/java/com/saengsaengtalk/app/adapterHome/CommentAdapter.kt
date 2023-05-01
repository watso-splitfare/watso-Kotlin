package com.saengsaengtalk.app.adapterHome

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.saengsaengtalk.app.API.Comment
import com.saengsaengtalk.app.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CommentAdapter(val comments: List<Comment>, val user_id: Long) : RecyclerView.Adapter<CommentAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lyt_comment, parent, false)
        return CustomViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val comment = comments.get(position)
        if (comment.depth == 0){
            holder.iv_reply.layoutParams.height = 0
            holder.iv_reply.layoutParams.width = 0
        }
        else
            holder.btn_reply.text = ""
        if (comment.user_id != user_id)
            holder.btn_delete.text = ""
        holder.tv_nickname.text = comment.nick_name
        holder.tv_comment.text = comment.content
        val regDate = LocalDateTime.parse(comment.update_date, DateTimeFormatter.ISO_DATE_TIME)
        holder.tv_datetime.text = regDate.format(
            DateTimeFormatter.ofPattern("YYYY. MM. dd HH:MM")
        )
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iv_reply = itemView.findViewById<ImageView>(R.id.iv_reply)
        val tv_nickname = itemView.findViewById<TextView>(R.id.tv_nickname)
        val tv_comment = itemView.findViewById<TextView>(R.id.tv_comment)
        val tv_datetime = itemView.findViewById<TextView>(R.id.tv_time)
        val btn_delete = itemView.findViewById<Button>(R.id.btn_delete)
        val btn_reply = itemView.findViewById<Button>(R.id.btn_reply)
    }

}