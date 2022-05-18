package com.example.saengsaengtalk.fragmentBaedal

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.adapterBaedal.BaedalComment
import com.example.saengsaengtalk.adapterBaedal.BaedalCommentAdapter
import com.example.saengsaengtalk.databinding.FragBaedalMenuBinding
import com.example.saengsaengtalk.databinding.FragBaedalPostBinding
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentBaedalMenu :Fragment() {
    var postNum: String? = null
    var storeId: String? = null

    private var mBinding: FragBaedalMenuBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postNum = it.getString("postNum")
            storeId = it.getString("storeId")
        }

        Log.d("배달 메뉴", "게시물 번호: ${postNum}")
        Log.d("배달 메뉴", "스토어 id: ${storeId}")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalMenuBinding.inflate(inflater, container, false)

        refreshView()

        binding.btnPrevious.setOnClickListener { onBackPressed() }

        return binding.root
    }

    fun refreshView() {
        val assetManager = resources.assets
        val jsonString = assetManager.open("nene.json").bufferedReader().use { it.readText() }

        //Log.d("제이슨: ", jsonString)
        val jObject = JSONObject(jsonString)
        val jArray = jObject.getJSONArray("nene")

        for (i in 0 until jArray.length()) {
            val obj = jArray.getJSONObject(i)
            val section = obj.getString("section")
            val name = obj.getString("name")
            val radio = obj.getJSONArray("radio")
            val combo = obj.getJSONArray("combo")
            Log.d("${i+1}번째 메뉴 ", "${section}\t${name}")

            try {
                for (j in 0 until radio.length()) {
                    val radio_ = radio.getJSONObject(j)
                    val radio_area = radio_.getString("area")
                    val radio_name = radio_.getString("name")
                    val radio_price = radio_.getString("price")
                    Log.d("${i + 1}번째 메뉴 ${radio_area}", "${radio_name}\t${radio_price}")
                }
            } catch (e: JSONException) {
                Log.d("${i + 1} 번째 메뉴", "라디오버튼 없음")
            }

            try {
                for (k in 0 until combo.length()) {
                    val combo_ = combo.getJSONObject(k)
                    val combo_area = combo_.getString("area")
                    val combo_name = combo_.getString("name")
                    val combo_price = combo_.getString("price")
                    Log.d("${i + 1}번째 메뉴 ${combo_area}", "${combo_name}\t${combo_price}")
                }
            } catch (e: JSONException) {
                Log.d("${i + 1} 번째 메뉴", "콤보박스 없음")
            }
        }

    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}