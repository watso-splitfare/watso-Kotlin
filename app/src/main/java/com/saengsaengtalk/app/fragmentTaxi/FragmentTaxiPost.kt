package com.saengsaengtalk.app.fragmentTaxi

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.saengsaengtalk.app.APIS.*
import com.saengsaengtalk.app.LoopingDialog
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.R
import com.saengsaengtalk.app.databinding.FragTaxiPostBinding
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

    var isMember = false
    var isClosed = false

    private var mBinding: FragTaxiPostBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()
    var member = 0
    lateinit var taxiPost:TaxiPostModel

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

        getPostInfo()

    }

    class Content(
        val title: String,
        val writer: String,
        val created: LocalDateTime,
        val depart: String,
        val dest: String,
        val time: LocalDateTime,
        val member: Int,
        val isClosed: Boolean,
        //val price: Int,
        val content: String?,
        //val comment: MutableList<Comment>
    ) {}

    fun getPostInfo() {
        val loopingDialog = looping()
        api.getTaxiPost(postId).enqueue(object : Callback<TaxiPostModel> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<TaxiPostModel>, response: Response<TaxiPostModel>) {
                if (response.code() == 200) {
                    taxiPost = response.body()!!
                    isMember = userId in taxiPost.join_users
                    isClosed = taxiPost.is_closed
                    if (userId == taxiPost.user_id) binding.ivBottom.visibility = View.GONE

                    Log.d("log", response.toString())
                    Log.d("log", taxiPost.toString())

                    //binding.tvPostTitle.text = taxiPost.title
                    binding.tvPostWriter.text = taxiPost.nick_name

                    if (userId == taxiPost.user_id) {
                        binding.tvDelete.setOnClickListener { deletePost() }
                    } else binding.tvDelete.visibility = View.GONE

                    val created =
                        LocalDateTime.parse(taxiPost.update_date, DateTimeFormatter.ISO_DATE_TIME)
                    val today = LocalDate.now().atTime(0, 0)
                    binding.tvPostCreated.text = when (created.isBefore(today)) {
                        true -> created.format(DateTimeFormatter.ofPattern("MM/dd"))
                        else -> created.format(DateTimeFormatter.ofPattern("HH:mm"))
                    }
                    binding.tvDepart.text = taxiPost.depart_name
                    binding.tvDest.text = taxiPost.dest_name
                    val departTime =
                        LocalDateTime.parse(taxiPost.depart_time, DateTimeFormatter.ISO_DATE_TIME)
                    binding.tvTime.text = departTime.format(
                        DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(
                            Locale.forLanguageTag("ko")
                        )
                    )
                    member = taxiPost.join_users.size
                    binding.tvMember.text = "${member}명"


                    val dec = DecimalFormat("#,###")
                    //binding.tvPrice.text = "${dec.format(content.price/content.member)}원"
                    binding.tvContent.text = taxiPost.content

                    //binding.tvCommentCount.text = "댓글 ${content.comment.size}"
                    binding.rvComment.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    binding.rvComment.setHasFixedSize(true)
                    //binding.rvComment.adapter = CommentAdapter(content.comment)

                    setBottomBtn()
                } else {
                    Log.e("taxi Post Fragment - getTaxiPost", response.toString())
                    makeToast("게시글 조회 실패")
                    onBackPressed()
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<TaxiPostModel>, t: Throwable) {
                Log.e("taxi Post Fragment - getTaxiPost", t.message.toString())
                makeToast("게시글 조회 실패")
                onBackPressed()
                looping(false, loopingDialog)
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

    fun setBottomBtn() {
        //println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@")
        //println("userId: ${userId}, 작성자Id: ${taxiPost.user_id}, isClosed: ${taxiPost.is_closed}, joinUsers: ${taxiPost.join_users}")
        binding.ivBottom.visibility = View.GONE
        binding.btnBottom.setOnClickListener { switchCondition() }
        if (isClosed) {
            binding.btnBottom.setBackgroundResource(R.drawable.btn_baedal_order_closed)
            if (userId == taxiPost.user_id) binding.tvBottom.text = "동승자 다시받기"
            else {
                binding.tvBottom.text = "마감되었습니다."
                binding.btnBottom.isEnabled = false
            }
        }
        else {
            binding.btnBottom.setBackgroundResource(R.drawable.btn_taxi_propose)
            if (userId == taxiPost.user_id) binding.tvBottom.text = "동승자 그만받기"
            else {
                if (isMember) binding.tvBottom.text = "동승 취소하기"
                else {
                    binding.tvBottom.text = "동승 신청하기"
                    binding.ivBottom.visibility = View.VISIBLE
                }
                binding.btnBottom.setOnClickListener { taxiJoin() }
            }
        }
    }

    fun switchCondition() {
        /*val loopingDialog = looping()
        api.switchTaxiIsClosed(mapOf("post_id" to postId)).enqueue(object : Callback<IsClosedResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<IsClosedResponse>, response: Response<IsClosedResponse>) {
                if (response.code() == 200) {
                    isClosed = response.body()!!.is_closed
                    setBottomBtn()
                } else {
                    makeToast("다시 시도해주세요.")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<IsClosedResponse>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
                makeToast("다시 시도해주세요.")
                looping(false, loopingDialog)
            }
        })*/
    }

    fun taxiJoin() {
        /*val loopingDialog = looping()
        api.switchTaxiJoin(mapOf("post_id" to postId, "user_id" to userId.toString())).enqueue(object : Callback<JoinResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<JoinResponse>, response: Response<JoinResponse>) {
                if (response.code() == 200) {
                    isMember = response.body()!!.join
                    if (isMember) member += 1
                    else member -= 1

                    setBottomBtn()
                    binding.tvMember.text = "${member}명"
                } else {
                    makeToast("다시 시도해주세요.")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<JoinResponse>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
                makeToast("다시 시도해주세요.")
                looping(false, loopingDialog)
            }
        })*/
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
        mActivity.setFrag(fragment, arguments)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}