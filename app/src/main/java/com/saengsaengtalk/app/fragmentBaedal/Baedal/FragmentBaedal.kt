package com.saengsaengtalk.app.fragmentBaedal.Baedal

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.saengsaengtalk.app.APIS.BaedalPostPreview
import com.saengsaengtalk.app.LoopingDialog
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.databinding.FragBaedalBinding
import com.saengsaengtalk.app.fragmentBaedal.BaedalAdd.FragmentBaedalAdd
import com.saengsaengtalk.app.fragmentBaedal.BaedalPost.FragmentBaedalPost
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        val loopingDialog = looping()
        api.getBaedalPostList().enqueue(object : Callback<List<BaedalPostPreview>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<BaedalPostPreview>>, response: Response<List<BaedalPostPreview>>) {
                if (response.code() == 200) {
                    val baedalPosts = response.body()!!.sortedBy { it.orderTime }
                    mappingAdapter(baedalPosts)
                } else {
                    Log.e("baedal Fragment - getBaedalPostList", response.toString())
                    makeToast("배달 게시글 리스트를 조회하지 못했습니다.")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<List<BaedalPostPreview>>, t: Throwable) {
                Log.e("home Fragment - getBaedalOrderListPreview", t.message.toString())
                makeToast("배달 게시글 리스트를 조회하지 못했습니다.")
                looping(false, loopingDialog)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun mappingAdapter(baedalPosts: List<BaedalPostPreview>) {
        binding.rvBaedalList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvBaedalList.setHasFixedSize(true)

        val tables = mutableListOf<Table>()
        val dates = mutableListOf<LocalDate>()
        var tableIdx = -1
            for (post in baedalPosts) {
            val date = LocalDate.parse(post.orderTime.substring(0 until 16), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            if (date !in dates) {
                dates.add(date)
                tables.add(Table(date, mutableListOf(post)))
                tableIdx += 1
            } else tables[tableIdx].rows.add(post)
        }

        if (tables.size > 0) {
            val adapter = TableAdapter(requireContext(), tables)
            binding.rvBaedalList.adapter = adapter
            adapter.addListener(object : TableAdapter.OnItemClickListener {
                override fun onClick(postId: String) {
                    Log.d("배달프래그먼트 온클릭", "${postId}")
                    setFrag(FragmentBaedalPost(), mapOf("postId" to postId))
                }
            })
        }
    }

    fun looping(loopStart: Boolean = true, loopingDialog: LoopingDialog? = null): LoopingDialog? {
        val mActivity = activity as MainActivity
        return mActivity.looping(loopStart, loopingDialog)
    }

    fun makeToast(message: String){
        val mActivity = activity as MainActivity
        mActivity.makeToast(message)
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, fragIndex=fragIndex)
    }
}