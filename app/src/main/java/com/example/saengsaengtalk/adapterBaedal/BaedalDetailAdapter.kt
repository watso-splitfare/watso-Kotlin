package com.example.saengsaengtalk.adapterBaedal

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.R
import org.json.JSONObject

class BaedalDetailAdapter(val baedalDetail: MutableList<BaedalDetail>) : RecyclerView.Adapter<BaedalDetailAdapter.CustomViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lyt_baedal_detail, parent, false)
        return CustomViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val arg = baedalDetail.get(position)
        holder.rb_menu.text = arg.optName
        holder.tv_price.text = arg.price
        holder.itemView.setOnClickListener { itemClickListener.onClick(arg.num) }
    }

    interface OnItemClickListener {
        fun onClick(id: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return baedalDetail.size
    }

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rb_menu = itemView.findViewById<RadioButton>(R.id.rb_menu)
        val tv_price = itemView.findViewById<TextView>(R.id.tv_price)
    }

    class BaedalDetailAdapterDecoration : RecyclerView.ItemDecoration() {
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