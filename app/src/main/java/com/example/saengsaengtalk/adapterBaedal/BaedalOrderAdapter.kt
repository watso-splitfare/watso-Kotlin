package com.example.saengsaengtalk.adapterBaedal

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytBaedalOrderBinding

class BaedalOrderAdapter(val context: Context, val baedalOrder: MutableList<BaedalOrder>) : RecyclerView.Adapter<BaedalOrderAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val order = baedalOrder.get(position)
        holder.bind(order)
    }

    override fun getItemCount(): Int {
        return baedalOrder.size
    }

    inner class CustomViewHolder(var binding: LytBaedalOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: BaedalOrder) {
            binding.tvMenuName.text = order.menuName
            binding.rvOpt.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val adapter = BaedalOrderOptAdapter(order.opt)
            binding.rvOpt.adapter = adapter
        }
    }
}