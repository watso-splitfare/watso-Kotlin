package com.saengsaengtalk.app.fragmentBaedal.BaedalOpt

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saengsaengtalk.app.APIS.Group
import com.saengsaengtalk.app.databinding.LytBaedalOptGroupBinding
import java.lang.ref.WeakReference


class BaedalOptGroupAdapter(val context: Context, val groups: List<Group>) : RecyclerView.Adapter<BaedalOptGroupAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalOptGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val group = groups[position]
        holder.bind(group)
    }

    interface OnItemClickListener {
        fun onClick(groupId: String, isRadio: Boolean, optionId: String, isChecked: Boolean)
    }

    private var listener = WeakReference<OnItemClickListener>(null)

    fun itemClick(groupId: String, isRadio:Boolean, optionId: String, isChecked: Boolean) {
        listener.get()?.onClick(groupId, isRadio, optionId, isChecked)
    }

    fun addListener(listener: OnItemClickListener) {
        this.listener = WeakReference(listener)
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    inner class CustomViewHolder(var binding: LytBaedalOptGroupBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(group: Group) {
            if (group.maxOrderQuantity > 1) {
                binding.tvGroup.text = group.name + " (최대 ${group.maxOrderQuantity.toString()}개)"
            } else binding.tvGroup.text = group.name

            binding.rvMenuGroup.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val adapter = BaedalOptAdapter(context, group.options, group.minOrderQuantity, group.maxOrderQuantity)
            binding.rvMenuGroup.adapter = adapter

            adapter.setItemClickListener(object: BaedalOptAdapter.OnItemClickListener {
                override fun onClick(isRadio:Boolean, optionId: String, isChecked: Boolean) {
                    itemClick(group._id, isRadio, optionId, isChecked)
                }
            })
        }
    }
}