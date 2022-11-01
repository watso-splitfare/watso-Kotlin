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
import com.example.saengsaengtalk.APIS.DataModels.TaxiJoinResponse
import com.example.saengsaengtalk.APIS.DataModels.TaxiPostModel
import com.example.saengsaengtalk.APIS.DataModels.TaxiPostPreviewModel
import com.example.saengsaengtalk.APIS.DataModels.TaxiSwitchConditionResponse
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
    var userId = MainActivity.prefs.getString("userId", "-1").toLong()

    private var mBinding: FragTaxiPostBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("postId")!!
        }
        println("postId: ${postId}")
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
        binding.lytComment.visibility = View.GONE       // 댓글 비활성화
        binding.textView16.visibility = View.GONE
        binding.divider20.visibility = View.GONE
        binding.tvPrice.visibility = View.GONE
        binding.divider24.visibility = View.GONE

        getPost()

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
                    LocalDateTime.parse(taxiPost.update_date, DateTimeFormatter.ISO_DATE_TIME),
                    taxiPost.depart_name,
                    taxiPost.dest_name,
                    LocalDateTime.parse(taxiPost.depart_time, DateTimeFormatter.ISO_DATE_TIME),
                    taxiPost.join_users.size,
                    taxiPost.content
                )
                Log.d("log", response.toString())
                Log.d("log", taxiPost.toString())

                binding.tvPostTitle.text = post.title
                binding.tvPostWriter.text = post.writer

                if (userId == taxiPost.user_id){
                    binding.tvDelete.setOnClickListener { deletePost() }
                } else binding.tvDelete.visibility = View.GONE

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

                setBottomBtn(taxiPost)
            }

            override fun onFailure(call: Call<TaxiPostModel>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }

    fun deletePost() {
        api.delTaxiPost(postId).enqueue(object: Callback<PostingResponse> {
            override fun onResponse(
                call: Call<PostingResponse>,
                response: Response<PostingResponse>
            ) {
                val res = response.body()!!
                if (res.success) onBackPressed() // 새로고침하기
                else println("삭제 실패")
            }
            override fun onFailure(call: Call<PostingResponse>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")

                println("삭제 실패")
            }
        })
    }

    fun setBottomBtn(taxiPost: TaxiPostModel) {
        println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@")
        println("userId: ${userId}, 작성자Id: ${taxiPost.user_id}, isClosed: ${taxiPost.is_closed}, joinUsers: ${taxiPost.join_users}")
        if (taxiPost.is_closed) {
            if (userId == taxiPost.user_id) {
                binding.tvBottom.text = "동승자 다시받기"
                println("@@@@@@@@@@@@1")

                binding.btnBottom.setOnClickListener { switchCondition() }
            } else {
                binding.tvBottom.text = "마감되었습니다."
                println("@@@@@@@@@@@@2")
                binding.btnBottom.setOnClickListener { }
            }
        }
        else {
            if (userId == taxiPost.user_id) {
                binding.tvBottom.text = "동승자 그만받기"
                binding.btnBottom.setOnClickListener { switchCondition() }
            } else {
                if (userId in taxiPost.join_users){
                    binding.tvBottom.text = "동승 취소하기"
                    println("@@@@@@@@@@@@3")
                    binding.btnBottom.setOnClickListener { taxiJoin() }
                } else {
                    binding.tvBottom.text = "동승 신청하기"
                    println("@@@@@@@@@@@@4")
                    binding.btnBottom.setOnClickListener { taxiJoin() }
                }
            }
        }
    }

    fun switchCondition() {
        api.switchCondition(mapOf("post_id" to postId)).enqueue(object : Callback<TaxiSwitchConditionResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<TaxiSwitchConditionResponse>, response: Response<TaxiSwitchConditionResponse>) {
                val res = response.body()!!

                Log.d("log", response.toString())
                Log.d("log", res.toString())

                if (res.condition) binding.tvBottom.text = "동승자 그만받기"
                else binding.tvBottom.text = "동승자 다시받기"


            }

            override fun onFailure(call: Call<TaxiSwitchConditionResponse>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }

    fun taxiJoin() {
        api.taxiJoin(mapOf("post_id" to postId)).enqueue(object : Callback<TaxiJoinResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<TaxiJoinResponse>, response: Response<TaxiJoinResponse>) {
                val res = response.body()!!

                Log.d("log", response.toString())
                Log.d("log", res.toString())

                if (res.join) binding.tvBottom.text = "동승 취소하기"
                else binding.tvBottom.text = "동승 신청하기"
            }

            override fun onFailure(call: Call<TaxiJoinResponse>, t: Throwable) {
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