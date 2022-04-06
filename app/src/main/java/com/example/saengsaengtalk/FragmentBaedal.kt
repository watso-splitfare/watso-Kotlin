package com.example.saengsaengtalk

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.adapterHome.BaedalPre
import com.example.saengsaengtalk.adapterOthers.BaedalList
import com.example.saengsaengtalk.adapterOthers.BaedalListAdapter
import com.example.saengsaengtalk.databinding.FragBaedalBinding
import com.example.saengsaengtalk.databinding.FragHomeBinding
import java.time.LocalDateTime

class FragmentBaedal :Fragment() {

    private var mBinding: FragBaedalBinding? = null
    private val binding get() = mBinding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //val view = inflater.inflate(R.layout.frag_baedal, container, false)

        mBinding = FragBaedalBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
        //return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {

        /* 배달 */
        val baedalList = arrayListOf(
            BaedalList(7, true,43, "네네치킨 같이 드실분~~", LocalDateTime.now(), "네네치킨", 3, 10000),
            BaedalList(3, false,11, "치킨 같이 드실분~~", LocalDateTime.now(), "네네치킨", 2, 10000),
            BaedalList(5, false,35, "같이 드실분~~", LocalDateTime.now(), "네네치킨", 1, 10000),
            BaedalList(6, false,24, "드실분~~", LocalDateTime.now(), "네네치킨", 3, 10000),
            BaedalList(2, true, 13, "네네치킨~~", LocalDateTime.now(), "네네치킨", 2, 10000),
            BaedalList(6, false,7, "네네치킨 드실분~~", LocalDateTime.now(), "네네치킨", 4, 10000)

        )
        binding.rvBaedalList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvBaedalList.setHasFixedSize(true)
        binding.rvBaedalList.adapter = BaedalListAdapter(baedalList)
    }
}