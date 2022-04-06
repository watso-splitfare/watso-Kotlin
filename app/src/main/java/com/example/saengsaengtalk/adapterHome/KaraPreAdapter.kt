package com.example.saengsaengtalk.adapterHome

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.R
import java.time.format.DateTimeFormatter


class KaraPreAdapter(val karaList: MutableList<KaraPre>) : RecyclerView.Adapter<KaraPreAdapter.CustomViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lyt_kara_pre, parent, false)
        return CustomViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val content =karaList.get(position)
        val available = when(content.use) { true -> "사용중" else -> "사용가능" }
        val period = when(content.use) {
            true -> content.starttime.format(DateTimeFormatter.ofPattern("HH:mm")) + " ~ " +
                    content.endtime.format(DateTimeFormatter.ofPattern("HH:mm"))
            else -> ""
        }
        val text = "No.%d %s %s ".format(content.number, available, period)
        holder.content.text = text
    }

    override fun getItemCount(): Int {
        return karaList.size
    }

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content = itemView.findViewById<TextView>(R.id.tv_kara_pre)
    }

}