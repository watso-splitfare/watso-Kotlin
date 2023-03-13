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
/*import com.saengsaengtalk.app.fragmentBaedal.BaedalOrder
import com.saengsaengtalk.app.fragmentBaedal.Group
import com.saengsaengtalk.app.fragmentBaedal.Option*/
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
    val joinUsers = mutableListOf<Long>()

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
        api.getBaedalPost(postId!!).enqueue(object : Callback<BaedalPost> {
            override fun onResponse(call: Call<BaedalPost>, response: Response<BaedalPost>) {
                if (response.code() == 200) {
                    baedalPost = response.body()!!

                    for (user in baedalPost.userOrders) joinUsers.add(user.userId!!)
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

                    binding.ivLike.visibility = View.GONE
                    binding.tvLike.visibility = View.GONE


                    /** 주문하기 및 주문가능 여부 변경 */

                    if (userId == baedalPost.userId) binding.lytSwitchStatus.setOnClickListener { switchStatus() }
                    else binding.lytSwitchStatus.visibility = View.GONE
                    binding.lytCancel.setOnClickListener { leaveGroup() }
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
                                    myOrder.add(userOrder)
                                    myOrder[-1].isMyOrder = true
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
        binding.lytCancel.visibility = View.GONE
        if (isOpen) {                                // 마감 안됐을 때
            if (userId == baedalPost.userId) {         // 게시글 작성자 일 경우 마감버튼 바인딩
                binding.tvSwitchStatus.text = "주문 마감하기"
                binding.tvSwitchStatus.setTextColor(Color.WHITE)
                binding.lytSwitchStatus.setBackgroundResource(R.drawable.btn_baedal_close)
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
        } else {                         // 마감 됐을 때
            if (userId == baedalPost.userId) {         // 게시글 작성자 일 경우 마감버튼 바인딩
                binding.tvSwitchStatus.text = "추가 주문받기"
                binding.tvSwitchStatus.setTextColor(Color.BLACK)
                binding.lytSwitchStatus.setBackgroundResource(R.drawable.btn_baedal_order_closed)
            }

            binding.tvOrder.text = "주문이 마감되었습니다."
            binding.ivOrder.visibility = View.GONE
            binding.lytOrder.setBackgroundResource(R.drawable.btn_baedal_order_closed)
            binding.lytOrder.isEnabled = false

        }
    }

    fun switchStatus(){
        val loopingDialog = looping()
        api.baedalSwitchStatus(postId!!, SwitchStatus(isOpen)).enqueue(object : Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                if (response.code() == 204) {
                    isOpen = !isOpen
                    setBottomBtn()
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
    }

    fun leaveGroup(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("주문 취소하기")
            .setMessage("주문을 취소하시겠습니까? \n다시 주문하기 위해서는 주문을 다시 작성해야합니다.")
            .setPositiveButton("확인",
                DialogInterface.OnClickListener { dialog, id ->
                    val loopingDialog = looping()
                    api.baedalLeaveGroup(postId!!).enqueue(object: Callback<VoidResponse> {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                            if (response.code() == 204) {
                                isMember = false
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

    fun goToOrderingFrag(isUpdating: Boolean) {
        val currentMember = joinUsers.size
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