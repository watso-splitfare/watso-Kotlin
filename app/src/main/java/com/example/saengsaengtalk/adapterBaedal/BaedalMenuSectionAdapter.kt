package com.example.saengsaengtalk.adapterBaedal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.databinding.LytBaedalMenuSectionBinding
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class BaedalMenuSectionAdapter(val context: Context, val baedalMenuSection: MutableList<BaedalMenuSection>) : RecyclerView.Adapter<BaedalMenuSectionAdapter.CustomViewHolder>() {

    /*override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaedalMenuSectionAdapter.Holder {
        val binding = LytBaedalMenuSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val section = baedalMenuSection[position]
        holder.bind(section)
    }

    override fun getItemCount(): Int {
        return baedalMenuSection.size
    }

    inner class Holder(var binding: LytBaedalMenuSectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BaedalMenuSection) {
            binding.rvMenuSection = item

            binding.
        }
    }*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        //val view = LayoutInflater.from(parent.context).inflate(R.layout.lyt_baedal_menu_section, parent, false)
        //return CustomViewHolder(view)
        val binding = LytBaedalMenuSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = baedalMenuSection[position]
        holder.bind(item)
    }


    override fun getItemCount(): Int {
        return baedalMenuSection.size
    }

    inner class CustomViewHolder(var binding: LytBaedalMenuSectionBinding) : RecyclerView.ViewHolder(binding.root) {
        //val tv_section = itemView.findViewById<TextView>(R.id.tv_section)
        //var rv_menu = itemView.findViewById<RecyclerView>(R.id.rv_menu_section)
        fun bind(item: BaedalMenuSection) {
            binding.tvSection.text = item.section
            binding.rvMenuSection.adapter = BaedalMenuAdapter(item.sectionList)
            binding.rvMenuSection.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    class BaedalMenuSectionAdapterDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)

            val position = parent.getChildAdapterPosition(view)
            val count = state.itemCount
            val offset = 20

            if(position == 0) {
                outRect.top = offset
            } else if(position ==  count-1) {
                outRect.bottom = offset
            } else {
                outRect.top = offset
                outRect.bottom = offset
            }
        }

        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDraw(c, parent, state)

            val paint = Paint()
            paint.color = Color.GRAY

            val left = parent.paddingStart.toFloat()
            val right = (parent.width - parent.paddingEnd).toFloat()

            for(i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val layoutParams = child.layoutParams as RecyclerView.LayoutParams
                val top = (child.bottom + layoutParams.bottomMargin + 20).toFloat()
                val bottom = top + 1f

                c.drawRect(left, top, right, bottom, paint)
            }
        }
    }
}