package com.watso.app.fragmentBaedal.BaedalOrders

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.watso.app.API.UserOrder
import com.watso.app.databinding.LytBaedalOrderUserBinding
import com.watso.app.fragmentBaedal.BaedalConfirm.SelectedMenuAdapter
import java.text.DecimalFormat


class BaedalUserOrderAdapter(val context: Context, val userOrders: MutableList<UserOrder>, val isMyOrder: Boolean=false) :
    RecyclerView.Adapter<BaedalUserOrderAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalOrderUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val userOrder = userOrders[position]
        Log.d("UserOrderAdapter onBindViewHolder${position}", userOrder.toString())
        holder.bind(userOrder)
    }

    override fun getItemCount(): Int {
        return userOrders.size
    }

    inner class CustomViewHolder(var binding: LytBaedalOrderUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(userOrder: UserOrder) {
            Log.d("userOrder 어댑터", userOrder.toString())
            val dec = DecimalFormat("#,###")
            var sumPrice = 0
            userOrder.orders.forEach { sumPrice += it.price!! * it.quantity }
            binding.tvNickname.text = "주문자: ${userOrder.nickname}"
            binding.tvTotalPrice.text = "주문금액: ${dec.format(sumPrice)}원"
            val requestComment = userOrder.requestComment
            if (requestComment.trim() == "") binding.lytRequest.visibility = View.GONE
            else binding.tvRequest.text = "요청사항: ${requestComment}"
            binding.rvOrderMenu.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val adapter = SelectedMenuAdapter(context, userOrder.orders, false, isMyOrder)
            binding.rvOrderMenu.adapter = adapter
        }
    }
}