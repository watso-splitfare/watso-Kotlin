package com.example.saengsaengtalk.fragmentBaedal.adapterBaedal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytBaedalOptBinding
import java.text.DecimalFormat

class BaedalOptAdapter(val baedalOpt: MutableList<BaedalOpt>) : RecyclerView.Adapter<BaedalOptAdapter.CustomViewHolder>() {

    var checkedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalOptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val opt = baedalOpt.get(position)
        holder.bind(opt)
    }

    interface OnItemClickListener {
        fun onClick(isRadio: Boolean, area: String, num: String, isChecked: Boolean)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return baedalOpt.size
    }

    inner class CustomViewHolder(var binding: LytBaedalOptBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var checkBtn: CompoundButton
        fun bind(opt: BaedalOpt) {
            if (opt.is_radio) {
                binding.rbMenu.text = opt.optName
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
                    itemClickListener.onClick(true, opt.area, opt.num, true)
                    println("0")
                }
                itemView.setOnClickListener {
                    checkedPosition = adapterPosition
                    notifyDataSetChanged()
                    itemClickListener.onClick(true, opt.area, opt.num, true)
                    println("1")
                }
            } else {
                binding.cbMenu.text = opt.optName
                binding.rbMenu.setVisibility(View.INVISIBLE)
                checkBtn = binding.cbMenu

                checkBtn.setOnClickListener {
                    itemClickListener.onClick(false, opt.area, opt.num, checkBtn.isChecked)
                    println(0)
                }
                itemView.setOnClickListener {
                    checkBtn.setChecked(!checkBtn.isChecked)
                    itemClickListener.onClick(false, opt.area, opt.num, checkBtn.isChecked)
                    println(1)
                }
            }
            binding.cbMenu.text = opt.optName
            val dec = DecimalFormat("#,###")
            binding.tvPrice.text = "${dec.format(opt.price)}원"
        }
    }
}