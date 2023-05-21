package com.watso.app.fragmentTaxi

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.watso.app.API.*
import com.watso.app.ActivityController
import com.watso.app.MainActivity
import com.watso.app.databinding.FragTaxiBinding
import com.watso.app.fragmentTaxi.adapterTaxi.TaxiTable
import com.watso.app.fragmentTaxi.adapterTaxi.TaxiTableAdapter
import com.watso.app.fragmentTaxi.adapterTaxi.TaxiTableRow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime

class FragmentTaxi :Fragment() {
    lateinit var AC: ActivityController
    lateinit var fragmentContext: Context

    private var mBinding: FragTaxiBinding? = null
    private val binding get() = mBinding!!
    val api= API.create()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragTaxiBinding.inflate(inflater, container, false)
        AC = ActivityController(activity as MainActivity)

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
        AC.showProgressBar()
        api.getTaxiPostListPreview().enqueue(object : Callback<List<TaxiPostPreviewModel>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<TaxiPostPreviewModel>>, response: Response<List<TaxiPostPreviewModel>>) {
                if (response.code() == 200) {
                    val taxiPosts = response.body()!!.sortedBy { it.depart_time }
                    mappingAdapter(taxiPosts)
                } else {
                    Log.e("taxi Fragment - getTaxiPostListPreview", response.toString())
                    makeToast("택시 게시글 리스트를 조회하지 못했습니다.")
                }
                AC.hideProgressBar()
            }

            override fun onFailure(call: Call<List<TaxiPostPreviewModel>>, t: Throwable) {
                Log.e("taxi Fragment - getTaxiPostListPreview", t.message.toString())
                makeToast("택시 게시글 리스트를 조회하지 못했습니다.")
                AC.hideProgressBar()
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
            //println(post)
            posts[idx].rows.add(TaxiTableRow(
                post._id, post.depart_name, post.dest_name, LocalDateTime.parse(post.depart_time), post.join_users.size))
        }
        binding.rvTaxiTable.layoutManager = LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
        binding.rvTaxiTable.setHasFixedSize(true)

        val adapter = TaxiTableAdapter(fragmentContext, posts)
        binding.rvTaxiTable.adapter = adapter
        adapter.addListener(object: TaxiTableAdapter.OnItemClickListener{
            override fun onClick(postId: String) {
                setFrag(FragmentTaxiPost(), mapOf("postId" to postId))
            }
        })
    }

    fun makeToast(message: String){
        val mActivity = activity as MainActivity
        mActivity.makeToast(message)
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }
}