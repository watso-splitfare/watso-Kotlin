package com.example.saengsaengtalk.fragmentBaedal.BaedalPost

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.APIS.BaedalConditionResponse
import com.example.saengsaengtalk.APIS.BaedalPostModel
import com.example.saengsaengtalk.APIS.BaedalPostingResponse
import com.example.saengsaengtalk.APIS.Order
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.adapterHome.CommentAdapter
import com.example.saengsaengtalk.databinding.FragBaedalPostBinding
import com.example.saengsaengtalk.fragmentBaedal.BaedalAdd.FragmentBaedalAdd
import com.example.saengsaengtalk.fragmentBaedal.BaedalMenu.FragmentBaedalMenu
import com.example.saengsaengtalk.fragmentBaedal.BaedalOrder
import com.example.saengsaengtalk.fragmentBaedal.Group
import com.example.saengsaengtalk.fragmentBaedal.Option
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentBaedalPost :Fragment() {
    var postId: String? = null
    var userId: Int = 0

    val dec = DecimalFormat("#,###")

    private var mBinding: FragBaedalPostBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()
    lateinit var baedalPost: BaedalPostModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("postId")!!
        }

        Log.d("배달 포스트", "게시물 번호: ${postId}")
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalPostBinding.inflate(inflater, container, false)

        refreshView(inflater)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView(inflater: LayoutInflater) {
        binding.btnPrevious.setOnClickListener { onBackPressed() }
        binding.tvDelete.visibility = View.GONE     // 삭제 비활성화

        getPostInfo()

        getActivity()?.getSupportFragmentManager()
            ?.setFragmentResultListener("updatePost", this) { requestKey, bundle ->
                val sucess = bundle.getBoolean("updateResult")
                println("수정완료")
                println(sucess)
                postId = "1"
                getPostInfo()
            }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPostInfo() {
        api.getBaedalPost(postId!!.toInt()).enqueue(object : Callback<BaedalPostModel> {
            override fun onResponse(call: Call<BaedalPostModel>, response: Response<BaedalPostModel>) {
                baedalPost = response.body()!!
                val store = baedalPost.store
                val comments = baedalPost.comments

                val postCreated = LocalDateTime.parse(baedalPost.reg_date, DateTimeFormatter.ISO_DATE_TIME)
                val orderTime = LocalDateTime.parse(baedalPost.order_time, DateTimeFormatter.ISO_DATE_TIME)

                if (userId != baedalPost.user.user_id) {
                    binding.tvDelete.visibility = View.GONE
                    binding.tvUpdate.visibility = View.GONE
                }

                /** 수정 버튼 */
                binding.tvUpdate.setOnClickListener {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("게시글 수정하기")
                        .setMessage("게시글을 수정하시겠습니까? \n주문 수정은 주문수정 버튼을 이용해 주세요.")
                        .setPositiveButton("확인",
                            DialogInterface.OnClickListener { dialog, id ->
                                setFrag(FragmentBaedalAdd(), mapOf(
                                    "isUpdating" to "true",
                                    "postId" to postId!!,
                                    "title" to baedalPost.title,
                                    "content" to if (baedalPost.content != "") baedalPost.content!! else "",
                                    "orderTime" to orderTime.toString(),
                                    "storeName" to store.store_name,
                                    "place" to baedalPost.place,
                                    "minMember" to baedalPost.min_member.toString(),
                                    "maxMember" to baedalPost.max_member.toString(),
                                    "fee" to store.fee.toString()
                                ))
                            })
                        .setNegativeButton("취소",
                            DialogInterface.OnClickListener { dialog, id ->
                                println("취소")
                            })
                    builder.show()
                }

                /** 포스트 내용 바인딩 */
                binding.tvPostTitle.text = baedalPost.title
                binding.tvPostWriter.text = baedalPost.user.nick_name
                binding.tvPostCreated.text = postCreated.format(
                    DateTimeFormatter.ofPattern("YYYY. MM. dd HH:MM")
                )


                binding.tvOrderTime.text = orderTime.format(DateTimeFormatter.ofPattern("M월 d일(E) H시 m분", Locale.KOREAN))
                binding.tvStore.text = store.store_name
                binding.tvCurrentMember.text = baedalPost.current_member.toString()
                binding.tvFee.text = "${dec.format(store.fee)}원"

                if (baedalPost.content != null) binding.tvContent.text = baedalPost.content

                binding.ivLike.visibility = View.GONE
                binding.tvLike.visibility = View.GONE


                /** 주문하기 및 주문가능 여부 변경 */
                if (baedalPost.is_closed) {
                    binding.tvOrder.text = "주문이 마감되었습니다."
                    binding.lytOrder.isEnabled = false
                    binding.lytOrder.setBackgroundResource(R.drawable.btn_baedal_order_closed)
                    binding.tvClose.text = "추가 주문받기"
                    binding.tvClose.setTextColor(Color.BLACK)
                } else {
                    binding.tvClose.text = "주문 마감하기"
                    binding.tvClose.setTextColor(Color.WHITE)
                    if (userId == baedalPost.user.user_id) {
                        binding.lytClose.visibility = View.VISIBLE
                        /** 주문 가능 여부 변경 */
                        binding.lytClose.setOnClickListener {
                            api.setClosed(mapOf("post_id" to postId!!)).enqueue(object : Callback<BaedalConditionResponse> {
                                override fun onResponse(call: Call<BaedalConditionResponse>, response: Response<BaedalConditionResponse>) {
                                    val res = response.body()!!
                                    if (res.condition) {
                                        binding.tvClose.text = "주문 마감하기"
                                        binding.tvClose.setTextColor(Color.WHITE)
                                        binding.lytClose.setBackgroundResource(R.drawable.btn_baedal_close)
                                    } else {
                                        binding.tvClose.text = "추가 주문받기"
                                        binding.tvClose.setTextColor(Color.BLACK)
                                        binding.lytClose.setBackgroundResource(R.drawable.btn_baedal_order_closed)
                                    }
                                }
                                override fun onFailure(call: Call<BaedalConditionResponse>, t: Throwable) {
                                    // 실패
                                    println("실패")
                                    Log.d("log",t.message.toString())
                                    Log.d("log","fail")
                                }
                            })
                        }
                    } else {
                        binding.lytClose.visibility = View.GONE
                    }

                    /** 주문하기 */
                    if (baedalPost.is_member) {
                        binding.lytCancel.visibility = View.VISIBLE
                        binding.tvOrder.text = "주문 수정하기"
                        binding.lytOrder.setOnClickListener {
                            setFrag(FragmentBaedalMenu(), mapOf(
                                "isPosting" to "false",
                                "postId" to postId!!,
                                "currentMember" to baedalPost.current_member.toString(),
                                // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
                            ))
                        }

                        /** 주문취소 */
                        println("userID: ${userId}, baedalPost.user.user_id: ${baedalPost.user.user_id}")
                        if (userId != baedalPost.user.user_id) {
                            binding.lytCancel.setOnClickListener {
                                api.baedalOrderCancel(postId!!).enqueue(object : Callback<BaedalPostingResponse> {
                                        override fun onResponse(call: Call<BaedalPostingResponse>,response: Response<BaedalPostingResponse>) {
                                            val res = response.body()!!
                                            println("주문취소: ${res}")
                                            binding.lytCancel.visibility = View.GONE
                                        }

                                        override fun onFailure(call: Call<BaedalPostingResponse>,t: Throwable) {
                                            // 실패
                                            println("주문취소: 실패")
                                            Log.d("log", t.message.toString())
                                            Log.d("log", "fail")
                                        }
                                    })
                            }
                        }
                    }
                    else {
                        binding.tvOrder.text = "나도 주문하기"
                        binding.lytCancel.visibility = View.GONE
                    }
                }

                /** 주문내역 바인딩 */
                binding.rvOrderList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                binding.rvOrderList.setHasFixedSize(true)

                val baedalOrderUsers = mutableListOf<BaedalOrderUser>()
                for (orderUser in baedalPost.order_users) {
                    var orderPrice = 0
                    val baedalOrders = mutableListOf<BaedalOrder>()
                    for (order in orderUser.orders) {
                        val baedalOrder = apiModelToAdapterModel(order)
                        baedalOrders.add(baedalOrder)
                        orderPrice += baedalOrder.count * baedalOrder.sumPrice
                    }

                    baedalOrderUsers.add(
                        BaedalOrderUser(
                        orderUser.nick_name, "${dec.format(orderPrice)}원", baedalOrders))
                }

                val adapter = BaedalOrderUserAdapter(requireContext(), baedalOrderUsers)
                binding.rvOrderList.adapter = adapter

                /** 댓글 */
                binding.tvCommentCount.text = "댓글 ${comments.size}"
                binding.rvComment.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                binding.rvComment.setHasFixedSize(true)
                binding.rvComment.adapter = CommentAdapter(comments, userId)
            }

            override fun onFailure(call: Call<BaedalPostModel>, t: Throwable) {
                // 실패
                println("실패")
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }

    fun apiModelToAdapterModel(order: Order): BaedalOrder {
        val groups = mutableListOf<Group>()
        for (group in order.groups) {
            val options = mutableListOf<Option>()
            for (option in group.options) {
                options.add(Option(null, option.option_name, option.option_price))
            }
            groups.add(Group(null, group.group_name, options))
        }

        return BaedalOrder(
            order.count, null, order.menu_name, order.menu_price, order.sum_price, groups
        )
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