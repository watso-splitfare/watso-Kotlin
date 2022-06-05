package com.example.saengsaengtalk.adapterBaedal

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytBaedalConfirmMenuBinding
import java.text.DecimalFormat


class BaedalConfirmMenuAdapter(val context: Context, val baedalConfirmMenu: MutableList<BaedalConfirmMenu>) : RecyclerView.Adapter<BaedalConfirmMenuAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalConfirmMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

        val item = baedalConfirmMenu[position]
        holder.bind(item)
    }

    interface OnItemClickListener {
        fun onChange(position: Int, price: Int, change: String)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return baedalConfirmMenu.size
    }

    inner class CustomViewHolder(var binding: LytBaedalConfirmMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BaedalConfirmMenu) {
            val dec = DecimalFormat("#,###")
            var count = item.count
            var priceString = "${dec.format(item.price * count)}원"

            binding.tvMenu.text = item.menu

            binding.rvMenu.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            val adapter = BaedalConfirmAdapter(item.optList)
            binding.rvMenu.adapter = adapter

            binding.tvPrice.text = priceString
            binding.tvCount.text = count.toString()

            binding.btnRemove.setOnClickListener {
                itemClickListener.onChange(adapterPosition, item.price * count, "remove")
                binding.lytBaedalConfirm.visibility = View.GONE
            }
            binding.btnSub.setOnClickListener {
                if (count > 1) {
                    count -= 1
                    priceString = "${dec.format(item.price * count)}원"
                    binding.tvPrice.text = priceString
                    binding.tvCount.text = count.toString()
                    itemClickListener.onChange(adapterPosition, item.price, "sub")
                }
            }
            binding.btnAdd.setOnClickListener {
                if (count < 10) {
                    count += 1
                    priceString = "${dec.format(item.price * count)}원"
                    binding.tvPrice.text = priceString
                    binding.tvCount.text = count.toString()
                    itemClickListener.onChange(adapterPosition, item.price, "add")
                }
            }
        }
    }
}