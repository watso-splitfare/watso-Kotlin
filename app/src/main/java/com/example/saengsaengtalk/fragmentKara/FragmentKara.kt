package com.example.saengsaengtalk.fragmentKara

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.adapterHome.BoardPre
import com.example.saengsaengtalk.adapterHome.BoardPreAdapter
import com.example.saengsaengtalk.databinding.FragKaraBinding
import com.example.saengsaengtalk.fragmentFreeBoard.FragmentFreeBoardPost
import com.example.saengsaengtalk.fragmentKara.adapter.KaraRoom
import com.example.saengsaengtalk.fragmentKara.adapter.KaraRoomAdapter
import java.time.LocalDateTime

class FragmentKara :Fragment() {

    private var mBinding: FragKaraBinding? = null
    private val binding get() = mBinding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragKaraBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        /*방 리스트*/
        val karaRoom = mutableListOf<KaraRoom>()
        karaRoom.add(KaraRoom(1, true, "사용하기"))
        karaRoom.add(KaraRoom(5, false, "15:10~15:30"))
        karaRoom.add(KaraRoom(2, true, "사용하기"))
        karaRoom.add(KaraRoom(6, false, "15:05~15:15"))
        karaRoom.add(KaraRoom(3, false, "15:03~15:13"))
        karaRoom.add(KaraRoom(7, true, "사용하기"))
        karaRoom.add(KaraRoom(4, true, "사용하기"))


        val adapter = KaraRoomAdapter(karaRoom)
        binding.rvKaraRoom.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvKaraRoom.adapter = adapter

        adapter.setItemClickListener(object: KaraRoomAdapter.OnItemClickListener{
            override fun onClick(position: Int) {
                Log.d("노래방", "${karaRoom[position].Num}")
                setFrag(FragmentFreeBoardPost(), mapOf("Num" to karaRoom[position].Num.toString()))
            }
        })

        /*노래방 게시판*/
        val boardPre = mutableListOf<BoardPre>()

        boardPre.add(BoardPre("노래방게시판입니다.", LocalDateTime.now()))
        boardPre.add(BoardPre("노래방게시판입니다.222", LocalDateTime.now()))
        boardPre.add(BoardPre("노래방게시판입니다.33333", LocalDateTime.parse("2022-04-04T15:10:00")))


        binding.rvKaraBoard.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvKaraBoard.setHasFixedSize(true)
        binding.rvKaraBoard.adapter = BoardPreAdapter(boardPre)
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }
}