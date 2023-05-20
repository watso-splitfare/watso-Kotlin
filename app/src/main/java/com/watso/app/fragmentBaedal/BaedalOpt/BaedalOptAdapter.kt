package com.watso.app.fragmentBaedal.BaedalOpt

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.watso.app.API.Group
import com.watso.app.API.Option
import com.watso.app.databinding.LytBaedalOptBinding
import java.text.DecimalFormat

class BaedalOptAdapter(val context: Context): RecyclerView.Adapter<BaedalOptAdapter.CustomViewHolder>() {

    private var options = mutableListOf<Option>()
    private var minOrderableQuantity = 1
    private var maxOrderableQuantity = 1

    private var isRadio = false
    private var checkedPosition = -1
    private var count = 0

    fun setData(group: Group) {
        group.options?.let {
            options.clear()
            options.addAll(it)
            minOrderableQuantity = group.minOrderQuantity
            maxOrderableQuantity = group.maxOrderQuantity
            if (minOrderableQuantity == 1 && maxOrderableQuantity == 1)
                isRadio = true
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalOptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val option = options.get(position)
        holder.bind(option)
    }

    interface OnOptionClickListener { fun onOptionClick(isRadio:Boolean, optionId: String, isChecked: Boolean) }

    fun setOptionClickListener(onOptionClickListener: OnOptionClickListener) { this.optionClickListener = onOptionClickListener }

    private lateinit var optionClickListener : OnOptionClickListener

    override fun getItemCount(): Int {
        return options.size
    }

    inner class CustomViewHolder(var binding: LytBaedalOptBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var checkBtn: CompoundButton
        fun bind(option: Option) {

            if (isRadio) {
                binding.rbOption.text = option.name
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
                    optionClickListener.onOptionClick(isRadio, option._id, true)
                }
                itemView.setOnClickListener {
                    checkedPosition = adapterPosition
                    notifyDataSetChanged()
                    optionClickListener.onOptionClick(isRadio, option._id, true)
                }
            } else {
                binding.cbOption.text = option.name
                binding.rbOption.setVisibility(View.INVISIBLE)
                checkBtn = binding.cbOption


                checkBtn.setOnClickListener { callOptionClickListener(option._id) }
                itemView.setOnClickListener {
                    checkBtn.setChecked(!checkBtn.isChecked)
                    callOptionClickListener(option._id)
                }

            }
            binding.cbOption.text = option.name
            val dec = DecimalFormat("#,###")
            binding.tvPrice.text = "${dec.format(option.price)}원"
        }

        fun callOptionClickListener(optionId: String) {
            if (checkBtn.isChecked) count += 1
            else count -= 1


            if (count > maxOrderableQuantity) {
                checkBtn.setChecked(false)
                count -= 1
                Toast.makeText(context, "최대 ${maxOrderableQuantity}개까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                optionClickListener.onOptionClick(isRadio, optionId, checkBtn.isChecked)
            }
        }
    }
}