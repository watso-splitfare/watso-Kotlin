package com.example.saengsaengtalk.adapterBaedal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytBaedalOrderBinding

class BaedalOrderAdapter(val baedalOrder: MutableList<BaedalOrder>) : RecyclerView.Adapter<BaedalOrderAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val opt = baedalOrder.get(position)
        holder.bind(opt)
    }

    override fun getItemCount(): Int {
        return baedalOrder.size
    }

    class CustomViewHolder(var binding: LytBaedalOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(opt: BaedalOrder) {
            binding.tvMenuName.text = opt.menuName
            binding.tvOpt.text = opt.opt
        }
    }
}