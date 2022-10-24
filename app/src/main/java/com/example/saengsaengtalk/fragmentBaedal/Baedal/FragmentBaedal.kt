package com.example.saengsaengtalk.fragmentBaedal.Baedal

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.APIS.BaedalPostPreviewModel
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.databinding.FragBaedalBinding
import com.example.saengsaengtalk.fragmentBaedal.BaedalAdd.FragmentBaedalAdd
import com.example.saengsaengtalk.fragmentBaedal.BaedalPost.FragmentBaedalPost
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentBaedal :Fragment() {
    val fragIndex = 1

    private var mBinding: FragBaedalBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnBaedalPostAdd.setOnClickListener {
            setFrag(FragmentBaedalAdd())
        }
        binding.etBaedalSearch.visibility = View.GONE
        binding.btnBaedalSearch.visibility = View.GONE

        getPostPreview()
    }

    fun getPostPreview() {
        api.getBaedalOrderListPreview().enqueue(object : Callback<List<BaedalPostPreviewModel>> {
            override fun onResponse(call: Call<List<BaedalPostPreviewModel>>, response: Response<List<BaedalPostPreviewModel>>) {
                val baedalPosts = response.body()!!
                mappingAdapter(baedalPosts)
                Log.d("log", response.toString())
                Log.d("log", baedalPosts.toString())
            }

            override fun onFailure(call: Call<List<BaedalPostPreviewModel>>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }

    fun mappingAdapter(baedalPosts: List<BaedalPostPreviewModel>) {
        binding.rvBaedalList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvBaedalList.setHasFixedSize(true)

        val adapter = BaedalListAdapter(baedalPosts)
        binding.rvBaedalList.adapter = adapter
        adapter.setItemClickListener(object: BaedalListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                Log.d("배달프래그먼트 온클릭", "${baedalPosts[position]._id}")
                setFrag(FragmentBaedalPost(), mapOf("postId" to baedalPosts[position]._id))
            }
        })
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, fragIndex=fragIndex)
    }
}