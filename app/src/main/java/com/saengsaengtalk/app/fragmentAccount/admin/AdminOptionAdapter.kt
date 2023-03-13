package com.saengsaengtalk.app.fragmentAccount.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saengsaengtalk.app.databinding.LytAdminOptionBinding
import org.json.JSONArray
import org.json.JSONObject


class AdminOptionAdapter(val options: JSONArray) : RecyclerView.Adapter<AdminOptionAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytAdminOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val option = options.getJSONObject(position)
        holder.bind(option)

    }

    override fun getItemCount(): Int {
        return options.length()
    }

    inner class CustomViewHolder(var binding: LytAdminOptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(option: JSONObject) {
            binding.tvName.text = option.getString("name")
            binding.tvId.text = option.getString("optionId")
            binding.tvPrice.text = option.getString("price")
        }
    }

}