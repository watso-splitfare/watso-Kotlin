package com.example.saengsaengtalk.fragmentTaxi

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.adapterHome.Comment
import com.example.saengsaengtalk.adapterHome.CommentAdapter
import com.example.saengsaengtalk.databinding.FragTaxiPostBinding
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentTaxiPost :Fragment() {
    var postNum: Int? = null

    private var mBinding: FragTaxiPostBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postNum = it.getString("postNum")?.toInt()
        }
        println(postNum)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragTaxiPostBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        val content = Content("생자대->밀양역 가실분", "주넝이", LocalDateTime.now(),
            "생자대", "밀양역", LocalDateTime.parse("2022-04-04T15:10:00"), 2,6600,
            "밀양역 가실분 구해요", mutableListOf(Comment("동동이", "저요!",
                LocalDateTime.now(), 0, 0, 0, "ehdehd")))

        binding.tvPostTitle.text = content.title
        binding.tvPostWriter.text = content.writer

        val created = content.created
        val today = LocalDate.now().atTime(0, 0)
        binding.tvPostCreated.text = when (created.isBefore(today)) {
            true -> created.format(DateTimeFormatter.ofPattern("MM/dd"))
            else -> created.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        binding.tvDepart.text = content.depart
        binding.tvDest.text = content.dest
        binding.tvTime.text = content.time.format(DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(
            Locale.forLanguageTag("ko")))
        binding.tvMember.text = "${content.member}명"

        val dec = DecimalFormat("#,###")
        binding.tvPrice.text = "${dec.format(content.price/content.member)}원"
        binding.tvContent.text = content.content

        binding.tvCommentCount.text = "댓글 ${content.comment.size}"
        binding.rvComment.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvComment.setHasFixedSize(true)
        binding.rvComment.adapter = CommentAdapter(content.comment)
    }

    class Content(
        val title: String,
        val writer: String,
        val created: LocalDateTime,
        val depart: String,
        val dest: String,
        val time: LocalDateTime,
        val member: Int,
        val price: Int,
        val content: String,
        val comment: MutableList<Comment>
    ) {}

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}