package com.example.saengsaengtalk.adapterBaedal

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytBaedalMenuSectionBinding
import java.lang.ref.WeakReference


class BaedalMenuSectionAdapter(val context: Context, val baedalMenuSection: MutableList<BaedalMenuSection>) : RecyclerView.Adapter<BaedalMenuSectionAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalMenuSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = baedalMenuSection[position]
        holder.bind(item)
    }


    interface OnItemClickListener {
        fun onClick(id: Int)
    }

    private var listener = WeakReference<OnItemClickListener>(null)

    fun itemClick(id: Int) {
        listener.get()?.onClick(id)
    }

    fun addListener(listener: BaedalMenuSectionAdapter.OnItemClickListener) {
        this.listener = WeakReference(listener)
    }

    override fun getItemCount(): Int {
        return baedalMenuSection.size
    }

    inner class CustomViewHolder(var binding: LytBaedalMenuSectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BaedalMenuSection) {
            binding.tvSection.text = item.section
            binding.lytSection.setOnClickListener{
                if (binding.rvMenuSection.visibility == View.VISIBLE) {
                    binding.rvMenuSection.visibility = View.GONE
                    binding.ivArrow.setRotation(180f)
                }
                else {
                    binding.rvMenuSection.visibility = View.VISIBLE
                    binding.ivArrow.setRotation(0f)
                }
            }

            binding.rvMenuSection.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            //binding.rvMenuSection.addItemDecoration(BaedalMenuAdapter.BaedalMenuAdapterDecoration())

            val adapter = BaedalMenuAdapter(item.sectionList)
            binding.rvMenuSection.adapter = adapter

            adapter.setItemClickListener(object: BaedalMenuAdapter.OnItemClickListener{
                override fun onClick(id:Int) {
                    itemClick(id)
                }
            })
        }
    }
}