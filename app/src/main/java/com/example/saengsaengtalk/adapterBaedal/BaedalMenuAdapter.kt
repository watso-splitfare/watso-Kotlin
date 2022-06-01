package com.example.saengsaengtalk.adapterBaedal

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.databinding.LytBaedalMenuBinding

class BaedalMenuAdapter(val baedalMenu: MutableList<BaedalMenu>) : RecyclerView.Adapter<BaedalMenuAdapter.CustomViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val menu = baedalMenu.get(position)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(menu.id)
        }
        holder.bind(menu)
    }

    interface OnItemClickListener {
        fun onClick(id: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return baedalMenu.size
    }

    class CustomViewHolder(var binding: LytBaedalMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(menu: BaedalMenu) {
            binding.tvName.text = menu.menuName
            binding.tvPrice.text = menu.price
        }
    }

    class BaedalMenuAdapterDecoration : RecyclerView.ItemDecoration() {
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