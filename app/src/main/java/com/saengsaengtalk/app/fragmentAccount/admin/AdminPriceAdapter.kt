package com.saengsaengtalk.app.fragmentAccount.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saengsaengtalk.app.databinding.LytAdminPriceBinding
import org.json.JSONArray
import org.json.JSONObject


class AdminPriceAdapter(val priceOptions: JSONArray) : RecyclerView.Adapter<AdminPriceAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytAdminPriceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val priceOption = priceOptions.getJSONObject(position)
        holder.bind(priceOption)

    }

    override fun getItemCount(): Int {
        return priceOptions.length()
    }

    inner class CustomViewHolder(var binding: LytAdminPriceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(priceOption: JSONObject) {
            binding.tvName.text = priceOption.getString("name")
            binding.tvId.text = priceOption.getString("optionId")
            binding.tvPrice.text = priceOption.getString("price")
        }
    }

}