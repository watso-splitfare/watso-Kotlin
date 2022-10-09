package com.example.saengsaengtalk.fragmentBaedal.BaedalConfirm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytBaedalConfirmBinding
import org.json.JSONArray
import java.text.DecimalFormat

class AdapterSelectedOption(val groups: JSONArray) : RecyclerView.Adapter<AdapterSelectedOption.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalConfirmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val dec = DecimalFormat("#,###")
        val group = groups.getJSONObject(position)
        //val groupName = group.getString("groupName")
        val options = group.getJSONArray("options")
        var optionNames = ""//mutableListOf<String>()
        for (i in 0 until options.length()) {
            val option = options.getJSONObject(i)
            optionNames += (option.getString("optionName") + " (" + dec.format(option.getInt("optionPrice")) + "원)")
            if (i != options.length() - 1) optionNames += " / "
        }
        val optionString = "• ${group.getString("groupName")}: ${optionNames}"
        holder.bind(optionString)
    }

    override fun getItemCount(): Int {
        return groups.length()
    }

    class CustomViewHolder(var binding: LytBaedalConfirmBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(optionString: String) {
            binding.tvOptPrice.text = optionString
        }
    }
}