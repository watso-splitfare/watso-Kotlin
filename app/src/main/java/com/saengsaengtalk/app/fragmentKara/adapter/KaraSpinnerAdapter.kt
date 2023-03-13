package com.saengsaengtalk.app.fragmentKara.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.saengsaengtalk.app.R


class KaraSpinnerAdapter(internal var context: Context, var spinner: MutableList<KaraSpinner>) :
    BaseAdapter() {
    internal var inflter: LayoutInflater

    init {
        inflter = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return spinner.size
    }

    override fun getItem(i: Int): Any? {
        return null
    }

    override fun getItemId(i: Int): Long {
        return 0
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {

        val view = inflter.inflate(R.layout.lyt_kara_spinner,null)
        val num = view.findViewById<View>(R.id.tv_number) as TextView?
        val light = view.findViewById<View>(R.id.view_light) as View?
        num!!.text = spinner[i].Number
        if (spinner[i].useAble) light!!.setBackgroundResource(R.drawable.box_style_kara_useable)
        else light!!.setBackgroundResource(R.drawable.box_style_kara_unusable)
        return view
    }
}