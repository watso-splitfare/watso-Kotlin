package com.watso.app.fragmentBaedal.BaedalOpt

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.watso.app.API.Group
import com.watso.app.databinding.LytBaedalOptGroupBinding
import java.lang.ref.WeakReference


class BaedalOptGroupAdapter(val context: Context) : RecyclerView.Adapter<BaedalOptGroupAdapter.CustomViewHolder>() {

    private var groups = mutableListOf<Group>()

    fun setData(groupData: List<Group>) {
        groups.clear()
        groups.addAll(groupData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalOptGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val group = groups[position]
        holder.bind(group)
    }

    interface OnGroupOptClickListener { fun onClick(groupId: String, isRadio: Boolean, optionId: String, isChecked: Boolean) }

    fun setGroupOptClickListener(onGroupOptClickListener: OnGroupOptClickListener) { this.groupOptClickListener = onGroupOptClickListener }

    private lateinit var groupOptClickListener: OnGroupOptClickListener

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

            val adapter = BaedalOptAdapter(context)
            binding.rvMenuGroup.adapter = adapter
            adapter.setData(group)

            adapter.setOptionClickListener(object: BaedalOptAdapter.OnOptionClickListener {
                override fun onOptionClick(isRadio:Boolean, optionId: String, isChecked: Boolean) {
                    groupOptClickListener.onClick(group._id, isRadio, optionId, isChecked)
                }
            })
        }
    }
}