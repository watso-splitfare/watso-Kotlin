package com.example.saengsaengtalk

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.adapterBaedal.BaedalList
import com.example.saengsaengtalk.adapterBaedal.BaedalListAdapter
import com.example.saengsaengtalk.adapterHome.BaedalPreAdapter
import com.example.saengsaengtalk.databinding.FragBaedalBinding
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
            BaedalList(7, true,43, "네네치킨 같이 드실분~~", LocalDateTime.now(), "네네치킨", 3, 10000, 1),
            BaedalList(3, false,11, "치킨 같이 드실분~~", LocalDateTime.now(), "네네치킨", 2, 10000, 2),
            BaedalList(5, false,35, "같이 드실분~~", LocalDateTime.now(), "네네치킨", 1, 10000, 3),
            BaedalList(6, false,24, "드실분~~", LocalDateTime.now(), "네네치킨", 3, 10000, 4),
            BaedalList(2, true, 13, "네네치킨~~", LocalDateTime.now(), "네네치킨", 2, 10000, 5),
            BaedalList(6, false,7, "네네치킨 드실분~~", LocalDateTime.now(), "네네치킨", 4, 10000, 6)

        )
        binding.rvBaedalList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvBaedalList.setHasFixedSize(true)

        val adapter = BaedalListAdapter(baedalList)
        binding.rvBaedalList.adapter = adapter
        adapter.setItemClickListener(object: BaedalListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                Toast.makeText(v.context, "${baedalList[position].postNum}번", Toast.LENGTH_SHORT).show()
                Log.d("배달프래그먼트 온클릭", "${baedalList[position].postNum}")
                setDataAtFrag(FragmentBaedalPost(), baedalList[position].postNum.toString(), true)
            }
        })
        adapter.notifyDataSetChanged()

        binding.rvBaedalList.addItemDecoration(BaedalListAdapter.BaedalListAdapterDecoration())
    }

    fun setFrag(fragment: Fragment, addBackStack:Boolean=false, popAllStack:Boolean=false) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, addBackStack, popAllStack)
    }
    fun setDataAtFrag(fragment: Fragment, postNum:String, addBackStack:Boolean=false, popAllStack:Boolean=false) {
        val mActivity = activity as MainActivity
        mActivity.setDataAtFrag(fragment, postNum, addBackStack, popAllStack)
    }
}