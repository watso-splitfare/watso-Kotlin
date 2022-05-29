package com.example.saengsaengtalk

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.saengsaengtalk.databinding.ActivityMainBinding
import com.example.saengsaengtalk.fragmentBaedal.FragmentBaedalList
import com.example.saengsaengtalk.fragmentBaedal.FragmentBaedalPost
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!
    var mBackWait:Long = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFrag(FragmentHome())

        binding.btnHome.setOnClickListener { setFrag(FragmentHome(), addBackStack = false) }
        binding.btnBaedal.setOnClickListener { setFrag(FragmentBaedalList(), addBackStack = false) }
        binding.btnTaxi.setOnClickListener { setFrag(FragmentBaedalPost(),addBackStack = false) }
        binding.btnKara.setOnClickListener { setFrag(FragmentKara(), addBackStack = false) }
        binding.btnFreeBoard.setOnClickListener { setFrag(FragmentFreeBoard(), addBackStack = false) }
    }

    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, addBackStack:Boolean=true) {
        if (arguments != null) {        // 넘겨줄 인자가 있나 체크
            val bundle = Bundle()
            for (i in arguments.keys) {
                bundle.putString(i, arguments[i])
            }
            fragment.arguments = bundle
        }

        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()

        if (addBackStack) {
            transaction.setCustomAnimations(R.anim.enter_from_right, 0, 0, R.anim.exit_to_right)
            transaction.add(R.id.main_frame, fragment).addToBackStack(null)
        }

        else {
            val count = fm.backStackEntryCount
            for (i in 0 until count) {
                fm.popBackStack()
            }
            transaction.replace(R.id.main_frame, fragment)
        }

        transaction.commit()
    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        val count = fm.backStackEntryCount
        if (count > 0)
            super.onBackPressed()
        else {
            if(System.currentTimeMillis() - mBackWait >= 2000) {
                mBackWait = System.currentTimeMillis()
                Toast.makeText(this, "뒤로가기 버튼을 한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
            } else {
                finish()
            }
        }
    }
}