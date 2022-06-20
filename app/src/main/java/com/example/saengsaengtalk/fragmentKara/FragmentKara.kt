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
import org.json.JSONArray
import org.json.JSONObject
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
        val karaRoom = mutableListOf(
            KaraRoom(1, true, "사용하기"),
            KaraRoom(5, false, "15:10~15:30"),
            KaraRoom(2, true, "사용하기"),
            KaraRoom(6, false, "15:05~15:15"),
            KaraRoom(3, false, "15:03~15:13"),
            KaraRoom(7, true, "사용하기"),
            KaraRoom(4, true, "사용하기")
        )

        val roomAdapter = KaraRoomAdapter(karaRoom)
        binding.rvKaraRoom.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvKaraRoom.adapter = roomAdapter


        roomAdapter.setItemClickListener(object: KaraRoomAdapter.OnItemClickListener{
            override fun onClick(position: Int) {
                Log.d("노래방", "${karaRoom[position].num}")
                setFrag(FragmentKaraUse(), mapOf(
                    "num" to karaRoom[position].num.toString(),
                    "use" to karaRoom[position].use))
            }
        })


        /*노래방 게시판*/
        val boardPre = mutableListOf<BoardPre>()

        boardPre.add(BoardPre("노래방게시판입니다.", LocalDateTime.now()))
        boardPre.add(BoardPre("노래방게시판입니다.222", LocalDateTime.now()))
        boardPre.add(BoardPre("노래방게시판입니다.33333", LocalDateTime.parse("2022-04-04T15:10:00")))

        val boardAdapter = BoardPreAdapter(boardPre)
        binding.rvKaraBoard.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvKaraBoard.adapter = boardAdapter

        boardAdapter.setItemClickListener(object: BoardPreAdapter.OnItemClickListener{
            override fun onClick(position: Int) {
                Log.d("노래방", "${karaRoom[position].num}")
                setFrag(FragmentFreeBoardPost(), mapOf("Num" to karaRoom[position].num.toString()))
            }
        })
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }
}