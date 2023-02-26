package com.example.saengsaengtalk.fragmentBaedal.BaedalMenu

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.APIS.Menu
import com.example.saengsaengtalk.databinding.LytBaedalMenuBinding
import java.text.DecimalFormat

class BaedalMenuAdapter(val menus: List<Menu>) : RecyclerView.Adapter<BaedalMenuAdapter.CustomViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val menu = menus.get(position)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(menu.name)
        }
        holder.bind(menu)
    }

    interface OnItemClickListener {
        fun onClick(menuName: String)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return menus.size
    }

    class CustomViewHolder(var binding: LytBaedalMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        val dec = DecimalFormat("#,###")

        fun bind(menu: Menu) {
            binding.tvName.text = menu.name
            binding.tvPrice.text = "%s원".format(dec.format(menu.price))
        }
    }
}