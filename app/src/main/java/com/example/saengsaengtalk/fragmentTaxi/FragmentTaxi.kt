package com.example.saengsaengtalk.fragmentTaxi

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
import com.example.saengsaengtalk.APIS.DataModels.TaxiPostPreviewModel
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.databinding.FragTaxiBinding
import com.example.saengsaengtalk.fragmentBaedal.Baedal.BaedalListAdapter
import com.example.saengsaengtalk.fragmentBaedal.BaedalPost.FragmentBaedalPost
import com.example.saengsaengtalk.fragmentTaxi.adapterTaxi.TaxiTable
import com.example.saengsaengtalk.fragmentTaxi.adapterTaxi.TaxiTableAdapter
import com.example.saengsaengtalk.fragmentTaxi.adapterTaxi.TaxiTableRow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime

class FragmentTaxi :Fragment() {
    val fragIndex = 2

    private var mBinding: FragTaxiBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragTaxiBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnBaedalPostAdd.setOnClickListener { setFrag(FragmentTaxiAdd()) }
        binding.constraintLayout8.visibility = View.GONE    // 출발지, 목적지 필터 숨김
        binding.constraintLayout9.visibility = View.GONE
        binding.textView7.visibility = View.GONE            // 예상비용 숨김
        getPostPreview()
    }

    fun getPostPreview() {
        api.getTaxiPostListPreview().enqueue(object : Callback<List<TaxiPostPreviewModel>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<TaxiPostPreviewModel>>, response: Response<List<TaxiPostPreviewModel>>) {
                val taxiPosts = response.body()!!
                mappingAdapter(taxiPosts)
                Log.d("log", response.toString())
                Log.d("log", taxiPosts.toString())
            }

            override fun onFailure(call: Call<List<TaxiPostPreviewModel>>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun mappingAdapter(taxiPosts: List<TaxiPostPreviewModel>) {
        val posts = mutableListOf<TaxiTable>()
        val dateList = mutableListOf<LocalDate>()
        for (post in taxiPosts) {
            val dest_date = LocalDate.parse(post.depart_time.substring(0 until 10))
            if (dest_date !in dateList) {
                dateList.add(dest_date)
                posts.add(TaxiTable(dest_date, mutableListOf()))
            }
            val idx = dateList.indexOf(dest_date)
            posts[idx].rows.add(TaxiTableRow(
                post.post_id, post.depart_name, post.dest_name, LocalDateTime.parse(post.depart_time), post.join_users.size))
        }
        binding.rvTaxiTable.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvTaxiTable.setHasFixedSize(true)

        val adapter = TaxiTableAdapter(requireContext(), posts)
        binding.rvTaxiTable.adapter = adapter
        adapter.addListener(object: TaxiTableAdapter.OnItemClickListener{
            override fun onClick(postId: String) {
                setFrag(FragmentTaxiPost(), mapOf("postId" to postId))
            }
        })
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        println("setIndex = ${fragIndex}")
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, fragIndex=fragIndex)
    }
}