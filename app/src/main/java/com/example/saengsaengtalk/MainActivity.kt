package com.example.saengsaengtalk

import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.example.saengsaengtalk.databinding.ActivityMainBinding
import com.example.saengsaengtalk.fragmentBaedal.FragmentBaedalList
import com.example.saengsaengtalk.fragmentFreeBoard.FragmentFreeBoard
import com.example.saengsaengtalk.fragmentKara.FragmentKara


class MainActivity : AppCompatActivity() {

    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!
    var mBackWait:Long = 0
    var bottomBarIndex:Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFrag(FragmentHome())

        binding.btnHome.setOnClickListener { setFrag(FragmentHome(), popBackStack = 0, index=0) }
        binding.btnBaedal.setOnClickListener { setFrag(FragmentBaedalList(), popBackStack = 0, index=1) }
        binding.btnTaxi.setOnClickListener { setFrag(FragmentTaxi(),popBackStack = 0, index=2) }
        binding.btnKara.setOnClickListener { setFrag(FragmentKara(), popBackStack = 0, index=3) }
        binding.btnFreeBoard.setOnClickListener { setFrag(FragmentFreeBoard(), popBackStack = 0, index=4) }
    }

    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, popBackStack:Int = -1, index:Int = 0) {
        if (bottomBarIndex != index) setBottomBarSize(index)

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

    fun setBottomBarSize(index: Int) {
        val r: Resources = resources
        var small_size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,30F, r.displayMetrics).toInt()
        var big_size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,40F, r.displayMetrics).toInt()

        when (bottomBarIndex) {
            0 -> binding.btnHome.updateLayoutParams {
                    width = small_size
                    height = small_size
            }

            1 -> binding.btnBaedal.updateLayoutParams {
                    width = small_size
                    height = small_size
            }
            2 -> binding.btnTaxi.updateLayoutParams {
                    width = small_size
                    height = small_size
            }
            3 -> binding.btnKara.updateLayoutParams {
                    width = small_size
                    height = small_size
            }
            4 -> binding.btnFreeBoard.updateLayoutParams {
                    width = small_size
                    height = small_size
            }
        }

        when (index) {
            0 -> binding.btnHome.updateLayoutParams {
                    width = big_size
                    height = big_size
            }
            1 -> binding.btnBaedal.updateLayoutParams {
                    width = big_size
                    height = big_size
            }
            2 -> binding.btnTaxi.updateLayoutParams {
                    width = big_size
                    height = big_size
            }
            3 -> binding.btnKara.updateLayoutParams {
                    width = big_size
                    height = big_size
            }
            4 -> binding.btnFreeBoard.updateLayoutParams {
                    width = big_size
                    height = big_size
            }
        }
        bottomBarIndex = index
    }
}