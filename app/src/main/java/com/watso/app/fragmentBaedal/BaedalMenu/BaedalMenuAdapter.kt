package com.watso.app.fragmentBaedal.BaedalMenu

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.watso.app.API.SectionMenu
import com.watso.app.databinding.LytBaedalMenuBinding
import java.text.DecimalFormat

class BaedalMenuAdapter() : RecyclerView.Adapter<BaedalMenuAdapter.CustomViewHolder>() {

    private var menus = mutableListOf<SectionMenu>()

    fun setData(menuData: List<SectionMenu>) {
        menus.clear()
        menus.addAll(menuData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val menu = menus.get(position)
        holder.itemView.setOnClickListener {
            Log.d("메뉴 어댑터", "클릭")
            menuClickListener.onMenuClick(menu._id)
        }
        holder.bind(menu)
    }

    interface OnMenuClickListener { fun onMenuClick(menuId: String) }

    fun setMenuClickListener(onMenuClickListener: OnMenuClickListener) { this.menuClickListener = onMenuClickListener}

    private lateinit var menuClickListener : OnMenuClickListener

    override fun getItemCount(): Int {
        return menus.size
    }

    class CustomViewHolder(var binding: LytBaedalMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        val dec = DecimalFormat("#,###")

        fun bind(menu: SectionMenu) {
            if (adapterPosition == 0) binding.divider.visibility = View.GONE
            binding.tvName.text = menu.name
            binding.tvPrice.text = "%s원".format(dec.format(menu.price))
        }
    }
}