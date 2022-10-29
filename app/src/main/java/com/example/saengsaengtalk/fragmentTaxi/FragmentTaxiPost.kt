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
import com.example.saengsaengtalk.APIS.DataModels.TaxiPostModel
import com.example.saengsaengtalk.APIS.DataModels.TaxiPostPreviewModel
import com.example.saengsaengtalk.APIS.PostingResponse
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.adapterHome.CommentAdapter
import com.example.saengsaengtalk.databinding.FragTaxiPostBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentTaxiPost :Fragment() {
    var postId = ""

    private var mBinding: FragTaxiPostBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("postId")!!
        }
        //println(postNum)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragTaxiPostBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        getPost()

        /*val content = Content("생자대->밀양역 가실분", "주넝이", LocalDateTime.now(),
            "생자대", "밀양역", LocalDateTime.parse("2022-04-04T15:10:00"), 2,6600,
            "밀양역 가실분 구해요", /*mutableListOf(Comment("동동이", "저요!",
                LocalDateTime.now(), 0, 0, 0, "ehdehd"))*/)*/


    }

    class Content(
        val title: String,
        val writer: String,
        val created: LocalDateTime,
        val depart: String,
        val dest: String,
        val time: LocalDateTime,
        val member: Int,
        //val price: Int,
        val content: String?,
        //val comment: MutableList<Comment>
    ) {}

    fun getPost() {
        api.getTaxiPost(postId).enqueue(object : Callback<TaxiPostModel> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<TaxiPostModel>, response: Response<TaxiPostModel>) {
                val taxiPost = response.body()!!
                val post = Content(
                    taxiPost.title,
                    taxiPost.nick_name,
                    LocalDateTime.parse(taxiPost.reg_date),
                    taxiPost.depart_name,
                    taxiPost.dest_name,
                    LocalDateTime.parse(taxiPost.depart_time),
                    taxiPost.join_users.size,
                    taxiPost.content
                )
                Log.d("log", response.toString())
                Log.d("log", taxiPost.toString())

                binding.tvPostTitle.text = post.title
                binding.tvPostWriter.text = post.writer

                val created = post.created
                val today = LocalDate.now().atTime(0, 0)
                binding.tvPostCreated.text = when (created.isBefore(today)) {
                    true -> created.format(DateTimeFormatter.ofPattern("MM/dd"))
                    else -> created.format(DateTimeFormatter.ofPattern("HH:mm"))
                }
                binding.tvDepart.text = post.depart
                binding.tvDest.text = post.dest
                binding.tvTime.text = post.time.format(DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(
                    Locale.forLanguageTag("ko")))
                binding.tvMember.text = "${post.member}명"

                val dec = DecimalFormat("#,###")
                //binding.tvPrice.text = "${dec.format(content.price/content.member)}원"
                binding.tvContent.text = post.content

                //binding.tvCommentCount.text = "댓글 ${content.comment.size}"
                binding.rvComment.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                binding.rvComment.setHasFixedSize(true)
                //binding.rvComment.adapter = CommentAdapter(content.comment)
            }

            override fun onFailure(call: Call<TaxiPostModel>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
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