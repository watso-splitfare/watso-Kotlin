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
import java.time.LocalDate
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

        /** 게시글 수정 또는 주문추가를 하였을 경우, 리스너를 통해 확인하고 게시글을 다시 조회한다 */
        getActivity()?.getSupportFragmentManager()
            ?.setFragmentResultListener("updatePost", this) { requestKey, bundle ->
                val success = bundle.getBoolean("success")
                postId = bundle.getString("postId")
                getPostInfo()
            }

        getActivity()?.getSupportFragmentManager()
            ?.setFragmentResultListener("ordering", this) { requestKey, bundle ->
                val success = bundle.getBoolean("success")
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

                    if (userId != baedalPost.user_id) {
                        binding.tvDelete.visibility = View.GONE
                        binding.tvUpdate.visibility = View.GONE
                    }

                    /** 게시글 수정 버튼 */
                    binding.tvUpdate.setOnClickListener {
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setTitle("게시글 수정하기")
                            .setMessage("게시글을 수정하시겠습니까? \n주문 수정은 주문수정 버튼을 이용해 주세요.")
                            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                                setFrag(FragmentBaedalAdd(), mapOf(
                                    "isUpdating" to "true",
                                    "postId" to postId!!,
                                    "title" to baedalPost.title,
                                    "content" to if (baedalPost.content != null) baedalPost.content!! else "",
                                    "orderTime" to orderTime.toString(),
                                    "storeName" to store.name,
                                    "place" to baedalPost.place,
                                    "minMember" to if (baedalPost.min_member != null) baedalPost.min_member.toString() else "0",
                                    "maxMember" to if (baedalPost.max_member != null )baedalPost.max_member.toString() else "0",
                                    "fee" to store.fee.toString()
                                ))
                            })
                            .setNegativeButton("취소",
                                DialogInterface.OnClickListener { dialog, id ->
                                    println("취소")
                                }
                            )
                        builder.show()
                    }

                    /** 포스트 내용 바인딩 */
                    binding.tvPostWriter.text = baedalPost.nick_name
                    val today = LocalDate.now().atTime(0, 0)
                    binding.tvPostCreated.text = when (postCreated.isBefore(today)) {
                        true -> postCreated.format(DateTimeFormatter.ofPattern("MM/dd"))
                        else -> postCreated.format(DateTimeFormatter.ofPattern("HH:mm"))
                    }


                    binding.tvOrderTime.text =
                        orderTime.format(
                            DateTimeFormatter.ofPattern(
                                "M월 d일(E) H시 m분",
                                Locale.KOREAN
                            )
                        )
                    binding.tvStore.text = store.name
                    binding.tvCurrentMember.text = currentMember.toString()
                    binding.tvFee.text = "${dec.format(store.fee)}원"

                    if (baedalPost.content != null) binding.tvContent.text = baedalPost.content

                    binding.ivLike.visibility = View.GONE
                    binding.tvLike.visibility = View.GONE


                    /** 주문하기 및 주문가능 여부 변경 */

                    if (userId == baedalPost.user_id) binding.lytClose.setOnClickListener { switchIsClosed() }
                    else binding.lytClose.visibility = View.GONE
                    binding.lytCancel.setOnClickListener { cancelJoin() }
                    setBottomBtn()

                    /** 주문내역 바인딩 */
                    binding.rvMyOrder.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    binding.rvMyOrder.setHasFixedSize(true)
                    binding.rvOrderList.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    binding.rvOrderList.setHasFixedSize(true)

                    val baedalOrderUsers = mutableListOf<BaedalOrderUser>()
                    val myOrder = mutableListOf<BaedalOrderUser>()

                    if (baedalPost.user_orders != null) {
                        for (orderUser in baedalPost.user_orders!!) {
                            var orderPrice = 0
                            val baedalOrders = mutableListOf<BaedalOrder>()
                            for (order in orderUser.orders) {
                                val baedalOrder = apiModelToAdapterModel(order)
                                baedalOrders.add(baedalOrder)
                                orderPrice += baedalOrder.count * baedalOrder.sumPrice
                            }
                            val priceString = "${dec.format(orderPrice)}원"

                            if (userId == orderUser.user_id) {
                                myOrder.add(
                                    BaedalOrderUser
                                        (
                                        orderUser.nick_name,
                                        priceString,
                                        baedalOrders,
                                        true
                                    )
                                )
                            } else {
                                baedalOrderUsers.add(
                                    BaedalOrderUser
                                        (
                                        orderUser.nick_name,
                                        priceString,
                                        baedalOrders,
                                        false
                                    )
                                )
                            }
                        }

                        if (myOrder.size > 0) {
                            val myOrderAdapter = BaedalOrderUserAdapter(requireContext(), myOrder)
                            binding.rvMyOrder.adapter = myOrderAdapter
                        } else {
                            binding.tvMyOrder.visibility = View.GONE
                            binding.rvMyOrder.visibility = View.GONE
                            binding.divider2.visibility = View.GONE
                        }
                        if (baedalOrderUsers.size > 0) {
                            val adapter = BaedalOrderUserAdapter(requireContext(), baedalOrderUsers)
                            binding.rvOrderList.adapter = adapter
                        } else {
                            binding.tvOrderList.visibility = View.GONE
                            binding.rvOrderList.visibility = View.GONE
                        }
                    }

                    /** 댓글 */
                    /*binding.tvCommentCount.text = "댓글 ${comments.size}"
                    binding.rvComment.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    binding.rvComment.setHasFixedSize(true)
                    binding.rvComment.adapter = CommentAdapter(comments, userId)*/
                } else {
                    Log.e("baedal Post Fragment - getBaedalPost", response.toString())
                    makeToast("게시글 조회 실패")
                    onBackPressed()
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<BaedalPostModel>, t: Throwable) {
                Log.e("taxi Post Fragment - getTaxiPost", t.message.toString())
                makeToast("게시글 조회 실패")
                onBackPressed()
                looping(false, loopingDialog)
            }
        })
    }

    fun setBottomBtn() {
        binding.lytCancel.visibility = View.GONE
        if (isClosed) {                         // 마감 됐을 때
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
                binding.lytCancel.visibility = View.VISIBLE
            } else {
                binding.tvOrder.text = "나도 주문하기"
                binding.lytOrder.setOnClickListener { goToOrderingFrag(false) }
            }
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
                Log.d("log",t.message.toString())
                Log.d("log","fail")
                makeToast("다시 시도해주세요.")
                looping(false, loopingDialog)
            }
        })
    }

    fun cancelJoin(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("주문 취소하기")
            .setMessage("주문을 취소하시겠습니까? \n다시 주문하기 위해서는 주문을 다시 작성해야합니다.")
            .setPositiveButton("확인",
                DialogInterface.OnClickListener { dialog, id ->
                    val loopingDialog = looping()
                    api.switchBaedalJoin(mapOf("post_id" to postId!!)).enqueue(object: Callback<JoinResponse> {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(call: Call<JoinResponse>, response: Response<JoinResponse>) {
                            if (response.code() == 200) {
                                val res = response.body()!!
                                isMember = res.join
                                getPostInfo()
                            } else makeToast("다시 시도해주세요.")
                            looping(false, loopingDialog)
                        }
                        override fun onFailure(call: Call<JoinResponse>, t: Throwable) {
                            Log.d("log",t.message.toString())
                            Log.d("log","fail")
                            makeToast("다시 시도해주세요.")
                            looping(false, loopingDialog)
                        }
                    })
                })
            .setNegativeButton("취소",
                DialogInterface.OnClickListener { dialog, id ->
                    println("취소")
                })
        builder.show()
    }

    fun goToOrderingFrag(isUpdating: Boolean) {
        val currentMember = baedalPost.join_users.size
        val store = baedalPost.store

        setFrag(FragmentBaedalMenu(), mapOf(
            "isPosting" to "false",
            "postId" to postId!!,
            "currentMember" to currentMember.toString(),
            "isUpdating" to isUpdating.toString(),
            "storeName" to store.name,
            "storeId" to store._id,
            "baedalFee" to store.fee.toString(),
            "orders" to ""
        ))
    }

    /**
     * 이 프로젝트에서는 카멜케이스를 사용하지만 API 모델에서는 스네이크 케이스를 사용하므로
     * 데이터 모델간 변환이 필요합니다.
     */
    fun apiModelToAdapterModel(order: Order): BaedalOrder {
        val groups = mutableListOf<Group>()
        for (group in order.groups) {
            val options = mutableListOf<Option>()
            for (option in group.options) {
                options.add(Option(null, option.name, option.price))
            }
            groups.add(Group(null, group.name, options))
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