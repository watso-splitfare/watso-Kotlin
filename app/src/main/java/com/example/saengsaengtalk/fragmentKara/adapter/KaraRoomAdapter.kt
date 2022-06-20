package com.example.saengsaengtalk.fragmentKara.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.databinding.LytKaraRoomBinding
class KaraRoomAdapter(val karaRoom: MutableList<KaraRoom>) : RecyclerView.Adapter<KaraRoomAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytKaraRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = karaRoom[position]
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

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return karaRoom.size
    }

    inner class CustomViewHolder(var binding: LytKaraRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(room: KaraRoom) {
            binding.tvRoomNumber.text = room.num.toString()+"번방"
            if (room.useAble) {
                binding.tvState.text = "사용가능"
                binding.viewLight.setBackgroundResource(R.drawable.box_style_kara_useable)
            } else {
                binding.tvState.text = "사용중"
                binding.viewLight.setBackgroundResource(R.drawable.box_style_kara_unusable)
            }

            binding.tvUse.text = room.use
        }
    }
}