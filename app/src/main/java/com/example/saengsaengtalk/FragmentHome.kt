package com.example.saengsaengtalk

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.adapterHome.*
import com.example.saengsaengtalk.databinding.FragHomeBinding
import java.time.LocalDateTime

class FragmentHome :Fragment() {

    private var mBinding: FragHomeBinding? = null
    private val binding get() = mBinding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //val view = inflater.inflate(R.layout.frag_home, container, false)
        mBinding = FragHomeBinding.inflate(inflater, container, false)


        refreshView()

        return binding.root
        //return view
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
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
        binding.rvBaedal.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvBaedal.setHasFixedSize(true)

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
        binding.rvTaxi.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTaxi.setHasFixedSize(true)

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
        binding.rvKara.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvKara.setHasFixedSize(true)
        binding.rvKara.adapter = KaraPreAdapter(karaList)


        /* 자유게시판 */
        val freeBoardList = arrayListOf(
            FreeBoardPre("자유게시판입니다.", LocalDateTime.now()),
            FreeBoardPre("자유게시판입니다.222", LocalDateTime.now()),
            FreeBoardPre("자유게시판입니다.33333", LocalDateTime.parse("2022-04-04T15:10:00"))
        )
        binding.rvFreeBoard.layoutManager = GridLayoutManager(requireContext(), 1)
        binding.rvFreeBoard.setHasFixedSize(true)
        binding.rvFreeBoard.adapter = FreeBoardPreAdapter(freeBoardList)


        /* 구인게시판 */
        val clubBoardList = arrayListOf(
            ClubBoardPre("구인구직게시판입니다.", LocalDateTime.now()),
            ClubBoardPre("구인게시판입니다.222", LocalDateTime.now()),
            ClubBoardPre("구인게시판입니다.33333", LocalDateTime.parse("2022-04-05T00:00:01")),
            ClubBoardPre("구인게시판입니다.3333344", LocalDateTime.parse("2022-04-04T23:59:59")),
        )
        binding.rvClubBoard.layoutManager = GridLayoutManager(requireContext(), 1)
        binding.rvClubBoard.setHasFixedSize(true)
        binding.rvClubBoard.adapter = ClubBoardPreAdapter(clubBoardList)

    }
}