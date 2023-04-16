package com.saengsaengtalk.app.fragmentBaedal.BaedalConfirm

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saengsaengtalk.app.APIS.Order
import com.saengsaengtalk.app.databinding.LytBaedalConfirmMenuBinding
import java.text.DecimalFormat


class SelectedMenuAdapter(val context: Context, val orders: List<Order>, val isRectifiable: Boolean=true, val isMyOrder: Boolean = false):
    RecyclerView.Adapter<SelectedMenuAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalConfirmMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        Log.d("SelectedMenuAdapter - orders", orders.toString())
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
    }

    interface OnItemClickListener {
        fun onChange(position: Int, change: String)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return orders.size
    }

    inner class CustomViewHolder(var binding: LytBaedalConfirmMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        val dec = DecimalFormat("#,###")
        //var menuPrice = "원"
        var quantity = 1
        var orderPrice = 0
        var priceString = "원"

        fun bind(order: Order) {
            orderPrice = order.price!!
            quantity = order.quantity

            if (isRectifiable) binding.tvQuantityString.visibility = View.GONE
            else {
                binding.lytRectify.visibility = View.GONE
                binding.btnRemove.visibility = View.GONE
                binding.tvPrice.visibility = View.GONE
            }

            binding.tvMenuName.text = order.menu.name

            if (order.menu.groups != null) {
                binding.rvMenu.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                val adapter = SelectedOptionAdapter(order.menu.groups)
                binding.rvMenu.adapter = adapter
            }
            setBindText()

            binding.btnRemove.setOnClickListener {
                itemClickListener.onChange(adapterPosition, "remove")
                binding.lytBaedalConfirm.visibility = View.GONE
            }
            binding.btnSub.setOnClickListener {
                if (quantity > 1) {
                    quantity -= 1
                    setBindText()
                    itemClickListener.onChange(adapterPosition, "sub")
                }
            }
            binding.btnAdd.setOnClickListener {
                if (quantity < 10) {
                    quantity += 1
                    setBindText()
                    itemClickListener.onChange(adapterPosition, "add")
                }
            }
        }

        fun setBindText() {
            priceString = "${dec.format(orderPrice * quantity)}원"
            Log.d("SelectedMenuAdapter-quantity, priceString",
                quantity.toString() + ", " + priceString)
            binding.tvQuantityString.text = "${quantity}개 (${priceString})"
            binding.tvPrice.text = priceString
            binding.tvQuantity.text = quantity.toString()
        }
    }
}