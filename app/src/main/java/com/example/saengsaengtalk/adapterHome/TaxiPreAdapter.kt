package com.example.saengsaengtalk.adapterHome

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.APIS.BaedalPostPreviewModel
import com.example.saengsaengtalk.APIS.DataModels.TaxiPostPreviewModel
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.databinding.LytBaedalPreBinding
import com.example.saengsaengtalk.databinding.LytTaxiPreBinding
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class TaxiPreAdapter(val taxiPosts: List<TaxiPostPreviewModel>) : RecyclerView.Adapter<TaxiPreAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaxiPreAdapter.CustomViewHolder {
        val binding = LytTaxiPreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: TaxiPreAdapter.CustomViewHolder, position: Int) {
        val post = taxiPosts[position]
        holder.bind(post)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return taxiPosts.size
    }

    inner class CustomViewHolder(var binding: LytTaxiPreBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(post: TaxiPostPreviewModel) {
            val dec = DecimalFormat("#,###원")
            val currentMember = post.join_users.size
            val text = getDateTimeFormating(post.depart_time) + ("\n${post.depart_name} -> ${post.depart_name}\n" +
                    "현인원 ${currentMember}명")/* +
                    "\n예상 택시비 ${dec.format(post.fee/currentMember)}")*/
            binding.tvTaxiPre.text = text
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getDateTimeFormating(dateTimeStr: String): String {
            val dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            val formatter = DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(Locale.forLanguageTag("ko"))
            return dateTime.format(formatter)
        }
    }

}