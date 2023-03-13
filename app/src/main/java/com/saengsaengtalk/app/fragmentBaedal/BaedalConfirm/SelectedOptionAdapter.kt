package com.saengsaengtalk.app.fragmentBaedal.BaedalConfirm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saengsaengtalk.app.APIS.OrderGroup
import com.saengsaengtalk.app.databinding.LytBaedalConfirmBinding
import java.text.DecimalFormat

class SelectedOptionAdapter(val groups: List<OrderGroup>) : RecyclerView.Adapter<SelectedOptionAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalConfirmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val dec = DecimalFormat("#,###")
        val group = groups[position]
        //val groupName = group.getString("groupName")
        val options = group.options
        var optionNames = ""//mutableListOf<String>()
        for (i in 0 until options.size) {
            val option = options[i]
            optionNames += (option.name + " (" + dec.format(option.price) + "원)")
            if (i != options.size - 1) optionNames += " / "
        }
        val optionString = "• ${group.name}: ${optionNames}"
        holder.bind(optionString)
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    class CustomViewHolder(var binding: LytBaedalConfirmBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(optionString: String) {
            binding.tvOptPrice.text = optionString
        }
    }
}