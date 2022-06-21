package com.example.saengsaengtalk.fragmentBaedal.adapterBaedal

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytBaedalMenuBinding

class BaedalMenuAdapter(val baedalMenu: MutableList<BaedalMenu>) : RecyclerView.Adapter<BaedalMenuAdapter.CustomViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val menu = baedalMenu.get(position)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(menu.id)
        }
        holder.bind(menu)
    }

    interface OnItemClickListener {
        fun onClick(id: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return baedalMenu.size
    }

    class CustomViewHolder(var binding: LytBaedalMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(menu: BaedalMenu) {
            binding.tvName.text = menu.menuName
            binding.tvPrice.text = menu.price
        }
    }
}