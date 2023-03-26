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
    interface OnUpdateBtnListener {
        fun onUpdateOrder(order: Order)
    }
    interface OnDeleteBtnListener {
        fun onDeleteOrder(orderId: String)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }
    fun setUpdateBtnListener(onUpdateBtnListener: OnUpdateBtnListener) {
        this.updateBtnListener = onUpdateBtnListener
    }
    fun setDeleteBtnListener(deleteBtnListener: OnDeleteBtnListener) {
        this.deleteBtnListener = deleteBtnListener
    }

    private lateinit var itemClickListener : OnItemClickListener
    private lateinit var updateBtnListener : OnUpdateBtnListener
    private lateinit var deleteBtnListener : OnDeleteBtnListener

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
                binding.lytUpdate.visibility = View.GONE
            } else {
                //binding.divider.visibility = View.GONE
                binding.lytRectify.visibility = View.GONE
                binding.btnRemove.visibility = View.GONE
                binding.tvPrice.visibility = View.GONE
                if (!isMyOrder)
                    binding.lytUpdate.visibility = View.GONE
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

            binding.btnUpdate.setOnClickListener {
                updateBtnListener.onUpdateOrder(order)
            }
            binding.btnDelete.setOnClickListener {
                deleteBtnListener.onDeleteOrder(order._id!!)
            }
        }

        fun setBindText(priceString: String, quantity: Int) {
            Log.d("SelectedMenuAdapter-quantity, priceString",
                quantity.toString() + ", " + priceString)
            binding.tvQuantityString.text = "${quantity}개 (${priceString})"
            binding.tvPrice.text = priceString
            binding.tvQuantity.text = quantity.toString()
        }
    }
}