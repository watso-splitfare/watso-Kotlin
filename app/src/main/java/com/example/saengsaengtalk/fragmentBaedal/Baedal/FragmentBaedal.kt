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
        api.getBaedalOrderListPreview().enqueue(object : Callback<List<BaedalPostPreviewModel>> {
            @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun mappingAdapter(baedalPosts: List<BaedalPostPreviewModel>) {
        binding.rvBaedalList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvBaedalList.setHasFixedSize(true)

        val tables = mutableListOf<Table>()
        val dates = mutableListOf<LocalDate>()
        var tableIdx = -1
            for (post in baedalPosts) {
            val date = LocalDate.parse(post.order_time, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            if (date !in dates) {
                dates.add(date)
                tables.add(Table(date, mutableListOf(post)))
                tableIdx += 1
            } else tables[tableIdx].rows.add(post)
        }

        val adapter = TableAdapter(requireContext(), tables)
        binding.rvBaedalList.adapter = adapter
        adapter.addListener(object: TableAdapter.OnItemClickListener{
            override fun onClick(postId: String) {
                Log.d("배달프래그먼트 온클릭", "${postId}")
                setFrag(FragmentBaedalPost(), mapOf("postId" to postId))
            }
        })
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, fragIndex=fragIndex)
    }
}