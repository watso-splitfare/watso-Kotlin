package com.example.saengsaengtalk.adapterBaedal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytBaedalOrderOptBinding

class BaedalOrderOptAdapter(val baedalOrderOpt: MutableList<BaedalOrderOpt>) : RecyclerView.Adapter<BaedalOrderOptAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalOrderOptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val opt = baedalOrderOpt.get(position)
        holder.bind(opt)
    }

    override fun getItemCount(): Int {
        return baedalOrderOpt.size
    }

    class CustomViewHolder(var binding: LytBaedalOrderOptBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(opt: BaedalOrderOpt) {
            binding.tvOpt.text = opt.opt
        }
    }
}