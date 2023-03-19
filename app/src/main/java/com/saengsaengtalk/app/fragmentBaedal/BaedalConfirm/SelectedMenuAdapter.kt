package com.saengsaengtalk.app.fragmentBaedal.BaedalConfirm

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saengsaengtalk.app.APIS.Order
import com.saengsaengtalk.app.databinding.LytBaedalConfirmMenuBinding
import java.text.DecimalFormat


class SelectedMenuAdapter(val context: Context, val orders: List<Order>, val isRectifiable: Boolean=true):
    RecyclerView.Adapter<SelectedMenuAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalConfirmMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        fun bind(order: Order) {
            val dec = DecimalFormat("#,###")
            var menuPrice = " (${dec.format(order.menu.price)}원)"
            var quantity = order.quantity
            var orderPrice = order.price
            var priceString = "${dec.format(orderPrice * quantity)}원"

            if (isRectifiable) {
                binding.tvQuantityString.visibility = View.GONE
            } else {
                binding.divider.visibility = View.GONE
                binding.btnSub.visibility = View.GONE
                binding.btnAdd.visibility = View.GONE
                binding.btnRemove.visibility = View.GONE
                binding.tvQuantity.visibility = View.GONE
                binding.tvPrice.visibility = View.GONE
            }

            binding.tvMenuName.text = order.menu.name + menuPrice

            if (order.menu.groups != null) {
                binding.rvMenu.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                val adapter = SelectedOptionAdapter(order.menu.groups)
                binding.rvMenu.adapter = adapter
            }
            setBindText(priceString, quantity)

            binding.btnRemove.setOnClickListener {
                itemClickListener.onChange(adapterPosition, "remove")
                binding.lytBaedalConfirm.visibility = View.GONE
            }
            binding.btnSub.setOnClickListener {
                if (quantity > 1) {
                    quantity -= 1
                    priceString = "${dec.format(orderPrice * quantity)}원"
                    setBindText(priceString, quantity)
                    itemClickListener.onChange(adapterPosition, "sub")
                }
            }
            binding.btnAdd.setOnClickListener {
                if (quantity < 10) {
                    quantity += 1
                    priceString = "${dec.format(orderPrice * quantity)}원"
                    setBindText(priceString, quantity)
                    itemClickListener.onChange(adapterPosition, "add")
                }
            }
        }

        fun setBindText(priceString: String, quantity: Int) {
            binding.tvQuantityString.text = "${quantity}개 (${priceString})"
            binding.tvPrice.text = priceString
            binding.tvQuantity.text = quantity.toString()
        }
    }
}