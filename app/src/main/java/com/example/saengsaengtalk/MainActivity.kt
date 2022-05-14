package com.example.saengsaengtalk

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

        binding.btnHome.setOnClickListener { setFrag(FragmentHome(), popAllStack = true) }
        binding.btnBaedal.setOnClickListener { setFrag(FragmentBaedalList(), popAllStack = true) }
        binding.btnTaxi.setOnClickListener { setFrag(FragmentBaedalPost(), popAllStack = true) }
        binding.btnKara.setOnClickListener { setFrag(FragmentKara(), popAllStack = true) }
        binding.btnFreeBoard.setOnClickListener { setFrag(FragmentFreeBoard(), popAllStack = true) }
    }

    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }

    fun setFrag(fragment: Fragment, postNum:String="", addBackStack:Boolean=false, popAllStack:Boolean=false) {
        if (postNum != "") {            // 넘겨줄 인자가 있나 체크
            val bundle = Bundle()
            bundle.putString("postNum", postNum)

            fragment.arguments = bundle
        }

        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()


        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)


        if (addBackStack)
            transaction.replace(R.id.main_frame, fragment).addToBackStack(null)
        else
            transaction.replace(R.id.main_frame, fragment)

        if (popAllStack) {
            val count = fm.backStackEntryCount
            for (i in 0 until count)
                fm.popBackStack()
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