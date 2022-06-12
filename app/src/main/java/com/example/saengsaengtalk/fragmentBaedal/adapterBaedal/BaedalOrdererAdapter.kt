package com.example.saengsaengtalk.fragmentBaedal.adapterBaedal

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytBaedalOrdererBinding


class BaedalOrdererAdapter(val context: Context, val baedalOrderer: MutableList<BaedalOrderer>) : RecyclerView.Adapter<BaedalOrdererAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalOrdererBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = baedalOrderer[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return baedalOrderer.size
    }

    inner class CustomViewHolder(var binding: LytBaedalOrdererBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BaedalOrderer) {
            binding.tvOrderer.text = item.orderer
            binding.rvOrderMenu.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val adapter = BaedalOrderAdapter(context, item.menuList)
            binding.rvOrderMenu.adapter = adapter
        }
    }
}