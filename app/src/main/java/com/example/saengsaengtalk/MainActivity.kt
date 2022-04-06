package com.example.saengsaengtalk

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
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

        refreshView()
    }

    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {

        /* 배달 */
        val baedalList = arrayListOf(
            BaedalPre(LocalDateTime.now(), "네네치킨", 2, 10000),
            BaedalPre(LocalDateTime.now(), "BBQ", 3, 10000),
            BaedalPre(LocalDateTime.now(), "마라탕", 2, 10000),
            BaedalPre(LocalDateTime.now(), "피자", 3, 9000),
            BaedalPre(LocalDateTime.now(), "치킨", 4, 6000)
        )
        binding.rvBaedal.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        //binding.rvBaedal.setHasFixedSize(true)

        if (baedalList.size > 5)
            binding.rvBaedal.adapter = BaedalPreAdapter(baedalList.subList(0,5))
        else
            binding.rvBaedal.adapter = BaedalPreAdapter(baedalList)


        /* 택시 */
        val taxiList = arrayListOf(
            TaxiPre(LocalDateTime.now(), "생자대", "밀양역", 1, 6600),
            TaxiPre(LocalDateTime.now(), "생자대", "밀양역", 2, 6600),
            TaxiPre(LocalDateTime.now(), "밀양역", "생자대", 3, 6600),
            TaxiPre(LocalDateTime.now(), "밀양역", "생자대", 3, 6600),
            TaxiPre(LocalDateTime.now(), "생자대", "영남루", 1, 6600)
        )
        binding.rvTaxi.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        //binding.rvTaxi.setHasFixedSize(true)

        if (taxiList.size > 5)
            binding.rvTaxi.adapter = TaxiPreAdapter(taxiList.subList(0,5))
        else
            binding.rvTaxi.adapter = TaxiPreAdapter(taxiList)


        /* 노래방 */
        val karaList = arrayListOf(
            KaraPre(1, LocalDateTime.now(), LocalDateTime.now().plusMinutes(20), true),
            KaraPre(5, LocalDateTime.now(), LocalDateTime.now().plusMinutes(20), false),
            KaraPre(2, LocalDateTime.now(), LocalDateTime.now().plusMinutes(20), true),
            KaraPre(6, LocalDateTime.now(), LocalDateTime.now().plusMinutes(20), false),
            KaraPre(3, LocalDateTime.now(), LocalDateTime.now().plusMinutes(20), true),
            KaraPre(7, LocalDateTime.now(), LocalDateTime.now().plusMinutes(20), true),
        )
        binding.rvKara.layoutManager = GridLayoutManager(this, 2)
        //binding.rvKara.setHasFixedSize(true)
        binding.rvKara.adapter = KaraPreAdapter(karaList)


        /* 자유게시판 */
        val freeBoardList = arrayListOf(
            FreeBoardPre("자유게시판입니다.", LocalDateTime.now()),
            FreeBoardPre("자유게시판입니다.222", LocalDateTime.now()),
            FreeBoardPre("자유게시판입니다.33333", LocalDateTime.parse("2022-04-04T15:10:00"))
        )
        binding.rvFreeBoard.layoutManager = GridLayoutManager(this, 1)
        //binding.rvFreeBoard.setHasFixedSize(true)
        binding.rvFreeBoard.adapter = FreeBoardPreAdapter(freeBoardList)


        /* 구인게시판 */
        val clubBoardList = arrayListOf(
            ClubBoardPre("구인구직게시판입니다.", LocalDateTime.now()),
            ClubBoardPre("구인게시판입니다.222", LocalDateTime.now()),
            ClubBoardPre("구인게시판입니다.33333", LocalDateTime.parse("2022-04-05T00:00:01")),
            ClubBoardPre("구인게시판입니다.3333344", LocalDateTime.parse("2022-04-04T23:59:59")),
        )
        binding.rvClubBoard.layoutManager = GridLayoutManager(this, 1)
        //binding.rvClubBoard.setHasFixedSize(true)
        binding.rvClubBoard.adapter = ClubBoardPreAdapter(clubBoardList)

    }
}