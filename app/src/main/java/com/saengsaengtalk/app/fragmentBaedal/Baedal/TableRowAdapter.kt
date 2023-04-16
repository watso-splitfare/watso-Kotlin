package com.saengsaengtalk.app.fragmentBaedal.Baedal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saengsaengtalk.app.APIS.BaedalPost
import com.saengsaengtalk.app.databinding.LytBaedalTableRowBinding

class TableRowAdapter(val tableRows: List<BaedalPost>) : RecyclerView.Adapter<TableRowAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalTableRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val row = tableRows.get(position)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(row._id)
        }
        holder.bind(row)
    }

    interface OnItemClickListener {
        fun onClick(postId: String)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return tableRows.size
    }

    class CustomViewHolder(var binding: LytBaedalTableRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: BaedalPost) {
            val currentMember = post.users.size.toString()
            val maxMember = post.maxMember

            binding.tvTitle.text = post.title
            binding.tvPlace.text = post.place
            binding.tvMember.text = currentMember + " / " + maxMember + "명"
            binding.tvNickname.text = post.nickname
        }
    }
}