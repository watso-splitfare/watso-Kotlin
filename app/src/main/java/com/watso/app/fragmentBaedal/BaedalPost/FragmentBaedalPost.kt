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
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.watso.app.API.*
import com.watso.app.API.DataModels.ErrorResponse
import com.watso.app.LoopingDialog
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
    val prefs = MainActivity.prefs
    var isScrolled = false
    var infoHeight = 0
    var needMargin = 0
    var acconutHeight = 0

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

    override fun onDestroyView() {
        super.onDestroyView()
        hideSoftInput()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalPostBinding.inflate(inflater, container, false)

        refreshView()
        binding.lytContent.setOnTouchListener(this)
        binding.lytComment.setOnTouchListener(this)
        binding.rvComment.setOnTouchListener(this)
        return binding.root
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_MOVE -> isScrolled = true
            MotionEvent.ACTION_UP -> {
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
        val loopingDialog = looping()
        api.getBaedalPost(postId!!).enqueue(object : Callback<BaedalPost> {
            override fun onResponse(call: Call<BaedalPost>, response: Response<BaedalPost>) {
                looping(false, loopingDialog)
                if (response.code() == 200) {
                    baedalPost = response.body()!!
                    Log.d(TAG+"baedalPost", baedalPost.toString())
                    setPost()
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        makeToast(errorResponse.msg)
                        Log.d("$TAG[getPostInfo]", errorResponse.msg)
                    } catch (e: Exception) { Log.e("$TAG[getPostInfo]", e.toString())}
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

    fun getComments() {
        cancelReply()
        binding.etComment.setText("")
        val loopingDialog = looping()
        api.getComments(postId!!).enqueue(object : Callback<GetComments> {
            override fun onResponse(call: Call<GetComments>, response: Response<GetComments>) {
                looping(false, loopingDialog)
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
                        Log.d("$TAG[getComments]", errorResponse.msg)
                    } catch (e:Exception) { Log.e("$TAG[getComments]", e.toString())}
                }
            }

            override fun onFailure(call: Call<GetComments>, t: Throwable) {
                looping(false, loopingDialog)
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

        binding.tvMinOrder.text = store.minOrder.toString()
        binding.tvFee.text = baedalPost.fee.toString()
        binding.tvTelNum.text = store.telNum
        var noteStr = ""
        for ((idx, note) in store.note.withIndex()) {
            noteStr += note
            if (idx < store.note.size - 1)
                noteStr += "\n"
        }
        binding.tvNote.text = noteStr

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

        /** 포스트 내용 바인딩 */

        binding.tvStore.text = store.name
        binding.lytStore.setOnClickListener { binding.lytContent.scrollTo(0, 0) }
        binding.tvOrderTime.text = orderTime.format(
            DateTimeFormatter.ofPattern("M월 d일(E) H시 m분",Locale.KOREAN)
        )
        binding.tvCurrentMember.text = "${baedalPost.users.size}명 (최소 ${baedalPost.minMember}명 필요)"
        binding.tvFee.text = "${dec.format(store.fee)}원"

        /** 하단 버튼 바인딩 */
        /*binding.lytStatus.setOnClickListener {
            //bindStatusBtn()
            if (baedalPost.status == "recruiting") setStatus("closed")
            else setStatus("recruiting")
        }*/
        binding.lytStatusOpen.setOnClickListener { if (baedalPost.status == "closed") setStatus("recruiting")}
        binding.lytStatusClosed.setOnClickListener { if (baedalPost.status == "recruiting") setStatus("closed") }

        bindStatusBtn()
        bindBottomBtn()
        setBottomBtn()

        val contentView = binding.lytContent
        val marginLayoutParams = binding.scrollMargin.layoutParams

        /** 가게 정보의 길이에 맞게 스크롤의 길이를 늘린다 */
        binding.lytStoreInfo.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                infoHeight = binding.lytStoreInfo.height
                contentView.scrollTo(0, infoHeight)
                marginLayoutParams.height = infoHeight
                binding.scrollMargin.layoutParams = marginLayoutParams
                binding.scrollMargin.requestLayout()
                Log.d("$TAG[스토어 인포 옵저버][infoHeight]", infoHeight.toString())
                Log.d("$TAG[스토어 인포 옵저버][contentView.scrollY]", contentView.scrollY.toString())
                if (contentView.scrollY == infoHeight)
                    binding.lytStoreInfo.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        contentView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            //Log.d(TAG, "scroolY: $scrollY")
            val max = contentView.getChildAt(0).height - contentView.height
            val alpha = 1.0f - ( (max-scrollY).toFloat() / max.toFloat())

            binding.lytBack.alpha = alpha
        }

    }

    fun setLayoutListner(view: View, where: String="") {
        view.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (where != "") Log.d("$TAG[컨텐츠 옵저버]:", where)
                val windowh = binding.constraintLayout2.height
                val headh = binding.lytHead.height
                val realContenth = binding.lytContentWithComment.height//posth + commenth
                val addComh = binding.lytAddComment.height
                needMargin = windowh - headh - realContenth - addComh
                if (needMargin < 0) needMargin = 0
                val contentLayoutParams = binding.lytContentWithComment.layoutParams as ViewGroup.MarginLayoutParams
                contentLayoutParams.setMargins(0, 0, 0, needMargin)
                binding.lytContentWithComment.layoutParams = contentLayoutParams
                binding.lytContentWithComment.requestLayout()

                if (binding.lytContentWithComment.marginBottom == needMargin)
                    view.viewTreeObserver.removeOnGlobalLayoutListener(this)
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

        setLayoutListner(binding.lytComment, "댓글")
    }

    fun cancelReply() {
        replyTo = null
        binding.lytReplyTo.visibility = View.GONE
    }

    fun postComment(content: String, parentId: String? = null) {
        val loopingDialog = looping()
        if (parentId == null) {
            api.postComment(postId!!, PostComment(content)).enqueue(object : Callback<VoidResponse> {
                    override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                        looping(false, loopingDialog)
                        if (response.code() == 204) {
                            Log.d("FragBaedalPost postComment", "성공")
                            requestNotiPermission()
                            getComments()
                        } else {
                            try {
                                val errorBody = response.errorBody()?.string()
                                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                                makeToast(errorResponse.msg)
                                Log.d("$TAG[postComment]", errorResponse.msg)
                            } catch (e:Exception) {
                                Log.e("$TAG[postComment]", e.toString())
                                Log.d("$TAG[postComment]", response.errorBody()?.string().toString())
                            }
                        }
                    }

                    override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                        looping(false, loopingDialog)
                        Log.e("[FAIL][POST][postComment]", t.message.toString())
                        makeToast("다시 시도해주세요.")
                    }
                }
            )
        } else {
            api.postSubComment(postId!!, parentId, PostComment(content)).enqueue(object : Callback<VoidResponse> {
                override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                    looping(false, loopingDialog)
                    if (response.code() == 204) {
                        Log.d("FragBaedalPost postComment", "성공")
                        getComments()
                    } else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            makeToast(errorResponse.msg)
                            Log.d("$TAG[postSubComment]", errorResponse.msg)
                        } catch (e:Exception) { Log.e("$TAG[postSubComment]", e.toString())}
                    }
                }

                override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                    looping(false, loopingDialog)
                    Log.e("[FAIL][POST][postSubComment]", t.message.toString())
                    makeToast("다시 시도해주세요.")
                }
            }
            )
        }
    }

    fun deletePost() {
        val loopingDialog = looping()
        api.deleteBaedalPost(postId!!).enqueue(object : Callback<VoidResponse> {
            override fun onResponse(call: Call<VoidResponse>, response: Response<VoidResponse>) {
                looping(false, loopingDialog)
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
                        Log.d("$TAG[deletePost]", errorResponse.msg)
                    } catch (e:Exception) { Log.e("$TAG[deletePost]", e.toString())}
                }
            }
            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                looping(false, loopingDialog)
                Log.d("log",t.message.toString())
                Log.d("log","fail")
                makeToast("다시 시도해주세요.")
            }
        })
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
                binding.lytStatusOpen.setBackgroundResource(R.drawable.btn_baedal_open_pressed)
                binding.tvStatusOpen.setTextColor(Color.WHITE)
                binding.lytStatusClosed.setBackgroundResource(R.drawable.btn_baedal_close_released)
                binding.tvStatusClosed.setTextColor(Color.GRAY)
                binding.tvStatus.text = "모집중"
            }
            "closed" -> {
                binding.lytStatusOpen.setBackgroundResource(R.drawable.btn_baedal_open_released)
                binding.tvStatusOpen.setTextColor(Color.GRAY)
                binding.lytStatusClosed.setBackgroundResource(R.drawable.btn_baedal_close_pressed)
                binding.tvStatusClosed.setTextColor(Color.WHITE)
                binding.tvStatus.text = "모집 마감"
            }
            "ordered" -> binding.tvStatus.text = "모집 마감 (주문 완료)"
            "delivered" -> binding.tvStatus.text = "모집 마감 (배달 완료)"
            "canceld" -> binding.tvStatus.text = "취소"
            else -> binding.tvStatus.text = "모집 마감"
        }

        setLayoutListner(binding.constraintLayout17, "내용")
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

        setLayoutListner(binding.lytBottomButton, "버튼")
    }

    fun bindBtnOrder(background: Boolean, isEnabled: Boolean, text: String) {
        if (background) binding.btnOrder.setBackgroundResource(R.drawable.btn_primary_blue_10)
        else binding.btnOrder.setBackgroundResource(R.drawable.btn_primary_gray_10)
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
                    updateFee(builderItem.getFee())
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
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        makeToast(errorResponse.msg)
                        Log.d("$TAG[setStatus]", errorResponse.msg)
                    } catch (e:Exception) { Log.e("$TAG[setStatus]", e.toString())}
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                looping(false, loopingDialog)
                Log.e("FragBaedalPost setStatus", t.message.toString())
                makeToast("상태 변경에 실패했습니다.")
            }
        })
    }

    fun updateFee(fee: Int) {
        if (fee != baedalPost.fee) {
            val loopingDialog = looping()
            api.updateBaedalFee(postId!!, Fee(fee)).enqueue(object : Callback<VoidResponse> {
                    override fun onResponse(call: Call<VoidResponse>,response: Response<VoidResponse>) {
                        looping(false, loopingDialog)
                        if (response.code() == 204) {
                            makeToast("배달비가 변경되었습니다.")
                            setStatus("delivered")
                        } else {
                            try {
                                val errorBody = response.errorBody()?.string()
                                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                                makeToast(errorResponse.msg)
                                Log.d("$TAG[updateFee]", errorResponse.msg)
                            } catch (e:Exception) { Log.e("$TAG[updateFee]", e.toString())}
                        }
                    }

                    override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                        looping(false, loopingDialog)
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
            /*view.etFee.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    view.etFee.setText(dec.format(view.etFee.text))
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })*/
        }

        fun getView(): AlertdialogInputtextBinding { return view }

        fun getFee(): Int {
            return view.etFee.text.toString().replace(",","").toInt()
        }


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
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        makeToast(errorResponse.msg)
                        Log.d("$TAG[deleteOrders]", errorResponse.msg)
                    } catch (e:Exception) { Log.e("$TAG[deleteOrders]", e.toString())}
                }
            }

            override fun onFailure(call: Call<VoidResponse>, t: Throwable) {
                looping(false, loopingDialog)
                Log.e("FragBaedalPost deleteOrders", t.message.toString())
                makeToast("주문 취소 실패")
            }
        })
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
        Log.d(TAG, "키보드 숨기기")
        Log.d(TAG, view.toString())
        val mActivity = activity as MainActivity
        return mActivity.hideSoftInput()
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
        cancelReply()
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}