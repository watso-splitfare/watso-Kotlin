package com.watso.app.adapterHome

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.watso.app.databinding.LytBoardPreBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class BoardPreAdapter(val boardList: MutableList<BoardPre>) : RecyclerView.Adapter<BoardPreAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBoardPreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = boardList[position]
        holder.bind(item)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(position)
        }
    }

    interface OnItemClickListener {
        fun onClick(position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener: OnItemClickListener

    override fun getItemCount(): Int {
        return boardList.size
    }

    inner class CustomViewHolder(var binding: LytBoardPreBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(boardPre: BoardPre) {
            val datetime = boardPre.datetime
            val today = LocalDate.now().atTime(0, 0)

            binding.tvBoardPre.text = boardPre.title
            binding.tvBoardPreDate.text = when (datetime.isBefore(today)) {
                true -> datetime.format(DateTimeFormatter.ofPattern("MM/dd"))
                else -> datetime.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
        }

    }
}