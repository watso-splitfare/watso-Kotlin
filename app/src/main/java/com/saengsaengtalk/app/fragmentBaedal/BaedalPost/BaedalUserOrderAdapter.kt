package com.saengsaengtalk.app.fragmentBaedal.BaedalPost

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saengsaengtalk.app.APIS.Order
import com.saengsaengtalk.app.APIS.UserOrder
import com.saengsaengtalk.app.databinding.LytBaedalOrderUserBinding
import com.saengsaengtalk.app.fragmentBaedal.BaedalConfirm.SelectedMenuAdapter
import java.lang.ref.WeakReference
import java.text.DecimalFormat


class BaedalUserOrderAdapter(val context: Context, val userOrders: List<UserOrder>, val isMyOrder: Boolean=false) :
    RecyclerView.Adapter<BaedalUserOrderAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalOrderUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val userOrder = userOrders[position]
        holder.bind(userOrder)
    }

    interface OnItemClickListener {
        fun onClick(orderId: String, menuName: String, menuPrice: Int, storeId: String)
    }

    private var listener = WeakReference<OnItemClickListener>(null)

    fun itemClick(orderId: String, menuName: String, menuPrice: Int, storeId: String) {
        listener.get()?.onClick(orderId, menuName, menuPrice, storeId)
    }

    fun addListener(listener: OnItemClickListener) {
        this.listener = WeakReference(listener)
    }


    override fun getItemCount(): Int {
        return userOrders.size
    }

    inner class CustomViewHolder(var binding: LytBaedalOrderUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(userOrder: UserOrder) {
            val dec = DecimalFormat("#,###")
            var sumPrice = 0
            userOrder.orders.forEach { sumPrice += it.price!! }
            if (userOrder.isMyOrder!!) binding.tvOrderUser.text = "주문금액: ${dec.format(sumPrice)}원"
            else binding.tvOrderUser.text = "주문자: ${userOrder.nickname}  주문금액: ${dec.format(sumPrice)}원"
            binding.rvOrderMenu.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val adapter = SelectedMenuAdapter(context, userOrder.orders, false, isMyOrder)
            binding.rvOrderMenu.adapter = adapter
        }
    }
}