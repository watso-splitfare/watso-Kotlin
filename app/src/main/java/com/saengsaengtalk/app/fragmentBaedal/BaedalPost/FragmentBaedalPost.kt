package com.saengsaengtalk.app.fragmentBaedal.BaedalPost

import android.annotation.SuppressLint
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
import com.google.gson.Gson
import com.saengsaengtalk.app.API.*
import com.saengsaengtalk.app.LoopingDialog
import com.saengsaengtalk.app.MainActivity
import com.saengsaengtalk.app.R
import com.saengsaengtalk.app.databinding.FragBaedalPostBinding
import com.saengsaengtalk.app.fragmentBaedal.BaedalAdd.FragmentBaedalAdd
import com.saengsaengtalk.app.fragmentBaedal.BaedalMenu.FragmentBaedalMenu
import com.saengsaengtalk.app.fragmentBaedal.BaedalOrders.FragmentBaedalOrders
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

    lateinit var baedalPost: BaedalPost

    private var mBinding: FragBaedalPostBinding? = null
    private val binding get() = mBinding!!
    val api= API.create()
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

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        Log.d("access", MainActivity.prefs.getString("accessToken", ""))
        Log.d("postId", postId.toString())
        binding.btnOrder.visibility = View.GONE
        binding.btnComplete.visibility = View.GONE
        getPostInfo()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPostInfo() {
        val loopingDialog = looping()
        api.getBaedalPost(postId!!).enqueue(object : Callback<BaedalPost> {
            override fun onResponse(call: Call<BaedalPost>, response: Response<BaedalPost>) {
                looping(false, loopingDialog)
                if (response.code() == 200) {
                    baedalPost = response.body()!!
                    setData()
                } else {
                    Log.e("baedal Post Fragment - getBaedalPost", response.toString())
                    makeToast("게시글 조회 실패")
                    onBackPressed()
                }
            }

            override fun onFailure(call: Call<BaedalPost>, t: Throwable) {
                looping(false, loopingDialog)
                Log.e("baedal Post Fragment - getBaedalPost", t.message.toString())
                makeToast("게시글 조회 실패")
                onBackPressed()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setData() {
        val joinUsers = baedalPost.users
        isMember = joinUsers.contains(userId)
        val store = baedalPost.store
        //val comments = baedalPost.comments
        Log.d("FragBaedalPost isOwner", (userId==baedalPost.userId).toString())
        Log.d("FragBaedalPost ismember", isMember.toString())
        Log.d("FragBaedalPost status", baedalPost.status)
        val orderTime = LocalDateTime.parse(baedalPost.orderTime, DateTimeFormatter.ISO_DATE_TIME)

        if (userId == baedalPost.userId) {
            binding.tvDelete.text = "삭제"
            binding.tvUpdate.text = "수정"
        } else {
            binding.tvDelete.visibility = View.GONE
            binding.tvUpdate.visibility = View.GONE
        }

        /** 게시글 삭제 버튼 */
        binding.tvDelete.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("게시글 삭제하기")
                .setMessage("게시글을 삭제하시겠습니까?")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    deletePost() })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
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
        binding.tvPostTitle.text = baedalPost.title

        binding.tvOrderTime.text = orderTime.format(
            DateTimeFormatter.ofPattern("M월 d일(E) H시 m분",Locale.KOREAN)
        )
        binding.tvStore.text = store.name
        binding.tvCurrentMember.text = baedalPost.users.size.toString()
        binding.tvFee.text = "${dec.format(store.fee)}원"

        /** 하단 버튼 바인딩 */
        binding.lytStatus.setOnClickListener { bindStatusBtn() }
        if (baedalPost.userId == userId) {
            binding.btnOrder.visibility = View.GONE
            if (baedalPost.status == "recruiting" || baedalPost.status == "closed") {
                binding.tvStatus.visibility = View.GONE
            } else {
                binding.tvStatus.text = "모집 마감"
                binding.tvStatus.visibility = View.VISIBLE
                binding.lytStatus.visibility = View.GONE
            }
        }
        else {
            if (baedalPost.status == "recruiting") binding.tvStatus.text = "모집중"
            else binding.tvStatus.text = "모집 마감"
            binding.lytStatus.visibility = View.GONE
            binding.btnComplete.visibility = View.GONE
        }
        bindBottomBtn()
        setBottomBtn()


        /** 댓글 */
        /*binding.tvCommentCount.text = "댓글 ${comments.size}"
        binding.rvComment.layoutManager =
        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvComment.setHasFixedSize(true)
        binding.rvComment.adapter = CommentAdapter(comments, userId)*/
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

    fun bindStatusBtn() {
        when (baedalPost.status) {
            "recruiting" -> {
                baedalPost.status = "closed"
                binding.ivStatus.setImageResource(R.drawable.baseline_person_off_black_24)
                binding.lytStatusOpen.setBackgroundResource(R.drawable.btn_baedal_open_released)
                binding.tvStatusOpen.setTextColor(Color.GRAY)
                binding.lytStatusClosed.setBackgroundResource(R.drawable.btn_baedal_close_pressed)
                binding.tvStatusClosed.setTextColor(Color.WHITE)//(R.color.baedal_status_released_text)
                setStatus("closed")
            }
            "closed" -> {
                baedalPost.status = "recruiting"
                binding.ivStatus.setImageResource(R.drawable.baseline_person_black_24)
                binding.lytStatusOpen.setBackgroundResource(R.drawable.btn_baedal_open_pressed)
                binding.tvStatusOpen.setTextColor(Color.WHITE)//(R.color.baedal_status_released_text)
                binding.lytStatusClosed.setBackgroundResource(R.drawable.btn_baedal_close_released)
                binding.tvStatusClosed.setTextColor(Color.GRAY)
                setStatus("recruiting")
            }
        }
    }

    fun bindBottomBtn() {
        if (userId == baedalPost.userId) {
            when (baedalPost.status) {
                "recruiting" -> binding.btnComplete.visibility = View.GONE
                "closed" -> {
                    binding.tvComplete.text = "주문 완료"
                    binding.btnComplete.visibility = View.VISIBLE
                }
                "ordered" -> {
                    binding.tvComplete.text = "배달 완료"
                    binding.btnComplete.visibility = View.VISIBLE
                }
                else -> {
                    binding.tvComplete.text = "배달 완료"
                    binding.btnComplete.setBackgroundResource(R.drawable.btn_baedal_confirm_false)
                    binding.btnComplete.visibility = View.VISIBLE
                }
            }
        } else {
            if (isMember) {
                binding.btnViewMyOrders.setBackgroundResource(R.drawable.btn_baedal_confirm)
                binding.btnViewMyOrders.isEnabled = true
            } else {
                binding.btnViewMyOrders.setBackgroundResource(R.drawable.btn_baedal_confirm_false)
                binding.btnViewMyOrders.isEnabled = false
            }

            when (baedalPost.status) {
                "recruiting" -> {
                    if (isMember) bindBtnOrder(false, true, "주문 취소")
                    else bindBtnOrder(true, true, "주문하기")
                }
                "closed" -> {
                    if (isMember) bindBtnOrder(false, true, "주문 취소")
                    else bindBtnOrder(false, false, "마감되었습니다.")
                }
                else -> bindBtnOrder(false, false, "마감되었습니다.")
            }
            binding.btnOrder.visibility = View.VISIBLE
        }
    }

    fun bindBtnOrder(background: Boolean, isEnabled: Boolean, text: String) {
        if (background) binding.btnOrder.setBackgroundResource(R.drawable.btn_baedal_confirm)
        else binding.btnOrder.setBackgroundResource(R.drawable.btn_baedal_confirm_false)
        binding.btnOrder.isEnabled = isEnabled
        binding.tvOrder.text = text
    }

    fun setBottomBtn() {
        binding.btnViewMyOrders.setOnClickListener {
            setFrag(FragmentBaedalOrders(), mapOf(
                "postId" to postId!!,
                "postTitle" to baedalPost.title,
                "isMyOrder" to "true"
            ))
        }
        binding.btnViewAllOrders.setOnClickListener {
            setFrag(FragmentBaedalOrders(), mapOf(
                "postId" to postId!!,
                "postTitle" to baedalPost.title,
                "isMyOrder" to "false"
            ))
        }
        binding.btnOrder.setOnClickListener { btnOrder() }
        binding.btnComplete.setOnClickListener { btnComplete() }
    }

    fun btnOrder() {
        if (isMember) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("주문 취소하기")
                .setMessage("주문을 취소하시겠습니까?\n 다시 참가하기 위해선\n주문을 다시 작성해야합니다.")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    deleteOrders()
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
            builder.show()
        }
        else setFrag(FragmentBaedalMenu(), mapOf("postId" to postId!!, "storeId" to baedalPost.store._id))
    }

    fun btnComplete() {
        val builder = AlertDialog.Builder(requireContext())
        if (baedalPost.status == "closed") {
            builder.setTitle("주문 완료")
                .setMessage("주문을 완료하셨나요?\n가게에 주문을 접수한 뒤에 확인버튼을 눌러주세요!")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    setStatus("ordered")
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
            builder.show()
        } // 주문 완료
        else {
            builder.setTitle("배달 완료")
                .setMessage("배달이 완료되었나요?\n주문 참가자들에게 알림이 전송됩니다.")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    setStatus("delivered")
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
            builder.show()} // 배달 완료
    }

    fun setStatus(status: String) {
        val loopingDialog = looping()
        api.setBaedalStatus(postId!!, BaedalStatus(status)).enqueue(object : Callback<VoidResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                looping(false, loopingDialog)
                if (response.code() == 204) {
                    makeToast("상태가 변경되었습니다.")
                    getPostInfo()
                } else {
                    Log.e("FragBaedalPost setStatus", response.toString())
                    makeToast("상태 변경에 실패했습니다.")
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                looping(false, loopingDialog)
                Log.e("FragBaedalPost setStatus", t.message.toString())
                makeToast("상태 변경에 실패했습니다.")
            }
        })
    }

    fun deleteOrders() {
        val loopingDialog = looping()
        api.deleteOrders(postId!!).enqueue(object : Callback<VoidResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                looping(false, loopingDialog)
                if (response.code() == 204) {
                    makeToast("주문이 취소되었습니다.")
                    getPostInfo()
                } else {
                    Log.e("FragBaedalPost deleteOrders", response.toString())
                    makeToast("주문 취소 실패")
                    onBackPressed()
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                looping(false, loopingDialog)
                Log.e("FragBaedalPost deleteOrders", t.message.toString())
                makeToast("주문 취소 실패")
                onBackPressed()
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