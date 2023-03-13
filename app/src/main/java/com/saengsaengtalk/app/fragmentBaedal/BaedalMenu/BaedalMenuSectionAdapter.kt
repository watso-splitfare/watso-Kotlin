package com.saengsaengtalk.app.fragmentBaedal.BaedalMenu

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saengsaengtalk.app.databinding.LytBaedalMenuSectionBinding
import java.lang.ref.WeakReference


class BaedalMenuSectionAdapter(val context: Context, val sections: List<Section>) : RecyclerView.Adapter<BaedalMenuSectionAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalMenuSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val section = sections[position]
        holder.bind(section)
    }

    interface OnItemClickListener {
        fun onClick(sectionName: String, menuName: String)
    }

    private var listener = WeakReference<OnItemClickListener>(null)

    fun itemClick(sectionName: String, menuName: String) {
        listener.get()?.onClick(sectionName, menuName)
    }

    fun addListener(listener: OnItemClickListener) {
        this.listener = WeakReference(listener)
    }

    override fun getItemCount(): Int {
        return sections.size
    }

    inner class CustomViewHolder(var binding: LytBaedalMenuSectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(section: Section) {
            binding.tvSection.text = section.name
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

            val adapter = BaedalMenuAdapter(section.menus)
            binding.rvMenuSection.adapter = adapter

            adapter.setItemClickListener(object: BaedalMenuAdapter.OnItemClickListener {
                override fun onClick(menuName: String) {
                    itemClick(section.name, menuName)
                }
            })
        }
    }
}