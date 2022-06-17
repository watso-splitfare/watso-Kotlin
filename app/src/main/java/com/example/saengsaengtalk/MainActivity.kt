package com.example.saengsaengtalk

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.saengsaengtalk.databinding.ActivityMainBinding
import com.example.saengsaengtalk.fragmentBaedal.FragmentBaedalList
import com.example.saengsaengtalk.fragmentFreeBoard.FragmentFreeBoard
import com.example.saengsaengtalk.fragmentKara.FragmentKara

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

        binding.btnHome.setOnClickListener { setFrag(FragmentHome(), popBackStack = 0) }
        binding.btnBaedal.setOnClickListener { setFrag(FragmentBaedalList(), popBackStack = 0) }
        binding.btnTaxi.setOnClickListener { setFrag(FragmentTaxi(),popBackStack = 0) }
        binding.btnKara.setOnClickListener { setFrag(FragmentKara(), popBackStack = 0) }
        binding.btnFreeBoard.setOnClickListener { setFrag(FragmentFreeBoard(), popBackStack = 0) }
    }

    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, popBackStack:Int=-1) {
        if (arguments != null) {        // 넘겨줄 인자가 있나 체크
            val bundle = Bundle()
            for (i in arguments.keys) {
                bundle.putString(i, arguments[i])
            }
            fragment.arguments = bundle
        }

        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()

        if (popBackStack == -1) {
            transaction.setCustomAnimations(R.anim.enter_from_right, 0, 0, R.anim.exit_to_right)
            transaction.add(R.id.main_frame, fragment).addToBackStack(null)
        } else if (popBackStack == 0) {
            val count = fm.backStackEntryCount
            for (i in 0 until count) {
                fm.popBackStack()
            }
            transaction.replace(R.id.main_frame, fragment)
        } else {
            for (i in 0 until popBackStack) { fm.popBackStack() }
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