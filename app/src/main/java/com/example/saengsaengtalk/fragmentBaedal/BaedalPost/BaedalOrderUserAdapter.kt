package com.example.saengsaengtalk.fragmentBaedal.BaedalPost

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytBaedalOrderUserBinding
import com.example.saengsaengtalk.fragmentBaedal.BaedalConfirm.SelectedMenuAdapter
import com.google.gson.Gson
import org.json.JSONArray


class BaedalOrderUserAdapter(val context: Context, val baedalOrderUsers: MutableList<BaedalOrderUser>) : RecyclerView.Adapter<BaedalOrderUserAdapter.CustomViewHolder>() {
    val gson = Gson()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalOrderUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val orderUser = baedalOrderUsers[position]
        holder.bind(orderUser)
    }

    override fun getItemCount(): Int {
        return baedalOrderUsers.size
    }

    inner class CustomViewHolder(var binding: LytBaedalOrderUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BaedalOrderUser) {
            if (item.isMyOrder) binding.tvOrderUser.text = "주문금액: ${item.price}"
            else binding.tvOrderUser.text = "주문자: ${item.nickName}  주문금액: ${item.price}"
            binding.rvOrderMenu.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val adapter = SelectedMenuAdapter(context, JSONArray(gson.toJson(item.menuList)), false)
            binding.rvOrderMenu.adapter = adapter
        }
    }
}