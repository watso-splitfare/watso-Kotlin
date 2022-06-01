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
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytBaedalOptAreaBinding
import java.lang.ref.WeakReference


class BaedalOptAreaAdapter(val context: Context, val baedalOptArea: MutableList<BaedalOptArea>) : RecyclerView.Adapter<BaedalOptAreaAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalOptAreaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = baedalOptArea[position]
        holder.bind(item)
    }

    interface OnItemClickListener {
        fun onClick(isRadio: Boolean, area: String, num: Int, isChecked: Boolean)
    }

    private var listener = WeakReference<OnItemClickListener>(null)

    fun itemClick(isRadio: Boolean, area: String, num: Int, isChecked: Boolean) {
        listener.get()?.onClick(isRadio, area, num, isChecked)
    }

    fun addListener(listener: BaedalOptAreaAdapter.OnItemClickListener) {
        this.listener = WeakReference(listener)
    }

    override fun getItemCount(): Int {
        return baedalOptArea.size
    }

    inner class CustomViewHolder(var binding: LytBaedalOptAreaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BaedalOptArea) {
            binding.tvArea.text = item.area

            binding.rvMenuArea.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val adapter = BaedalOptAdapter(item.areaList)
            binding.rvMenuArea.adapter = adapter

            adapter.setItemClickListener(object: BaedalOptAdapter.OnItemClickListener{
                override fun onClick(isRadio: Boolean, area: String, num: Int, isChecked: Boolean) {
                    itemClick(isRadio, area, num, isChecked)
                }
            })
        }
    }

    class BaedalOptAreaAdapterDecoration : RecyclerView.ItemDecoration() {
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