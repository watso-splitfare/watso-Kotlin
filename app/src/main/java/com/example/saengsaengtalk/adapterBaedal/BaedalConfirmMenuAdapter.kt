package com.example.saengsaengtalk.adapterBaedal

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytBaedalConfirmMenuBinding
import com.example.saengsaengtalk.databinding.LytBaedalMenuSectionBinding
import java.lang.ref.WeakReference
import java.text.DecimalFormat


class BaedalConfirmMenuAdapter(val context: Context, val baedalConfirmMenu: MutableList<BaedalConfirmMenu>) : RecyclerView.Adapter<BaedalConfirmMenuAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalConfirmMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = baedalConfirmMenu[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return baedalConfirmMenu.size
    }

    inner class CustomViewHolder(var binding: LytBaedalConfirmMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BaedalConfirmMenu) {
            val dec = DecimalFormat("#,###")

            binding.tvMenu.text = item.menu

            binding.rvMenu.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            val adapter = BaedalConfirmAdapter(item.optList)
            binding.rvMenu.adapter = adapter

            binding.tvPrice.text = "${dec.format(item.price * item.count)}원"
            binding.tvCount.text = item.count.toString()

        }
    }
}