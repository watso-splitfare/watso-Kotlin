package com.example.saengsaengtalk.adapterBaedal

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.R
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.util.*


class BaedalCommentAdapter(val baedalComment: MutableList<BaedalComment>) : RecyclerView.Adapter<BaedalCommentAdapter.CustomViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lyt_comment, parent, false)
        return CustomViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val content =baedalComment.get(position)
        holder.nickname.text = content.nickname
        holder.comment.text = content.comment
        holder.datetime.text = content.datetime.format(DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(Locale.forLanguageTag("ko")))
    }

    override fun getItemCount(): Int {
        return baedalComment.size
    }

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nickname = itemView.findViewById<TextView>(R.id.tv_nickname)
        val comment = itemView.findViewById<TextView>(R.id.tv_comment)
        val datetime = itemView.findViewById<TextView>(R.id.tv_time)
    }

}