package com.example.saengsaengtalk.fragmentBaedal.BaedalOpt

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.APIS.OptionModel
import com.example.saengsaengtalk.databinding.LytBaedalOptBinding
import java.text.DecimalFormat

class BaedalOptAdapter(val context: Context, val option: List<OptionModel>, val minOrderableQuantity: Int, val maxOrderableQuantity: Int):
    RecyclerView.Adapter<BaedalOptAdapter.CustomViewHolder>() {

    var isRadio = false
    var checkedPosition = -1
    var count = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalOptBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        if (minOrderableQuantity == 1 && maxOrderableQuantity == 1){
            isRadio = true
        }
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val option = option.get(position)
        holder.bind(option)
    }

    interface OnItemClickListener {
        fun onClick(isRadio:Boolean, optionId: String, isChecked: Boolean)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return option.size
    }

    inner class CustomViewHolder(var binding: LytBaedalOptBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var checkBtn: CompoundButton
        fun bind(option: OptionModel) {
            if (isRadio) {
                binding.rbOption.text = option.option_name
                binding.cbOption.setVisibility(View.INVISIBLE)
                checkBtn = binding.rbOption

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
                    itemClickListener.onClick(isRadio, option.option_id, true)
                    println("0")
                }
                itemView.setOnClickListener {
                    checkedPosition = adapterPosition
                    notifyDataSetChanged()
                    itemClickListener.onClick(isRadio, option.option_id, true)
                    println("1")
                }
            } else {
                binding.cbOption.text = option.option_name
                binding.rbOption.setVisibility(View.INVISIBLE)
                checkBtn = binding.cbOption


                checkBtn.setOnClickListener {
                    callItemClickListener(option.option_id)
                }
                itemView.setOnClickListener {
                    checkBtn.setChecked(!checkBtn.isChecked)
                    callItemClickListener(option.option_id)
                }

            }
            binding.cbOption.text = option.option_name
            val dec = DecimalFormat("#,###")
            binding.tvPrice.text = "${dec.format(option.option_price)}원"
        }

        fun callItemClickListener(optionId: String) {
            if (checkBtn.isChecked) {
                count += 1
            } else {
                count -= 1
            }

            if (count > maxOrderableQuantity) {
                checkBtn.setChecked(false)
                count -= 1
                Toast.makeText(context, "최대 ${maxOrderableQuantity}개까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                itemClickListener.onClick(isRadio, optionId, checkBtn.isChecked)
            }
        }
    }
}