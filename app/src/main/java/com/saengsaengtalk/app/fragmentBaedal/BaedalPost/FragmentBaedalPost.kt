package com.saengsaengtalk.app.fragmentBaedal.BaedalPost

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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.saengsaengtalk.app.APIS.*
import com.saengsaengtalk.app.LoopingDialog
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.R
import com.saengsaengtalk.app.databinding.FragBaedalPostBinding
import com.saengsaengtalk.app.fragmentBaedal.BaedalAdd.FragmentBaedalAdd
import com.saengsaengtalk.app.fragmentBaedal.BaedalMenu.FragmentBaedalMenu
import com.saengsaengtalk.app.fragmentBaedal.BaedalOpt.BaedalOptGroupAdapter
import com.saengsaengtalk.app.fragmentBaedal.BaedalOpt.FragmentBaedalOpt
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
    var isOpen = false

    lateinit var baedalPost: BaedalPost
    var userOrders = mutableMapOf<Long, List<Order>>()
    var orderConfirm = mutableMapOf<Long, Boolean>()

    private var mBinding: FragBaedalPostBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()
    val gson = Gson()

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
        //binding.tvDelete.visibility = View.GONE     // 삭제 비활성화
        binding.lytComment.visibility = View.GONE   // 댓글 비활성화

        Log.d("access", MainActivity.prefs.getString("accessToken", ""))
        Log.d("postId", postId.toString())
        getPostInfo()

        /** 게시글 수정 또는 주문추가를 하였을 경우, 리스너를 통해 확인하고 게시글을 다시 조회한다 */
        getActivity()?.getSupportFragmentManager()
            ?.setFragmentResultListener("updatePost", this) { requestKey, bundle ->
                val success = bundle.getBoolean("success")
                if (success) getPostInfo()
            }

        getActivity()?.getSupportFragmentManager()
            ?.setFragmentResultListener("ordering", this) { requestKey, bundle ->
                val success = bundle.getBoolean("success")
                if (success) getPostInfo()
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPostInfo() {
        val loopingDialog = looping()
        api.getBaedalPost(postId!!).enqueue(object : Callback<BaedalPost> {
            override fun onResponse(call: Call<BaedalPost>, response: Response<BaedalPost>) {
                if (response.code() == 200) {
                    baedalPost = response.body()!!

                    userOrders = mutableMapOf<Long, List<Order>>()
                    orderConfirm = mutableMapOf<Long, Boolean>()
                    for (userorder in baedalPost.userOrders) {
                        userOrders[userorder.userId!!] = userorder.orders
                        orderConfirm[userorder.userId] = userorder.orderConfirmation
                    }
                    val joinUsers = userOrders.keys
                    Log.d("FragBaedalPost-joinUsers", joinUsers.toString())
                    Log.d("FragBaedalPost-userId", userId.toString())
                    isMember = joinUsers.contains(userId)
                    isOpen = baedalPost.isOpen
                    val store = baedalPost.store
                    //val comments = baedalPost.comments
                    val currentMember = joinUsers.size

                    //val updateDate = LocalDateTime.parse(baedalPost.updateTime, DateTimeFormatter.ISO_DATE_TIME)
                    val orderTime =
                        LocalDateTime.parse(baedalPost.orderTime, DateTimeFormatter.ISO_DATE_TIME)

                    if (userId != baedalPost.userId) {
                        binding.tvDelete.visibility = View.GONE
                        binding.tvUpdate.visibility = View.GONE
                    }
                    if (baedalPost.orderCompleted) {
                        binding.tvDelete.visibility = View.GONE
                        binding.tvUpdate.visibility = View.GONE
                    }

                    /** 게시글 삭제 버튼 */
                    binding.tvDelete.setOnClickListener {
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setTitle("게시글 삭제하기")
                            .setMessage("게시글을 삭제하시겠습니까?")
                            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                                deletePost()
                            })
                            .setNegativeButton("취소",
                                DialogInterface.OnClickListener { dialog, id ->
                                    println("취소")
                                }
                            )
                        builder.show()
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
                                    // "title" to baedalPost.title,
                                    // "content" to if (baedalPost.content != null) baedalPost.content!! else "",
                                    "orderTime" to orderTime.toString(),
                                    "storeName" to store.name,
                                    "place" to baedalPost.place,
                                    "minMember" to if (baedalPost.minMember != null) baedalPost.minMember.toString() else "0",
                                    "maxMember" to if (baedalPost.maxMember != null )baedalPost.maxMember.toString() else "0",
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
                    binding.tvPostWriter.text = baedalPost.nickname
                    binding.tvPostCreated.visibility = View.GONE
                    /*val today = LocalDate.now().atTime(0, 0)
                    binding.tvPostCreated.text = when (updateDate.isBefore(today)) {
                        true -> updateDate.format(DateTimeFormatter.ofPattern("MM/dd"))
                        else -> updateDate.format(DateTimeFormatter.ofPattern("HH:mm"))
                    }*/

                    binding.tvOrderTime.text = orderTime.format(
                        DateTimeFormatter.ofPattern("M월 d일(E) H시 m분",Locale.KOREAN)
                    )
                    binding.tvStore.text = store.name
                    binding.tvCurrentMember.text = currentMember.toString()
                    binding.tvFee.text = "${dec.format(store.fee)}원"

                    //if (baedalPost.content != null) binding.tvContent.text = baedalPost.content

                    /** 하단 버튼 바인딩 */
                    binding.lytOrder.setOnClickListener { goToOrderingFrag() }
                    setBottomBtn()

                    /** 주문내역 바인딩 */
                    binding.rvMyOrder.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    binding.rvMyOrder.setHasFixedSize(true)
                    binding.rvOrderList.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    binding.rvOrderList.setHasFixedSize(true)


                    if (baedalPost.userOrders != null) {
                        val myOrder = mutableListOf<UserOrder>()
                        val otherOrders = mutableListOf<UserOrder>()

                        for (userOrder in baedalPost.userOrders) {
                            if (userOrder.orders.isNotEmpty()) {
                                if (userId == userOrder.userId) {
                                    myOrder.add(UserOrder(
                                        userOrder.userId,
                                        userOrder.nickname,
                                        userOrder.orderConfirmation,
                                        userOrder.orders,
                                        true
                                    ))
                                    //myOrder[-1].isMyOrder = true
                                } else otherOrders.add(userOrder)
                            }
                        }

                        /** 주문 수정 및 삭제 버튼 */
                        if (myOrder.size > 0) {
                            val myOrderAdapter = BaedalUserOrderAdapter(requireContext(), myOrder, true)
                            myOrderAdapter.addListener(object: BaedalUserOrderAdapter.OnUpdateBtnListener {
                                override fun onUpdateOrder(order: Order) {
                                    val builder = AlertDialog.Builder(requireContext())
                                    builder.setTitle("옵션 변경하기")
                                        .setMessage("옵션을 변경 하시겠습니까?\n")
                                        .setPositiveButton("네", DialogInterface.OnClickListener {
                                                dialog, id -> updateOrder(order)
                                        })
                                        .setNegativeButton("아니요",
                                            DialogInterface.OnClickListener { dialog, id -> })
                                    builder.show()

                                }
                            })
                            myOrderAdapter.addListener(object: BaedalUserOrderAdapter.OnDeleteBtnListener {
                                override fun onDeleteOrder(orderId: String) {
                                    val builder = AlertDialog.Builder(requireContext())
                                    builder.setTitle("주문 삭제하기")
                                        .setMessage("주문을 삭제 하시겠습니까?\n")
                                        .setPositiveButton("네", DialogInterface.OnClickListener {
                                                dialog, id -> deleteOrder(orderId)
                                        })
                                        .setNegativeButton("아니요",
                                            DialogInterface.OnClickListener { dialog, id -> })
                                    builder.show()
                                }
                            })
                            binding.rvMyOrder.adapter = myOrderAdapter
                        } else {
                            binding.tvMyOrder.visibility = View.GONE
                            binding.rvMyOrder.visibility = View.GONE
                            binding.divider2.visibility = View.GONE
                        }
                        if (otherOrders.size > 0) {
                            val adapter = BaedalUserOrderAdapter(requireContext(), otherOrders)
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

            override fun onFailure(call: Call<BaedalPost>, t: Throwable) {
                Log.e("baedal Post Fragment - getBaedalPost", t.message.toString())
                makeToast("게시글 조회 실패")
                onBackPressed()
                looping(false, loopingDialog)
            }
        })
    }

    fun setBottomBtn() {
        /** order 버튼 */
        if (!baedalPost.orderCompleted) {
            if (isMember) {
                if (orderConfirm.keys.contains(userId) && !orderConfirm[userId]!!) { // 유저가 확정 안했다면
                    if (userOrders.keys.contains(userId) && userOrders[userId]!!.isNotEmpty()) {    // 현재 유저의 주문이 있다면
                        binding.tvOrder.text = "메뉴 추가하기"
                    }
                } else {
                    binding.lytOrder.setBackgroundResource(R.drawable.btn_baedal_order_closed)
                    binding.tvOrder.text = "주문을 확정했어요"
                }
            } else {
                if (isOpen) {
                    binding.tvOrder.text = "나도 주문하기"
                } else {
                    binding.tvOrder.text = "마감되었습니다."
                    binding.tvOrder.setTextColor(Color.BLACK)
                    binding.ivOrder.visibility = View.GONE
                    binding.lytOrder.setBackgroundResource(R.drawable.btn_baedal_order_closed)
                    binding.lytOrder.isEnabled = false
                }
            }
        } else binding.lytOrder.visibility = View.GONE

        /** 그룹 관련 버튼 */
        if (!baedalPost.orderCompleted) {
            if (userId == baedalPost.userId) {       // 사용자가 대표자라면
                binding.lytStatus.visibility = View.VISIBLE
                binding.lytGroup.visibility = View.GONE
                binding.lytStatus.setOnClickListener { switchStatus() }
                if (isOpen) {
                    binding.tvStatus.text = "참여 마감하기"
                    binding.tvStatus.setTextColor(Color.WHITE)
                    binding.lytStatus.setBackgroundResource(R.drawable.btn_baedal_close)
                } else {
                    binding.tvStatus.text = "마감 취소하기"
                    binding.tvStatus.setTextColor(Color.BLACK)
                    binding.lytStatus.setBackgroundResource(R.drawable.btn_baedal_order_closed)
                }
            } else {                   // 사용자가 일반 멤버라면
                binding.lytStatus.visibility = View.GONE
                binding.lytGroup.visibility = View.VISIBLE
                binding.lytGroup.setOnClickListener { leaveGroup() }

                if (isMember) {
                    binding.tvGroup.text = "그룹 탈퇴하기"
                    binding.tvGroup.setTextColor(Color.WHITE)
                    binding.lytGroup.setBackgroundResource(R.drawable.btn_baedal_close)
                }
                else binding.lytGroup.visibility = View.GONE
            }
        } else {
            binding.lytGroup.visibility = View.GONE
            binding.lytStatus.visibility = View.GONE
        }

        /** 주문 확정 버튼 */
        if (!baedalPost.orderCompleted) {
            if (isMember) {
                binding.lytComplete.visibility = View.VISIBLE
                if (userId == baedalPost.userId) {  // 대표자라면
                    binding.tvComplete.text = "주문 완료하기"
                    var confirmCount = 1
                    orderConfirm.forEach {if (it.value) confirmCount += 1 }
                    if (confirmCount >= orderConfirm.size) {        // 모든 유저가 확정 했다면
                        binding.lytComplete.setOnClickListener { completeOrder() }
                        binding.tvComplete.setTextColor(Color.WHITE)
                        binding.lytComplete.setBackgroundResource(R.drawable.btn_baedal_complete)
                    } else {
                        binding.lytComplete.setOnClickListener { makeToast("모든 유저가 주문을 확정해야\n완료 가능합니다.") }
                        binding.tvComplete.setTextColor(Color.BLACK)
                        binding.lytComplete.setBackgroundResource(R.drawable.btn_baedal_order_closed)
                    }
                } else {                            // 대표자가 아니라면
                    if (orderConfirm.keys.contains(userId) && orderConfirm[userId]!!) {     // 유저가 주문을 확정했다면
                        binding.tvComplete.text = "주문 확정완료"
                        binding.lytComplete.isEnabled = false
                        binding.tvComplete.setTextColor(Color.BLACK)
                        binding.lytComplete.setBackgroundResource(R.drawable.btn_baedal_order_closed)
                    } else {
                        if (userOrders.keys.contains(userId) && userOrders[userId]!!.isNotEmpty()) {
                            binding.tvComplete.text = "주문 확정하기"
                            binding.lytComplete.setOnClickListener { confirmOrder() }
                            binding.tvComplete.setTextColor(Color.WHITE)
                            binding.lytComplete.setBackgroundResource(R.drawable.btn_baedal_complete)
                        }
                        else binding.lytComplete.visibility = View.GONE
                    }
                }
            }
            else binding.lytComplete.visibility = View.GONE
        }
        else {
            binding.tvComplete.text = "주문 완료된 게시글입니다."
            binding.lytComplete.isEnabled = false
        }
    }

    fun deletePost() {
        val loopingDialog = looping()
        api.deleteBaedalPost(postId!!).enqueue(object : Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                if (response.code() == 204) {
                    Log.d("FragBaedalPost-deletePost", "성공")
                    val bundle = bundleOf("success" to true)
                    getActivity()?.getSupportFragmentManager()?.setFragmentResult("deletePost", bundle)
                    onBackPressed()
                }
                else {
                    Log.d("FragBaedalPost-deletePost", "실패")
                    makeToast("다시 시도해주세요.")}
                looping(false, loopingDialog)
            }
            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                Log.d("log",t.message.toString())
                Log.d("log","fail")
                makeToast("다시 시도해주세요.")
                looping(false, loopingDialog)
            }
        })
    }

    /** 주문 참여가능 여부 변경 */
    fun switchStatus(){
        Log.d("switchStatus-postId", postId!!)
        Log.d("switchStatus-!isOpen", (isOpen).toString())

        val loopingDialog = looping()
        api.switchStatusBaedal(postId!!, SwitchStatus(!isOpen)).enqueue(object : Callback<VoidResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                if (response.code() == 204) {
                    Log.d("switchStatus", "1")
                    getPostInfo()}
                else {
                    Log.d("switchStatus", "2")
                    makeToast("다시 시도해주세요.")}
                looping(false, loopingDialog)
            }
            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                Log.d("log",t.message.toString())
                Log.d("log","fail")
                makeToast("다시 시도해주세요.")
                looping(false, loopingDialog)
            }
        })
    }

    fun leaveGroup(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("주문 취소하기")
            .setMessage("주문을 취소하시겠습니까? \n다시 주문하기 위해서는 메뉴를 다시 입력해야합니다.")
            .setPositiveButton("확인",
                DialogInterface.OnClickListener { dialog, id ->
                    val loopingDialog = looping()
                    api.leaveBaedalGroup(postId!!).enqueue(object: Callback<VoidResponse> {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                            looping(false, loopingDialog)
                            if (response.code() == 204) getPostInfo()
                            else makeToast("다시 시도해주세요.")
                        }
                        override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                            looping(false, loopingDialog)
                            Log.d("log",t.message.toString())
                            Log.d("log","fail")
                            makeToast("다시 시도해주세요.")
                        }
                    })
                })
            .setNegativeButton("취소",
                DialogInterface.OnClickListener { dialog, id ->
                    println("취소")
                })
        builder.show()
    }

    /** 참여자 주문 확정 */
    fun confirmOrder() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("주문 확정하기")
            .setMessage("주문을 확정하시겠습니까? \n주문 확정 이후에는 수정할 수 없습니다.")
            .setPositiveButton("확인",
                DialogInterface.OnClickListener { dialog, id ->
                    val loopingDialog = looping()
                    api.confirmBaedalOrder(postId!!).enqueue(object: Callback<VoidResponse> {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                            looping(false, loopingDialog)
                            if (response.code() == 204) getPostInfo()
                            else makeToast("다시 시도해주세요.")
                        }
                        override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                            looping(false, loopingDialog)
                            Log.d("log",t.message.toString())
                            Log.d("log","fail")
                            makeToast("다시 시도해주세요.")
                        }
                    })
                })
            .setNegativeButton("취소",
                DialogInterface.OnClickListener { dialog, id ->
                    println("취소")
                })
        builder.show()
    }

    /** 대표자 주문 완료 */
    fun completeOrder() {

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("주문 완료하기")
            .setMessage("주문을 완료하시겠습니까? \n참여자들은 더 이상 메뉴를 수정할 수 없습니다.")
            .setPositiveButton("확인",
                DialogInterface.OnClickListener { dialog, id ->
                    val loopingDialog = looping()
                    api.completeBaedalOrder(postId!!, OrderCompleted(true)).enqueue(object: Callback<VoidResponse> {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                            looping(false, loopingDialog)
                            if (response.code() == 204) getPostInfo()
                            else makeToast("다시 시도해주세요.")
                        }
                        override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                            looping(false, loopingDialog)
                            Log.d("log",t.message.toString())
                            Log.d("log","fail")
                            makeToast("다시 시도해주세요.")
                        }
                    })
                })
            .setNegativeButton("취소",
                DialogInterface.OnClickListener { dialog, id ->
                    println("취소")
                })
        builder.show()
    }

    fun goToOrderingFrag() {
        val currentMember = userOrders.size
        val store = baedalPost.store

        setFrag(FragmentBaedalMenu(), mapOf(
            "isPosting" to "false",
            "postId" to postId!!,
            "currentMember" to currentMember.toString(),
            "isUpdating" to isMember.toString(),
            "storeName" to store.name,
            "storeId" to store._id,
            "baedalFee" to store.fee.toString(),
            "orders" to ""
        ))
    }

    fun updateOrder(order: Order) {
        setFrag(FragmentBaedalOpt(), mapOf(
            "isUpdating" to "true",
            "postId" to baedalPost._id,
            "menuName" to order.menu.name,
            "menuPrice" to order.menu.price.toString(),
            "storeId" to baedalPost.store._id,
            "order" to gson.toJson(order)
        ))
    }

    fun deleteOrder(orderId: String) {
        val loopingDialog = looping()
        api.deleteBaedalOrder(postId!!, orderId).enqueue(object : Callback<VoidResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                if (response.code() == 204) getPostInfo()
                else makeToast("다시 시도해주세요.")
                looping(false, loopingDialog)
            }
            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                Log.d("log",t.message.toString())
                Log.d("log","fail")
                makeToast("다시 시도해주세요.")
                looping(false, loopingDialog)
            }
        })
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