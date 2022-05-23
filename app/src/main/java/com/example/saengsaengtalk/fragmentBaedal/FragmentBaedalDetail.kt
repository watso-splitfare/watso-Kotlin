package com.example.saengsaengtalk.fragmentBaedal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.adapterBaedal.*
import com.example.saengsaengtalk.databinding.FragBaedalMenuDetailBinding
import org.json.JSONObject

class FragmentBaedalDetail :Fragment() {
    var menu: JSONObject? = null

    private var mBinding: FragBaedalMenuDetailBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val jsonString = it.getString("menu")
            menu = JSONObject(jsonString)
            println("디테일 프래그먼트: ${jsonString}")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalMenuDetailBinding.inflate(inflater, container, false)

        refreshView()

        binding.btnPrevious.setOnClickListener { onBackPressed() }

        return binding.root
    }

    fun refreshView() {
        var areaMenu = mutableListOf<BaedalDetailArea>()

        val id = menu!!.getInt("id")
        val menuName = menu!!.getString("menuName")
        val radios = menu!!.getJSONArray("radio")
        val combos = menu!!.getJSONArray("combo")

        for (i in 0 until radios.length()) {
            val radio = radios.getJSONObject(i)
            var temp = mutableListOf<BaedalDetail>()
            val area = radio.getString("area")
            val opts = radio.getJSONArray("option")

            for (j in 0 until opts.length()){
                val opt = opts.getJSONObject(j)
                temp.add(BaedalDetail(
                    opt.getInt("rnum"),
                    opt.getString("optName"),
                    "${opt.getString("price")}원",
                true))
            }
            areaMenu.add(BaedalDetailArea(area, temp))
        }

        if (combos[0] != "") {
            for (i in 0 until combos.length()) {
                val combo = combos.getJSONObject(i)

                var temp = mutableListOf<BaedalDetail>()
                val area = combo.getString("area")
                val opts = combo.getJSONArray("option")
                val min = combo.getInt("min")
                val max = combo.getInt("max")

                for (j in 0 until opts.length()) {

                    val opt = opts.getJSONObject(j)
                    temp.add(
                        BaedalDetail(
                            opt.getInt("cnum"),
                            opt.getString("optName"),
                            "${opt.getString("price")}원",
                            false, min, max
                        )
                    )
                }
                areaMenu.add(BaedalDetailArea(area, temp))

            }
        }

        binding.rvMenu.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvMenu.setHasFixedSize(true)

        val adapter = BaedalDetailAreaAdapter(requireContext(), areaMenu)
        binding.rvMenu.adapter = adapter

        binding.rvMenu.addItemDecoration(BaedalDetailAreaAdapter.BaedalDetailAreaAdapterDecoration())

        adapter.notifyDataSetChanged()
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String?> = mapOf("none" to null)) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }

    fun onBackPressed() {
        val mActivity = activity as MainActivity
        mActivity.onBackPressed()
    }
}