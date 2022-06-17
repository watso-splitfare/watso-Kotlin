package com.example.saengsaengtalk.adapterHome

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.R
import java.time.format.DateTimeFormatter
import java.util.*


class CommentAdapter(val baedalComment: MutableList<Comment>) : RecyclerView.Adapter<CommentAdapter.CustomViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lyt_comment, parent, false)
        return CustomViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val content = baedalComment.get(position)
        if (content.depth == 0){
            holder.iv_reply.layoutParams.height = 0
            holder.iv_reply.layoutParams.width = 0
        }
        else
            holder.btn_reply.text = ""
        if (content.nickname != "주넝이")
            holder.btn_delete.text = ""
        holder.tv_nickname.text = content.nickname
        holder.tv_comment.text = content.comment
        holder.tv_datetime.text = content.createdAt.format(DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(Locale.forLanguageTag("ko")))
    }

    override fun getItemCount(): Int {
        return baedalComment.size
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