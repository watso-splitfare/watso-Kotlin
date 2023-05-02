package com.watso.app.adapterHome

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.watso.app.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class ClubBoardPreAdapter(val clubboardList: MutableList<ClubBoardPre>) : RecyclerView.Adapter<ClubBoardPreAdapter.CustomViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lyt_board_pre, parent, false)
        return CustomViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val content =clubboardList.get(position)
        val today = LocalDate.now().atTime(0,0)
        val datetime = content.datetime
        holder.title.text = content.title
        holder.datetime.text = when(datetime.isBefore(today)) {
            true -> datetime.format(DateTimeFormatter.ofPattern("MM/dd"))
            else -> datetime.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
    }

    override fun getItemCount(): Int {
        return clubboardList.size
    }

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.tv_board_pre)
        val datetime = itemView.findViewById<TextView>(R.id.tv_board_pre_date)
    }

}