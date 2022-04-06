package com.example.saengsaengtalk.adapterHome

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.R
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.util.*


class TaxiPreAdapter(val taxiList: MutableList<TaxiPre>) : RecyclerView.Adapter<TaxiPreAdapter.CustomViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lyt_taxi_pre, parent, false)
        return CustomViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val content =taxiList.get(position)
        val dt = content.datetime
        val dec = DecimalFormat("#,###")
        val text = dt.format(DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(Locale.forLanguageTag("ko"))) +
                "\n%s -> %s\n현 인원 %d명\n예상 택시비 %s원".format(content.depart, content.dest, content.member, dec.format(content.fee/content.member))
        holder.content.text = text
    }

    override fun getItemCount(): Int {
        return taxiList.size
    }

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content = itemView.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_taxi_pre)
    }

}