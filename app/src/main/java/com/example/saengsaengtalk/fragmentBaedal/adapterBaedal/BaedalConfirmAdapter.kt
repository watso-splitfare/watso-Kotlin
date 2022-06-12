package com.example.saengsaengtalk.fragmentBaedal.adapterBaedal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytBaedalConfirmBinding

class BaedalConfirmAdapter(val baedalConfirm: MutableList<BaedalConfirm>) : RecyclerView.Adapter<BaedalConfirmAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalConfirmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val optPrice = baedalConfirm.get(position)
        holder.bind(optPrice)
    }

    override fun getItemCount(): Int {
        return baedalConfirm.size
    }

    class CustomViewHolder(var binding: LytBaedalConfirmBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(optPrice: BaedalConfirm) {
            binding.tvOptPrice.text = optPrice.optPrice
        }
    }
}