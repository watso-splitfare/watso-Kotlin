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
import com.example.saengsaengtalk.APIS.*
import com.example.saengsaengtalk.LoopingDialog
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
    var userId = MainActivity.prefs.getString("userId", "-1").toLong()
    val dec = DecimalFormat("#,###")

    var isMember = false
    var isClosed = false

    private var mBinding: FragBaedalPostBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()
    lateinit var baedalPost: BaedalPostModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("postId")!!
        }

        //println("유저아이디 쿠키: ${MainActivity.prefs.getString("userId", "")}")
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
        binding.lytComment.visibility = View.GONE   // 댓글 비활성화

        getPostInfo()

        getActivity()?.getSupportFragmentManager()
            ?.setFragmentResultListener("updatePost", this) { requestKey, bundle ->
                val success = bundle.getBoolean("success")
                println("수정완료")
                println(success)
                postId = bundle.getString("postId")
                getPostInfo()
            }

        getActivity()?.getSupportFragmentManager()
            ?.setFragmentResultListener("ordering", this) { requestKey, bundle ->
                val success = bundle.getBoolean("success")
                println("주문완료")
                println(success)
                postId = bundle.getString("postId")
                getPostInfo()
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPostInfo() {
        val loopingDialog = looping()
        api.getBaedalPost(postId!!).enqueue(object : Callback<BaedalPostModel> {
            override fun onResponse(call: Call<BaedalPostModel>, response: Response<BaedalPostModel>) {
                if (response.code() == 200) {
                    baedalPost = response.body()!!
                    isMember = baedalPost.join_users.contains(userId)
                    isClosed = baedalPost.is_closed
                    val store = baedalPost.store
                    //val comments = baedalPost.comments
                    val currentMember = baedalPost.join_users.size

                    val postCreated =
                        LocalDateTime.parse(baedalPost.update_date, DateTimeFormatter.ISO_DATE_TIME)
                    val orderTime =
                        LocalDateTime.parse(baedalPost.order_time, DateTimeFormatter.ISO_DATE_TIME)

                    println("@@@@@@@@@@@@ userId: ${userId}")
                    println("@@@@@@@@@@@@ baedalPost: ${baedalPost}")
                    println("@@@@@@@@@@@@ post_user_id: ${baedalPost.user_id}")
                    if (userId != baedalPost.user_id) {
                        binding.tvDelete.visibility = View.GONE
                        binding.tvUpdate.visibility = View.GONE
                    }

                    /** 게시글 수정 버튼 */
                    binding.tvUpdate.setOnClickListener {
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setTitle("게시글 수정하기")
                            .setMessage("게시글을 수정하시겠습니까? \n주문 수정은 주문수정 버튼을 이용해 주세요.")
                            .setPositiveButton("확인",
                                DialogInterface.OnClickListener { dialog, id ->
                                    setFrag(
                                        FragmentBaedalAdd(), mapOf(
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
                                        )
                                    )
                                })
                            .setNegativeButton("취소",
                                DialogInterface.OnClickListener { dialog, id ->
                                    println("취소")
                                })
                        builder.show()
                    }

                    /** 포스트 내용 바인딩 */
                    binding.tvPostTitle.text = baedalPost.title
                    binding.tvPostWriter.text = baedalPost.nick_name
                    binding.tvPostCreated.text = postCreated.format(
                        DateTimeFormatter.ofPattern("YYYY. MM. dd HH:MM")
                    )


                    binding.tvOrderTime.text =
                        orderTime.format(
                            DateTimeFormatter.ofPattern(
                                "M월 d일(E) H시 m분",
                                Locale.KOREAN
                            )
                        )
                    binding.tvStore.text = store.store_name
                    binding.tvCurrentMember.text = currentMember.toString()
                    binding.tvFee.text = "${dec.format(store.fee)}원"

                    if (baedalPost.content != null) binding.tvContent.text = baedalPost.content

                    binding.ivLike.visibility = View.GONE
                    binding.tvLike.visibility = View.GONE


                    /** 주문하기 및 주문가능 여부 변경 */

                    if (userId == baedalPost.user_id) binding.lytClose.setOnClickListener { switchIsClosed() }
                    else binding.lytClose.visibility = View.GONE
                    setBottomBtn()

                    /*if (baedalPost.is_closed) {
                    binding.tvOrder.text = "주문이 마감되었습니다."
                    binding.lytOrder.isEnabled = false
                    binding.lytOrder.setBackgroundResource(R.drawable.btn_baedal_order_closed)
                    binding.tvClose.text = "추가 주문받기"
                    binding.tvClose.setTextColor(Color.BLACK)
                } else {
                    binding.tvClose.text = "주문 마감하기"
                    binding.tvClose.setTextColor(Color.WHITE)
                    if (userId == baedalPost.user_id) {
                        binding.lytClose.visibility = View.VISIBLE
                        /** 주문 가능 여부 변경 */
                        binding.lytClose.setOnClickListener {
                            api.switchIsClosed(mapOf("post_id" to postId!!)).enqueue(object : Callback<BaedalConditionResponse> {
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

                    /** 주문하기 및 주문수정 */
                    var isUpdating:Boolean
                    if (isMember) {
                        /** 주문수정 */
                        binding.lytCancel.visibility = View.VISIBLE
                        binding.tvOrder.text = "주문 수정하기"
                        isUpdating = true

                        /** 주문취소 */
                        println("userId: ${userId}, baedalPost.user.user_id: ${baedalPost.user_id}")
                        if (userId != baedalPost.user_id) {
                            binding.lytCancel.setOnClickListener {
                                api.SwitchOrderJoin(mapOf("post_id" to postId!!)).enqueue(object : Callback<BaedalPostingResponse> {
                                        override fun onResponse(call: Call<BaedalPostingResponse>,response: Response<BaedalPostingResponse>) {
                                            val res = response.toString()!!
                                            println("주문취소: ${res}")
                                            binding.lytCancel.visibility = View.GONE
                                            binding.tvOrder.text = "나도 주문하기"
                                            getPostInfo()
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
                        /** 주문작성 */
                        binding.lytCancel.visibility = View.GONE
                        binding.tvOrder.text = "나도 주문하기"
                        isUpdating = false
                    }

                    binding.lytOrder.setOnClickListener {
                        setFrag(FragmentBaedalMenu(), mapOf(
                            "isPosting" to "false",
                            "postId" to postId!!,
                            "currentMember" to currentMember.toString(),
                            "isUpdating" to isUpdating.toString(),
                            "storeName" to store.store_name,
                            "storeId" to store._id,
                            "baedalFee" to store.fee.toString(),
                            "orders" to ""
                        ))
                    }
                }*/

                    /** 주문내역 바인딩 */
                    binding.rvOrderList.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    binding.rvOrderList.setHasFixedSize(true)

                    val baedalOrderUsers = mutableListOf<BaedalOrderUser>()
                    for (orderUser in baedalPost.user_orders) {
                        var orderPrice = 0
                        val baedalOrders = mutableListOf<BaedalOrder>()
                        for (order in orderUser.orders) {
                            val baedalOrder = apiModelToAdapterModel(order)
                            baedalOrders.add(baedalOrder)
                            orderPrice += baedalOrder.count * baedalOrder.sumPrice
                        }

                        baedalOrderUsers.add(
                            BaedalOrderUser(
                                orderUser.nick_name, "${dec.format(orderPrice)}원", baedalOrders
                            )
                        )
                    }

                    val adapter = BaedalOrderUserAdapter(requireContext(), baedalOrderUsers)
                    binding.rvOrderList.adapter = adapter

                    /** 댓글 */
                    /*binding.tvCommentCount.text = "댓글 ${comments.size}"
                binding.rvComment.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                binding.rvComment.setHasFixedSize(true)
                binding.rvComment.adapter = CommentAdapter(comments, userId)*/
                } else {
                    makeToast("게시글 조회 실패")
                    onBackPressed()
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<BaedalPostModel>, t: Throwable) {
                // 실패
                println("실패")
                Log.d("log",t.message.toString())
                Log.d("log","fail")
                makeToast("게시글 조회 실패")
                onBackPressed()
                looping(false, loopingDialog)
            }
        })
    }

    fun setBottomBtn() {
        println("배달 포스트 setBottomBtn함수 userId:${userId}, postUserId: ${baedalPost.user_id}")

        if (isClosed) {             // 마감 됐을 때
            if (userId == baedalPost.user_id) {         // 게시글 작성자 일 경우 마감버튼 바인딩
                binding.tvClose.text = "추가 주문받기"
                binding.tvClose.setTextColor(Color.BLACK)
                binding.lytClose.setBackgroundResource(R.drawable.btn_baedal_order_closed)
            }

            binding.tvOrder.text = "주문이 마감되었습니다."
            binding.ivOrder.visibility = View.GONE
            binding.lytOrder.setBackgroundResource(R.drawable.btn_baedal_order_closed)
            binding.lytOrder.isEnabled = false

        } else {                                // 마감 안됐을 때
            if (userId == baedalPost.user_id) {         // 게시글 작성자 일 경우 마감버튼 바인딩
                binding.tvClose.text = "주문 마감하기"
                binding.tvClose.setTextColor(Color.WHITE)
                binding.lytClose.setBackgroundResource(R.drawable.btn_baedal_close)
            }

            binding.ivOrder.visibility = View.VISIBLE
            binding.lytOrder.setBackgroundResource(R.drawable.btn_baedal_order)
            binding.lytOrder.isEnabled = true
            if (isMember) {
                binding.tvOrder.text = "주문 수정하기"
                binding.lytOrder.setOnClickListener { goToOrderingFrag(true) }
            } else {
                binding.tvOrder.text = "나도 주문하기"
                binding.lytOrder.setOnClickListener { goToOrderingFrag(false) }
            }
        }
    }

    fun goToOrderingFrag(isUpdating: Boolean) {
        val currentMember = baedalPost.join_users.size
        val store = baedalPost.store

        binding.lytOrder.setOnClickListener {
            setFrag(FragmentBaedalMenu(), mapOf(
                "isPosting" to "false",
                "postId" to postId!!,
                "currentMember" to currentMember.toString(),
                "isUpdating" to isUpdating.toString(),
                "storeName" to store.store_name,
                "storeId" to store._id,
                "baedalFee" to store.fee.toString(),
                "orders" to ""
            ))
        }
    }

    fun switchIsClosed(){
        val loopingDialog = looping()
        api.switchBaedalIsClosed(mapOf("post_id" to postId!!)).enqueue(object : Callback<IsClosedResponse> {
            override fun onResponse(call: Call<IsClosedResponse>, response: Response<IsClosedResponse>) {
                if (response.code() == 200) {
                    val res = response.body()!!
                    isClosed = res.is_closed
                    setBottomBtn()
                } else makeToast("다시 시도해주세요.")
                looping(false, loopingDialog)
            }
            override fun onFailure(call: Call<IsClosedResponse>, t: Throwable) {
                // 실패
                println("실패")
                Log.d("log",t.message.toString())
                Log.d("log","fail")
                makeToast("다시 시도해주세요.")
                looping(false, loopingDialog)
            }
        })

    }

    fun apiModelToAdapterModel(order: Order): BaedalOrder {
        println("배달포스트 오류 order: ${order}")
        val groups = mutableListOf<Group>()
        for (group in order.groups) {
            val options = mutableListOf<Option>()
            for (option in group.options) {
                options.add(Option(null, option.option_name, option.option_price))
            }
            groups.add(Group(null, group.group_name, options))
        }

        return BaedalOrder(
            order.quantity, order.menu_name, order.menu_price, order.sum_price, groups
        )
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