package com.example.saengsaengtalk.adapterBaedal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytBaedalDetailBinding

class BaedalDetailAdapter(val baedalDetail: MutableList<BaedalDetail>) : RecyclerView.Adapter<BaedalDetailAdapter.CustomViewHolder>() {

    var checkedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = baedalDetail.get(position)
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return baedalDetail.size
    }

    inner class CustomViewHolder(var binding: LytBaedalDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var checkBtn: CompoundButton
        fun bind(item: BaedalDetail) {
            if (item.is_radio) {
                binding.rbMenu.text = item.optName
                binding.cbMenu.setVisibility(View.INVISIBLE)
                checkBtn = binding.rbMenu

                if (checkedPosition == -1 && adapterPosition == 0) {
                    checkBtn.setChecked(true)
                    checkedPosition = 0
                }
                if (checkedPosition == adapterPosition) {
                    checkBtn.setChecked(true)
                }
                else checkBtn.setChecked(false)

                checkBtn.setOnClickListener {
                    checkedPosition = adapterPosition
                    notifyDataSetChanged()
                    println("0")
                }
                itemView.setOnClickListener {
                    checkedPosition = adapterPosition
                    notifyDataSetChanged()
                    println("1")
                }
            } else {
                binding.cbMenu.text = item.optName
                binding.rbMenu.setVisibility(View.INVISIBLE)
                checkBtn = binding.cbMenu

                itemView.setOnClickListener {
                    if (checkBtn.isChecked) checkBtn.setChecked(false)
                    else checkBtn.setChecked(true)
                    println(1)
                }
            }
            binding.cbMenu.text = item.optName
            binding.tvPrice.text = "${item.price}원"
        }
    }
}