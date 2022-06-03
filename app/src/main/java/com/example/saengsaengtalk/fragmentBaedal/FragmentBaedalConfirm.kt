package com.example.saengsaengtalk.fragmentBaedal

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.adapterBaedal.*
import com.example.saengsaengtalk.databinding.FragBaedalConfirmBinding
import org.json.JSONArray

class FragmentBaedalConfirm :Fragment() {
    var storeName: String? = null
    var opt: JSONArray? = null

    var menu = mutableListOf<BaedalConfirmMenu>()

    private var mBinding: FragBaedalConfirmBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            storeName = it.getString("storeName")
            opt = JSONArray(it.getString("opt"))
        }
        println("스토어이름: ${storeName}")
        println("메뉴: ${opt}")

    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalConfirmBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        binding.rvMenuSelected.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvMenuSelected.setHasFixedSize(true)

        for (i in 0 until opt!!.length()) {
            val obj = opt!!.getJSONObject(i)
            val menuName = obj.getString("menuName")
            val count = obj.getInt("count")

            val strings = mutableListOf<BaedalConfirm>()
            val stringArray = obj.getJSONArray("optString")
            for (j in 0 until stringArray.length()) {
                strings.add(BaedalConfirm(stringArray.getString(j)))
            }

            menu.add(BaedalConfirmMenu(menuName,count,strings))
        }
        val adapter = BaedalConfirmMenuAdapter(requireContext(), menu)
        binding.rvMenuSelected.adapter = adapter
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}