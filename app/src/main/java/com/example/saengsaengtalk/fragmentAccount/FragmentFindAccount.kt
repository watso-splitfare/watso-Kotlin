package com.example.saengsaengtalk.fragmentAccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.databinding.FragFindAccountBinding

class FragmentFindAccount :Fragment() {
    var forgot = "id"

    private var mBinding: FragFindAccountBinding? = null
    private val binding get() = mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragFindAccountBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }

    fun refreshView() {
        binding.btnPrevious.setOnClickListener {
            onBackPressed()
        }

        binding.tvFindId.setOnClickListener {
            forgot = "id"
            binding.tvFindId.setTextColor(ContextCompat.getColor(requireContext(), R.color.kara))
            //binding.tvFindId.setTextAppearance()
            binding.tvFindPw.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.tvLine.text = "가입할 때 입력하셨던 이메일을 입력해 주세요."
            //binding.etInput.text = ""
            binding.etInputMail.visibility = View.VISIBLE
            binding.etInputId.visibility = View.INVISIBLE

            binding.btnFind.text = "아이디 찾기"
        }
        binding.tvFindPw.setOnClickListener {
            forgot = "pw"
            binding.tvFindId.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.tvFindPw.setTextColor(ContextCompat.getColor(requireContext(), R.color.kara))
            binding.tvLine.text = "비밀번호를 찾을 아이디를 입력해 주세요."
            //binding.etInput.text = ""
            binding.etInputMail.visibility = View.INVISIBLE
            binding.etInputId.visibility = View.VISIBLE
            binding.btnFind.text = "비밀번호 찾기"
        }
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