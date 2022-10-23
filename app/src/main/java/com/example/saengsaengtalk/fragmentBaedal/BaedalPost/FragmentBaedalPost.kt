package com.example.saengsaengtalk.fragmentBaedal.BaedalPost

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.APIS.BaedalPostModel
import com.example.saengsaengtalk.APIS.Order
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.adapterHome.CommentAdapter
import com.example.saengsaengtalk.databinding.FragBaedalPostBinding
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

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        getPostInfo()
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

                if (userId != baedalPost.user.user_id) {
                    if (baedalPost.is_closed){
                        binding.tvOrder.text = "주문이 마감되었습니다."
                        binding.lytOrder.isEnabled = false
                        binding.lytOrder.setBackgroundResource(R.drawable.btn_baedal_order_closed)
                    } else {
                        binding.tvOrder.text = "나도 주문하기"
                        binding.lytOrder.setOnClickListener {
                            setFrag(
                                FragmentBaedalMenu(),
                                mapOf("postId" to postId!!, "member" to baedalPost.current_member.toString(), "isPosting" to "false")
                            )
                        }
                    }
                } else {
                    binding.tvOrder.text = "주문 마감하기"
                    binding.ivOrder.visibility = View.GONE
                    binding.lytOrder.setOnClickListener {
                        binding.lytOrder.setBackgroundResource(R.drawable.btn_baedal_order_closed)
                        binding.lytOrder.isEnabled = false
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