package com.example.saengsaengtalk

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.util.*


class BaedalPreAdapter(val baedalList: MutableList<BaedalPre>) : RecyclerView.Adapter<BaedalPreAdapter.CustomViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaedalPreAdapter.CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lyt_baedal_pre, parent, false)
        return CustomViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: BaedalPreAdapter.CustomViewHolder, position: Int) {
        val content =baedalList.get(position)
        val dt = content.datetime
        val dec = DecimalFormat("#,###")
        val text = dt.format(DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(Locale.forLanguageTag("ko"))) +
                "\n%s %d팀\n예상 배달비 %s원".format(content.shop, content.member, dec.format(content.fee/content.member))
        holder.content.text = text
    }

    override fun getItemCount(): Int {
        return baedalList.size
    }

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content = itemView.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_baedal_pre)
    }

}