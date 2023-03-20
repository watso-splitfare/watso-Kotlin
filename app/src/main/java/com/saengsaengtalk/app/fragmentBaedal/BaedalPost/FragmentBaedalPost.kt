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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.saengsaengtalk.app.APIS.*
import com.saengsaengtalk.app.LoopingDialog
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.R
import com.saengsaengtalk.app.databinding.FragBaedalPostBinding
import com.saengsaengtalk.app.fragmentBaedal.BaedalAdd.FragmentBaedalAdd
import com.saengsaengtalk.app.fragmentBaedal.BaedalMenu.FragmentBaedalMenu
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

    private var mBinding: FragBaedalPostBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()
    lateinit var baedalPost: BaedalPost
    var userOrders = mutableMapOf<Long, List<Order>>()
    var orderConfirm = mutableMapOf<Long, Boolean>()

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
                    binding.tvPostWriter.text = baedalPost.nickName
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
                                        userOrder.nickName,
                                        userOrder.orderConfirmation,
                                        userOrder.orders,
                                        true
                                    ))
                                    //myOrder[-1].isMyOrder = true
                                } else otherOrders.add(userOrder)
                            }
                        }

                        if (myOrder.size > 0) {
                            val myOrderAdapter = BaedalUserOrderAdapter(requireContext(), myOrder)
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
        Log.d("FragbaedalPost-setBottomBtn-orderCompleted/isMember", baedalPost.orderCompleted.toString() +
        isMember.toString())
        Log.d("FragbaedalPost-orderConfirm.keys.contains(userId), orderConfirm[userId]",
            orderConfirm.keys.contains(userId).toString() + orderConfirm[userId]
        )
        if (!baedalPost.orderCompleted) {
            Log.d("FragbaedalPost", "1")
            if (isMember) {
                Log.d("FragbaedalPost", "2")
                if (orderConfirm.keys.contains(userId) && !orderConfirm[userId]!!) { // 유저가 확정 안했다면
                    Log.d("FragBaedalPost", "3")
                    binding.lytOrder.visibility = View.VISIBLE
                    if (userOrders.keys.contains(userId) && userOrders[userId]!!.isNotEmpty()) {    // 현재 유저의 주문이 있다면
                        Log.d("FragBaedalPost", "4")
                        binding.tvOrder.text = "메뉴 수정하기"
                        binding.lytOrder.setOnClickListener { goToOrderingFrag(true) }
                    } else {
                        Log.d("FragBaedalPost", "5")
                        binding.tvOrder.text = "메뉴 담기"
                        binding.lytOrder.setOnClickListener { goToOrderingFrag(false) }
                    }
                } else {
                    Log.d("FragBaedalPost", "6")
                    binding.lytOrder.visibility = View.GONE}
            } else {
                Log.d("FragBaedalPost", "7")
                binding.lytOrder.visibility = View.GONE}
        } else {
            Log.d("FragBaedalPost", "8")
            binding.lytOrder.visibility = View.GONE}

        /** 그룹 참가(마감) 버튼 */
        Log.d("","")
        Log.d("그룹 참가(마감) 버튼","")
        Log.d("baedalPost.orderCompleted", baedalPost.orderCompleted.toString())
        Log.d("userId == baedalPost.userId", (userId == baedalPost.userId).toString())
        Log.d("isOpen", isOpen.toString())
        Log.d("isMember", isMember.toString())
        if (!baedalPost.orderCompleted) {
            Log.d("그룹 참가(마감)", "1")
            if (userId == baedalPost.userId) {       // 사용자가 대표자라면
                Log.d("그룹 참가(마감)", "2")
                binding.lytGroup.visibility = View.GONE
                binding.lytStatus.setOnClickListener { switchStatus() }
                if (isOpen) {
                    Log.d("그룹 참가(마감)", "3")
                    binding.tvStatus.text = "참여 마감하기"
                    binding.tvStatus.setTextColor(Color.WHITE)
                    binding.lytStatus.setBackgroundResource(R.drawable.btn_baedal_close)
                } else {
                    Log.d("그룹 참가(마감)", "4")
                    binding.tvStatus.text = "마감 취소하기"
                    binding.tvStatus.setTextColor(Color.BLACK)
                    binding.lytStatus.setBackgroundResource(R.drawable.btn_baedal_order_closed)
                }
            } else {
                Log.d("그룹 참가(마감)", "5")                           // 사용자가 일반 멤버라면
                binding.lytStatus.visibility = View.GONE
                binding.lytGroup.setOnClickListener {
                    if (isMember) leaveGroup()
                    else joinGroupAlert()
                }
                if (isMember) {
                    Log.d("그룹 참가(마감)", "6")
                    binding.tvGroup.text = "그룹 탈퇴하기"
                    binding.tvGroup.setTextColor(Color.WHITE)
                    binding.lytGroup.setBackgroundResource(R.drawable.btn_baedal_close)
                } else {
                    Log.d("그룹 참가(마감)", "7")
                    if (isOpen) {
                        Log.d("그룹 참가(마감)", "8")
                        binding.tvGroup.text = "그룹 참가하기"
                        binding.tvGroup.setTextColor(Color.WHITE)
                        binding.lytGroup.setBackgroundResource(R.drawable.btn_baedal_order)
                    } else {
                        Log.d("그룹 참가(마감)", "9")
                        binding.tvGroup.text = "마감되었습니다."
                        binding.tvGroup.setTextColor(Color.BLACK)
                        binding.lytGroup.setBackgroundResource(R.drawable.btn_baedal_order_closed)
                    }
                }
            }
        } else {
            Log.d("그룹 참가(마감)", "10")
            binding.lytGroup.visibility = View.GONE
            binding.lytStatus.visibility = View.GONE
        }

        /** 주문 확정 버튼 */
        Log.d("","")
        Log.d("주문확정 버튼", "")
        Log.d("orderCompleted", baedalPost.orderCompleted.toString())
        Log.d("isMember", isMember.toString())
        Log.d("userId == baedalPost.userId", (userId == baedalPost.userId).toString())
        Log.d("orderConfirm.keys.contains(userId) && orderConfirm[userId]", (orderConfirm.keys.contains(userId) && orderConfirm[userId]!!).toString())
        Log.d("userOrders.keys.contains(userId)", userOrders.keys.contains(userId).toString())
        if (!baedalPost.orderCompleted) {
            Log.d("주문확정 버튼", "1")
            if (isMember) {
                Log.d("주문확정 버튼", "2")
                binding.lytComplete.visibility = View.VISIBLE
                if (userId == baedalPost.userId) {  // 대표자라면
                    Log.d("주문확정 버튼", "3")
                    binding.tvComplete.text = "주문 완료하기"
                    var confirmCount = 1
                    orderConfirm.forEach {if (it.value) confirmCount += 1 }
                    Log.d("confirmCount >= orderConfirm.size", (confirmCount >= orderConfirm.size).toString())
                    if (confirmCount >= orderConfirm.size) {        // 모든 유저가 확정 했다면
                        Log.d("주문확정 버튼", "4")
                        binding.lytComplete.setOnClickListener { completeOrder() }
                        binding.tvComplete.setTextColor(Color.WHITE)
                        binding.lytComplete.setBackgroundResource(R.drawable.btn_baedal_complete)
                    } else {
                        Log.d("주문확정 버튼", "5")
                        binding.lytComplete.isEnabled = false
                        binding.tvComplete.setTextColor(Color.BLACK)
                        binding.lytComplete.setBackgroundResource(R.drawable.btn_baedal_order_closed)
                    }
                } else {                            // 대표자가 아니라면
                    Log.d("주문확정 버튼", "6")
                    if (orderConfirm.keys.contains(userId) && orderConfirm[userId]!!) {     // 유저가 주문을 확정했다면
                        Log.d("주문확정 버튼", "7")
                        binding.tvComplete.text = "주문 확정완료"
                        binding.lytComplete.isEnabled = false
                        binding.tvComplete.setTextColor(Color.BLACK)
                        binding.lytComplete.setBackgroundResource(R.drawable.btn_baedal_order_closed)
                    } else {
                        Log.d("주문확정 버튼", "8")
                        if (userOrders.keys.contains(userId) && userOrders[userId]!!.isNotEmpty()) {
                            Log.d("주문확정 버튼", "9")
                            binding.tvComplete.text = "주문 확정하기"
                            binding.lytComplete.setOnClickListener { confirmOrder() }
                            binding.tvComplete.setTextColor(Color.WHITE)
                            binding.lytComplete.setBackgroundResource(R.drawable.btn_baedal_complete)
                        } else {
                            Log.d("주문확정 버튼", "10")
                            binding.lytComplete.visibility = View.GONE}
                    }
                }
            } else {
                Log.d("주문확정 버튼", "11")
                binding.lytComplete.visibility = View.GONE}
        }
        else {
            Log.d("주문확정 버튼", "12")
            binding.tvComplete.text = "주문 완료된 게시글입니다."
            binding.lytComplete.isEnabled = false
        }
    }

    fun switchStatus(){
        Log.d("switchStatus-postId", postId!!)
        Log.d("switchStatus-!isOpen", (isOpen).toString())

        val loopingDialog = looping()
        api.baedalSwitchStatus(postId!!, SwitchStatus(isOpen)).enqueue(object : Callback<VoidResponse> {
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

    fun joinGroupAlert() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("그룹 참여 하기")
            .setMessage("그룹에 참여 하시겠습니까?\n")
            .setPositiveButton("네", DialogInterface.OnClickListener {
                    dialog, id -> joinGroup()
            })
            .setNegativeButton("아니요",
                DialogInterface.OnClickListener { dialog, id -> })
        builder.show()
    }

    fun joinGroup() {
        val loopingDialog = looping()
        api.baedalJoin(postId!!).enqueue(object: Callback<VoidResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                if (response.code() == 204) {
                    getPostInfo()

                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("그룹 참여 완료")
                        .setMessage("주문을 작성하시겠습니까?\n")
                        .setPositiveButton("확인", DialogInterface.OnClickListener {
                            dialog, id -> goToOrderingFrag(false)
                        })
                        .setNegativeButton("조금 있다 할게요!",
                            DialogInterface.OnClickListener { dialog, id -> })
                    builder.show()
                } else {
                    makeToast("다시 시도해주세요.")
                    Log.d("FragBaedalPost joinGroup 그룹 참여 실패", "response code ${response.code()}")
                }
                looping(false, loopingDialog)
            }
            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                Log.d("FragBaedalPost joinGroup 그룹참여 실패", t.message.toString())
                makeToast("다시 시도해주세요.")
                looping(false, loopingDialog)
            }
        })
    }

    fun leaveGroup(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("그룹 탙퇴하기")
            .setMessage("그룹을 탈퇴하시겠습니까? \n다시 주문하기 위해서는 주문을 다시 작성해야합니다.")
            .setPositiveButton("확인",
                DialogInterface.OnClickListener { dialog, id ->
                    val loopingDialog = looping()
                    api.baedalLeaveGroup(postId!!).enqueue(object: Callback<VoidResponse> {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                            if (response.code() == 204) {
                                userOrders = mutableMapOf<Long, List<Order>>()
                                orderConfirm = mutableMapOf<Long, Boolean>()
                                getPostInfo()
                            } else makeToast("다시 시도해주세요.")
                            looping(false, loopingDialog)
                        }
                        override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
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

    fun confirmOrder() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("주문 확정하기")
            .setMessage("주문을 확정하시겠습니까? \n주문 확정 이후에는 수정할 수 없습니다.")
            .setPositiveButton("확인",
                DialogInterface.OnClickListener { dialog, id ->
                    val loopingDialog = looping()
                    api.baedalOrderConfirm(postId!!).enqueue(object: Callback<VoidResponse> {
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
                })
            .setNegativeButton("취소",
                DialogInterface.OnClickListener { dialog, id ->
                    println("취소")
                })
        builder.show()
    }

    fun completeOrder() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("주문 완료하기")
            .setMessage("주문을 완료하시겠습니까? \n참여자들은 더 이상 메뉴를 수정할 수 없습니다.")
            .setPositiveButton("확인",
                DialogInterface.OnClickListener { dialog, id ->
                    val loopingDialog = looping()
                    api.baedalOrderCompleted(postId!!, true).enqueue(object: Callback<VoidResponse> {
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
                })
            .setNegativeButton("취소",
                DialogInterface.OnClickListener { dialog, id ->
                    println("취소")
                })
        builder.show()
    }

    fun goToOrderingFrag(isUpdating: Boolean) {
        val currentMember = userOrders.size
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