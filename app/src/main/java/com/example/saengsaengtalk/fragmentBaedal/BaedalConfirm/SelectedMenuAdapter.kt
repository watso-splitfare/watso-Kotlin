package com.example.saengsaengtalk.fragmentBaedal.BaedalConfirm

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytBaedalConfirmMenuBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat


class SelectedMenuAdapter(val context: Context, val orders: JSONArray, val isRectifiable: Boolean=true):
    RecyclerView.Adapter<SelectedMenuAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalConfirmMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val order = orders.getJSONObject(position)
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
        return orders.length()
    }

    inner class CustomViewHolder(var binding: LytBaedalConfirmMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: JSONObject) {
            val dec = DecimalFormat("#,###")
            var menuPrice = " (${dec.format(order.getInt("menuPrice"))}원)"
            var count = order.getInt("count")
            var sumPrice = order.getInt("sumPrice")
            var priceString = "${dec.format(sumPrice * count)}원"

            if (isRectifiable) {
                binding.tvCountString.visibility = View.GONE
            } else {
                binding.divider.visibility = View.GONE
                binding.btnSub.visibility = View.GONE
                binding.btnAdd.visibility = View.GONE
                binding.btnRemove.visibility = View.GONE
                binding.tvCount.visibility = View.GONE
                binding.tvPrice.visibility = View.GONE
            }

            binding.tvMenuName.text = order.getString("menuName") + menuPrice

            binding.rvMenu.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            val adapter = SelectedOptionAdapter(order.getJSONArray("groups"))
            binding.rvMenu.adapter = adapter

            setBindText(priceString, count)

            binding.btnRemove.setOnClickListener {
                itemClickListener.onChange(adapterPosition, "remove")
                binding.lytBaedalConfirm.visibility = View.GONE
            }
            binding.btnSub.setOnClickListener {
                if (count > 1) {
                    count -= 1
                    priceString = "${dec.format(sumPrice * count)}원"
                    setBindText(priceString, count)
                    itemClickListener.onChange(adapterPosition, "sub")
                }
            }
            binding.btnAdd.setOnClickListener {
                if (count < 10) {
                    count += 1
                    priceString = "${dec.format(sumPrice * count)}원"
                    setBindText(priceString, count)
                    itemClickListener.onChange(adapterPosition, "add")
                }
            }
        }

        fun setBindText(priceString: String, count: Int) {
            binding.tvCountString.text = "${count}개 (${priceString})"
            binding.tvPrice.text = priceString
            binding.tvCount.text = count.toString()
        }
    }
}