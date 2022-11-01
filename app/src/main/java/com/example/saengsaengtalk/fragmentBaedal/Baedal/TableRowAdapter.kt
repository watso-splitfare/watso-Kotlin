package com.example.saengsaengtalk.fragmentBaedal.Baedal

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.APIS.BaedalPostPreviewModel
import com.example.saengsaengtalk.databinding.LytBaedalTableRowBinding
import com.example.saengsaengtalk.databinding.LytTaxiTableRowBinding
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TableRowAdapter(val tableRows: List<BaedalPostPreviewModel>) : RecyclerView.Adapter<TableRowAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalTableRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(post: BaedalPostPreviewModel) {
            binding.imgBaedalListLike.visibility = View.GONE
            binding.imgBaedalListViewed.visibility = View.GONE
            binding.tvBaedalListLike.visibility = View.GONE
            binding.tvBaedalListViewed.visibility = View.GONE

            val dec = DecimalFormat("#,###")
            val currentMember = post.join_user.size
            //val text = "%s\n".format(post.title) + getDateTimeFormating(post.order_time) + "\n%s\n%d팀\n예상 배달비 %s원".format(post.store.store_name, currentMember, dec.format(post.store.fee/currentMember))
            val text = "${post.title}\n" +
                    "${getDateTimeFormating(post.order_time)}\n" +
                    "${post.store.store_name}\n" +
                    "${currentMember}팀"
            binding.tvBaedalListContent.text = text

        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getDateTimeFormating(dateTimeStr: String): String {
            val dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            val formatter = DateTimeFormatter.ofPattern("HH시 mm분").withLocale(Locale.forLanguageTag("ko"))
            return dateTime.format(formatter)
        }
    }
}