package com.example.saengsaengtalk

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.databinding.ActivityMainBinding
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val baedalList = arrayListOf(
            BaedalPre(LocalDateTime.now(), "네네치킨", 2, 10000),
            BaedalPre(LocalDateTime.now(), "BBQ", 3, 10000),
            BaedalPre(LocalDateTime.now(), "마라탕", 2, 10000),
            BaedalPre(LocalDateTime.now(), "피자", 3, 9000),
            BaedalPre(LocalDateTime.now(), "치킨", 4, 6000)
        )
        binding.rvBaedalPre.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBaedalPre.setHasFixedSize(true)

        if (baedalList.size > 5)
            binding.rvBaedalPre.adapter = BaedalPreAdapter(baedalList.subList(0,5))
        else
            binding.rvBaedalPre.adapter = BaedalPreAdapter(baedalList)


        val taxiList = arrayListOf(
            TaxiPre(LocalDateTime.now(), "생자대", "밀양역", 1, 6600),
            TaxiPre(LocalDateTime.now(), "생자대", "밀양역", 2, 6600),
            TaxiPre(LocalDateTime.now(), "밀양역", "생자대", 3, 6600),
            TaxiPre(LocalDateTime.now(), "밀양역", "생자대", 3, 6600),
            TaxiPre(LocalDateTime.now(), "생자대", "영남루", 1, 6600)
        )
        binding.rvTaxiPre.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvTaxiPre.setHasFixedSize(true)

        if (taxiList.size > 5)
            binding.rvTaxiPre.adapter = TaxiPreAdapter(taxiList.subList(0,5))
        else
            binding.rvTaxiPre.adapter = TaxiPreAdapter(taxiList)


    }

    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }
}