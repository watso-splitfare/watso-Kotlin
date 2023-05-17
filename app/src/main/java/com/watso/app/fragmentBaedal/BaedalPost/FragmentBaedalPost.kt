package com.watso.app.fragmentBaedal.BaedalPost

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.watso.app.API.*
import com.watso.app.API.DataModels.ErrorResponse
import com.watso.app.ActivityController
import com.watso.app.MainActivity
import com.watso.app.R
import com.watso.app.adapterHome.CommentAdapter
import com.watso.app.databinding.AlertdialogInputtextBinding
import com.watso.app.databinding.FragBaedalPostBinding
import com.watso.app.fragmentBaedal.BaedalAdd.FragmentBaedalAdd
import com.watso.app.fragmentBaedal.BaedalMenu.FragmentBaedalMenu
import com.watso.app.fragmentBaedal.BaedalOrders.FragmentBaedalOrders
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentBaedalPost :Fragment(), View.OnTouchListener {
    val TAG = "FragBaedalPost"
    lateinit var AC: ActivityController

    val prefs = MainActivity.prefs
    var isScrolled = false
    var infoHeight = 0
    var needMargin = 0
    var addCommentOriginY = 0f
    private var isKeyboardOpen = false
    var originViewHeight = 0
    var keyBoardHeight = 0
    var viewSetted = false

    var postId: String? = null
    var userId = prefs.getString("userId", "-1").toLong()
    val dec = DecimalFormat("#,###")

    var isMember = false

    lateinit var baedalPost: BaedalPost
    var comments = mutableListOf<Comment>()
    var replyTo: Comment? = null

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
        AC = ActivityController(activity as MainActivity)

        refreshView()
        binding.lytContent.setOnTouchListener(this)
        binding.lytComment.setOnTouchListener(this)
        binding.rvComment.setOnTouchListener(this)

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        hideSoftInput()
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> Log.d("[$TAG][온터치]", "터치시작")
            MotionEvent.ACTION_MOVE -> {
                isScrolled = true
                Log.d("[$TAG][온터치]", "움직임")
            }
            MotionEvent.ACTION_UP -> {
                Log.d("[$TAG][온터치]", "터치끝")
                if (!isScrolled) hideSoftInput()
                isScrolled = false
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        Log.d("access", prefs.getString("accessToken", ""))
        Log.d("postId", postId.toString())
        binding.btnOrder.visibility = View.GONE
        binding.btnComplete.visibility = View.GONE
        getPostInfo()
        getComments()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPostInfo() {
        AC.showProgressBar()
        api.getBaedalPost(postId!!).enqueue(object : Callback<BaedalPost> {
            override fun onResponse(call: Call<BaedalPost>, response: Response<BaedalPost>) {
                AC.hideProgressBar()
                if (response.code() == 200) {
                    baedalPost = response.body()!!
                    Log.d(TAG+"baedalPost", baedalPost.toString())
                    setPost()
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        makeToast(errorResponse.msg)
                        Log.d("$TAG[getPostInfo]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e: Exception) { Log.e("$TAG[getPostInfo]", e.toString())}
                    onBackPressed()
                }
            }

            override fun onFailure(call: Call<BaedalPost>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("baedal Post Fragment - getBaedalPost", t.message.toString())
                makeToast("게시글 조회 실패")
                onBackPressed()
            }
        })
    }

    fun getAccountNum() {
        if (isMember && baedalPost.status == "delivered") {
            binding.lytAccountNumber.visibility = View.VISIBLE
            AC.showProgressBar()
            api.getAccountNumber(postId!!).enqueue(object : Callback<AccountNumber> {
                override fun onResponse(call: Call<AccountNumber>, response: Response<AccountNumber>) {
                    AC.hideProgressBar()
                    if (response.code() == 200) {
                        binding.tvAccountNumber.text = response.body()!!.AccountNumber
                        binding.btnCopyAccountNum.setOnClickListener {
                            copyToClipboard("대표자 계좌번호", binding.tvAccountNumber.text.toString())
                        }
                    } else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            makeToast(errorResponse.msg)
                            Log.d("$TAG[getAccountNum]", "${errorResponse.code}: ${errorResponse.msg}")
                        } catch (e:Exception) { Log.e("$TAG[getComments]", e.toString())}
                        finally { binding.tvAccountNumber.text = "계좌번호를 조회할 수 없습니다."  }
                    }
                }

                override fun onFailure(call: Call<AccountNumber>, t: Throwable) {
                    AC.hideProgressBar()
                    binding.tvAccountNumber.text = "계좌번호를 조회할 수 없습니다."
                    Log.e("baedal Post Fragment - getComments", t.message.toString())
                    makeToast("댓글 조회 실패")
                }
            })
        } else binding.lytAccountNumber.visibility = View.GONE
    }

    fun getComments() {
        cancelReply()
        binding.etComment.setText("")
        AC.showProgressBar()
        api.getComments(postId!!).enqueue(object : Callback<GetComments> {
            override fun onResponse(call: Call<GetComments>, response: Response<GetComments>) {
                AC.hideProgressBar()
                if (response.code() == 200) {
                    Log.d("FragBaedalPost getComments", response.toString())
                    Log.d("FragBaedalPost getComments body", response.body()!!.toString())
                    comments.clear()
                    comments = response.body()!!.comments
                    setComments()
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        makeToast(errorResponse.msg)
                        Log.d("$TAG[getComments]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e:Exception) { Log.e("$TAG[getComments]", e.toString())}
                }
            }

            override fun onFailure(call: Call<GetComments>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("baedal Post Fragment - getComments", t.message.toString())
                makeToast("댓글 조회 실패")
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setPost() {
        val joinUsers = baedalPost.users
        isMember = joinUsers.contains(userId)
        val store = baedalPost.store
        val orderTime = LocalDateTime.parse(baedalPost.orderTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))

        binding.tvStoreName.text = store.name
        if (userId == baedalPost.userId) {
            binding.tvDelete.text = "삭제"
            binding.tvUpdate.text = "수정"
            binding.tvDelete.visibility = View.VISIBLE
            binding.tvUpdate.visibility = View.VISIBLE
        } else {
            binding.tvDelete.visibility = View.GONE
            binding.tvUpdate.visibility = View.GONE
        }

        if (baedalPost.status != "recruiting") {
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
                .setMessage("게시글을 수정하시겠습니까?")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    setFrag(FragmentBaedalAdd(), mapOf(
                        "isUpdating" to "true",
                        "postId" to postId!!,
                        "orderTime" to baedalPost.orderTime,
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

        /** 가게 정보 */
        binding.tvTelNum.text = store.telNum
        binding.tvMinOrder.text = "${dec.format(store.minOrder)}원"
        binding.tvFee.text = "${dec.format(baedalPost.store.fee)}원"

        var noteStr = ""
        for ((idx, note) in store.note.withIndex()) {
            noteStr += note
            if (idx < store.note.size - 1)
                noteStr += "\n"
        }
        if (noteStr.trim() == "") noteStr = "없음"
        binding.tvNote.text = noteStr

        /** 포스트 내용 바인딩 */

        binding.tvOrderTime.text = orderTime.format(
            DateTimeFormatter.ofPattern("M월 d일(E) H시 m분",Locale.KOREAN)
        )
        binding.tvCurrentMember.text = "${baedalPost.users.size}명 (최소 ${baedalPost.minMember}명 필요)"
        binding.tvConfirmedFee.text = "${dec.format(baedalPost.fee)}원"
        if (baedalPost.userId == userId) {
            binding.lytConfirmedFee.visibility = View.VISIBLE
            binding.btnUpdateFee.visibility = View.VISIBLE
            binding.btnUpdateFee.setOnClickListener { onUpdateFee() }
        } else {
            binding.btnUpdateFee.visibility = View.GONE
            if (baedalPost.status == "delivered") binding.lytConfirmedFee.visibility = View.VISIBLE
            else binding.lytConfirmedFee.visibility = View.GONE
        }

        getAccountNum()

        /** 하단 버튼 바인딩 */
        binding.lytStatusOpen.setOnClickListener { if (baedalPost.status == "closed") setStatus("recruiting")}
        binding.lytStatusClosed.setOnClickListener { if (baedalPost.status == "recruiting") setStatus("closed") }

        bindStatusBtn()
        bindBottomBtn()
        setBottomBtn()
    }

    fun deletePost() {
        AC.showProgressBar()
        api.deleteBaedalPost(postId!!).enqueue(object : Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                AC.hideProgressBar()
                if (response.code() == 204) {
                    Log.d("FragBaedalPost-deletePost", "성공")
                    val bundle = bundleOf("success" to true)
                    getActivity()?.getSupportFragmentManager()?.setFragmentResult("deletePost", bundle)
                    onBackPressed()
                }
                else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        makeToast(errorResponse.msg)
                        Log.d("$TAG[deletePost]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e:Exception) { Log.e("$TAG[deletePost]", e.toString())}
                }
            }
            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                AC.hideProgressBar()
                Log.d("log",t.message.toString())
                Log.d("log","fail")
                makeToast("다시 시도해주세요.")
            }
        })
    }

    fun onUpdateFee() {
        val builder = AlertDialog.Builder(requireContext())
        val builderItem = EtBuilder()
        builderItem.init()

        builder.setTitle("배달비 변경")
            .setMessage("변경된 배달비를 입력해주세요")
            .setView(builderItem.getView().root)
            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                updateFee(builderItem.getFee())
            })
            .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
        builder.show()
    }

    fun bindStatusBtn() {
        binding.ivStatus.setImageResource(R.drawable.baseline_person_off_black_24)
        if (baedalPost.userId == userId) {
            binding.btnOrder.visibility = View.GONE
            if (baedalPost.status == "recruiting" || baedalPost.status == "closed") {
                binding.tvStatus.visibility = View.GONE
            } else {
                binding.tvStatus.visibility = View.VISIBLE
                binding.lytStatus.visibility = View.GONE
            }
        }
        else {
            binding.lytStatus.visibility = View.GONE
            binding.btnComplete.visibility = View.GONE
        }

        when (baedalPost.status) {
            "recruiting" -> {
                binding.ivStatus.setImageResource(R.drawable.baseline_person_black_24)
                binding.lytStatusOpen.setBackgroundResource(R.drawable.patch_green_10_green_left)
                binding.tvStatusOpen.setTextColor(Color.WHITE)
                binding.lytStatusClosed.setBackgroundResource(R.drawable.patch_white_10_silver_right)
                binding.tvStatusClosed.setTextColor(Color.GRAY)
                binding.tvStatus.text = "모집중"
            }
            "closed" -> {
                binding.lytStatusOpen.setBackgroundResource(R.drawable.patch_white_10_silver_left)
                binding.tvStatusOpen.setTextColor(Color.GRAY)
                binding.lytStatusClosed.setBackgroundResource(R.drawable.patch_silver_10_gray_right)
                binding.tvStatusClosed.setTextColor(Color.WHITE)
                binding.tvStatus.text = "모집 마감"
            }
            "ordered" -> binding.tvStatus.text = "모집 마감 (주문 완료)"
            "delivered" -> binding.tvStatus.text = "모집 마감 (배달 완료)"
            "canceld" -> binding.tvStatus.text = "취소"
            else -> binding.tvStatus.text = "모집 마감"
        }

        //setLayoutListner(binding.constraintLayout17, "내용")
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
                else -> binding.btnComplete.visibility = View.GONE
            }
        } else {
            if (isMember) {
                binding.btnViewMyOrders.visibility = View.VISIBLE
            } else {
                binding.btnViewMyOrders.visibility = View.GONE
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

        //setLayoutListner(binding.lytBottomButton, "버튼")
    }

    fun bindBtnOrder(background: Boolean, isEnabled: Boolean, text: String) {
        if (background) binding.btnOrder.setBackgroundResource(R.drawable.solid_primary)
        else binding.btnOrder.setBackgroundResource(R.drawable.solid_gray)
        binding.btnOrder.isEnabled = isEnabled
        binding.tvOrder.text = text
    }

    fun setBottomBtn() {
        binding.btnViewMyOrders.setOnClickListener {
            setFrag(FragmentBaedalOrders(), mapOf(
                "postJson" to gson.toJson(baedalPost),
                "isMyOrder" to "true"
            ))
        }
        binding.btnViewAllOrders.setOnClickListener {
            setFrag(FragmentBaedalOrders(), mapOf(
                "postJson" to gson.toJson(baedalPost),
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
                .setMessage("주문을 취소하시겠습니까?\n다시 참가하기 위해선 주문을 다시 작성해야합니다.")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    deleteOrders()
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
            builder.show()
        }
        else {
            if (baedalPost.users.size < baedalPost.maxMember) {
                prefs.setString("minMember", baedalPost.minMember.toString())
                setFrag(FragmentBaedalMenu(), mapOf("postId" to postId!!, "storeId" to baedalPost.store._id))
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("인원 마감")
                    .setMessage("참여 가능한 최대 인원에 도달했습니다.\n대표자에게 문의하세요")
                    .setPositiveButton("확인", DialogInterface.OnClickListener{_, _ ->})
            }
        }
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
            val builderItem = EtBuilder()
            builderItem.init()

            builder.setTitle("배달 완료")
                .setMessage("배달이 완료되었나요?\n주문 참가자들에게 알림이 전송됩니다.\n확정된 배달비를 입력해주세요.")
                .setView(builderItem.getView().root)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    updateFee(builderItem.getFee(), true)
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
            builder.show()} // 배달 완료
    }

    fun setStatus(status: String) {
        AC.showProgressBar()
        api.setBaedalStatus(postId!!, BaedalStatus(status)).enqueue(object : Callback<VoidResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                AC.hideProgressBar()
                if (response.code() == 204) {
                    makeToast("상태가 변경되었습니다.")
                    getPostInfo()
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        makeToast(errorResponse.msg)
                        Log.d("$TAG[setStatus]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e:Exception) { Log.e("$TAG[setStatus]", e.toString())}
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("FragBaedalPost setStatus", t.message.toString())
                makeToast("상태 변경에 실패했습니다.")
            }
        })
    }

    fun updateFee(fee: Int, completed: Boolean=false) {
        if (fee != baedalPost.fee) {
            AC.showProgressBar()
            api.updateBaedalFee(postId!!, Fee(fee)).enqueue(object : Callback<VoidResponse> {
                    override fun onResponse(call: Call<VoidResponse>,response: Response<VoidResponse>) {
                        AC.hideProgressBar()
                        if (response.code() == 204) {
                            makeToast("배달비가 변경되었습니다.")
                            if (completed) setStatus("delivered")
                            else getPostInfo()
                        } else {
                            try {
                                val errorBody = response.errorBody()?.string()
                                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                                makeToast(errorResponse.msg)
                                Log.d("$TAG[updateFee]", "${errorResponse.code}: ${errorResponse.msg}")
                            } catch (e:Exception) { Log.e("$TAG[updateFee]", e.toString())}
                        }
                    }

                    override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                        AC.hideProgressBar()
                        Log.e(TAG+"updateFee", t.message.toString())
                        makeToast("다시시도해주세요.")
                    }
                })
        } else setStatus("delivered")
    }

    inner class EtBuilder {
        private val view = AlertdialogInputtextBinding.inflate(layoutInflater)

        fun init() {
            view.etFee.setText(baedalPost.fee.toString())
        }

        fun getView(): AlertdialogInputtextBinding { return view }

        fun getFee(): Int {
            return view.etFee.text.toString().replace(",","").toInt()
        }


    }

    fun deleteOrders() {
        AC.showProgressBar()
        api.deleteOrders(postId!!).enqueue(object : Callback<VoidResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                AC.hideProgressBar()
                if (response.code() == 204) {
                    makeToast("주문이 취소되었습니다.")
                    getPostInfo()
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        makeToast(errorResponse.msg)
                        Log.d("$TAG[deleteOrders]", "${errorResponse.code}: ${errorResponse.msg}")
                    } catch (e:Exception) { Log.e("$TAG[deleteOrders]", e.toString())}
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                AC.hideProgressBar()
                Log.e("FragBaedalPost deleteOrders", t.message.toString())
                makeToast("주문 취소 실패")
            }
        })
    }

    fun setComments() {
        var count = 0
        for (comment in comments) {
            if (comment.status == "created") count++
        }
        binding.tvCommentCount.text = "댓글 $count"
        binding.rvComment.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvComment.setHasFixedSize(true)
        val adapter = CommentAdapter(requireContext(), comments, userId)
        binding.rvComment.adapter = adapter

        adapter.setDeleteListener(object: CommentAdapter.OnDeleteListener {
            override fun deleteComment() {
                getComments()
            }
        })
        adapter.setReplyListener(object : CommentAdapter.OnReplyListener {
            override fun makeReply(parentComment: Comment) {
                replyTo = parentComment
                binding.tvReplyTo.text = "${replyTo!!.nickname}님에게 대댓글"
                binding.lytReplyTo.visibility = View.VISIBLE
                showSoftInput(binding.etComment)
            }
        })

        binding.lytReplyTo.visibility = View.GONE
        binding.btnCancelReply.setOnClickListener { cancelReply() }

        binding.btnPostComment.setOnClickListener {
            val content = binding.etComment.text.toString()
            if (content.trim() != "") postComment(content, replyTo?._id)
        }

        //setLayoutListner(binding.lytComment, "댓글")
    }

    fun cancelReply() {
        replyTo = null
        binding.lytReplyTo.visibility = View.GONE
    }

    fun postComment(content: String, parentId: String? = null) {
        AC.showProgressBar()
        if (parentId == null) {
            api.postComment(postId!!, PostComment(content)).enqueue(object : Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                    AC.hideProgressBar()
                    if (response.code() == 204) {
                        Log.d("FragBaedalPost postComment", "성공")
                        requestNotiPermission()
                        getComments()
                    } else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            makeToast(errorResponse.msg)
                            Log.d("$TAG[postComment]", "${errorResponse.code}: ${errorResponse.msg}")
                        } catch (e:Exception) {
                            Log.e("$TAG[postComment]", e.toString())
                            Log.d("$TAG[postComment]", response.errorBody()?.string().toString())
                        }
                    }
                }

                override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                    AC.hideProgressBar()
                    Log.e("[FAIL][POST][postComment]", t.message.toString())
                    makeToast("다시 시도해주세요.")
                }
            }
            )
        } else {
            api.postSubComment(postId!!, parentId, PostComment(content)).enqueue(object : Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                    AC.hideProgressBar()
                    if (response.code() == 204) {
                        Log.d("FragBaedalPost postComment", "성공")
                        getComments()
                    } else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            makeToast(errorResponse.msg)
                            Log.d("$TAG[postSubComment]", "${errorResponse.code}: ${errorResponse.msg}")
                        } catch (e:Exception) { Log.e("$TAG[postSubComment]", e.toString())}
                    }
                }

                override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                    AC.hideProgressBar()
                    Log.e("[FAIL][POST][postSubComment]", t.message.toString())
                    makeToast("다시 시도해주세요.")
                }
            })
        }
    }

    fun requestNotiPermission() {
        val mActivity = activity as MainActivity
        mActivity.requestNotiPermission()
    }

    fun showSoftInput(view: View) {
        view.requestFocus()
        val mActivity = activity as MainActivity
        mActivity.showSoftInput(view)
    }

    fun hideSoftInput() {
        Log.d("$TAG[하이드 소프트 인풋]", "")
        val mActivity = activity as MainActivity
        mActivity.hideSoftInput()
    }

    fun copyToClipboard(label:String="", content: String) {
        val mActivity = activity as MainActivity
        mActivity.copyToClipboard(label, content)
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
        Log.d("$TAG[뒤로가기 키 눌림]", "")
//        cancelReply()
        hideSoftInput()
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}