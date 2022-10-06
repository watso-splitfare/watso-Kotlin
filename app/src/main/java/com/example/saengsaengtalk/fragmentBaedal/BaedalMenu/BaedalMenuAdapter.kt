package com.example.saengsaengtalk.fragmentBaedal.BaedalMenu

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.APIS.MenuModel
import com.example.saengsaengtalk.databinding.LytBaedalMenuBinding
import java.text.DecimalFormat

class BaedalMenuAdapter(val menu: List<MenuModel>) : RecyclerView.Adapter<BaedalMenuAdapter.CustomViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val menu = menu.get(position)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(menu.menu_id)
        }
        holder.bind(menu)
    }

    interface OnItemClickListener {
        fun onClick(menuId: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return menu.size
    }

    class CustomViewHolder(var binding: LytBaedalMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        val dec = DecimalFormat("#,###")

        fun bind(menu: MenuModel) {
            binding.tvName.text = menu.menu_name
            binding.tvPrice.text = "%s원".format(dec.format(menu.menu_price))
        }
    }
}