package com.example.saengsaengtalk

import APIS
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
import com.example.saengsaengtalk.APIS.*
import com.example.saengsaengtalk.APIS.DataModels.TaxiPostPreviewModel
import com.example.saengsaengtalk.adapterHome.*
import com.example.saengsaengtalk.databinding.FragHomeBinding
import com.example.saengsaengtalk.fragmentAccount.FragmentAccount
import com.example.saengsaengtalk.fragmentAccount.FragmentLogin
import com.example.saengsaengtalk.fragmentAccount.admin.FragmentAdmin
import com.example.saengsaengtalk.fragmentBaedal.Baedal.FragmentBaedal
import com.example.saengsaengtalk.fragmentBaedal.BaedalAdd.FragmentBaedalAdd
import com.example.saengsaengtalk.fragmentBaedal.BaedalPost.FragmentBaedalPost
import com.example.saengsaengtalk.fragmentFreeBoard.FragmentFreeBoard
import com.example.saengsaengtalk.fragmentKara.FragmentKara
import com.example.saengsaengtalk.fragmentTaxi.FragmentTaxi
import com.example.saengsaengtalk.fragmentTaxi.FragmentTaxiAdd
import com.example.saengsaengtalk.fragmentTaxi.FragmentTaxiPost
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime


class FragmentHome :Fragment() {

    private var mBinding: FragHomeBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()
    val baeminApi = BaeminAPIS.create()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragHomeBinding.inflate(inflater, container, false)

        refreshView()

        binding.lytHomeBaedallist.setOnClickListener { setFrag(FragmentBaedal(), fragIndex=1) }
        binding.lytHomeTaxilist.setOnClickListener { setFrag(FragmentTaxi(), fragIndex=2) }
        binding.lytHomeKaralist.setOnClickListener { setFrag(FragmentKara(), fragIndex=3) }
        binding.lytHomeFreeboard.setOnClickListener { setFrag(FragmentFreeBoard(), fragIndex=4) }
        //binding.lytHomeClubboard.setOnClickListener { setFrag(FragmentClubBoard()) }

        return binding.root
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnOption.setOnClickListener {
            //var auth = MainActivity.prefs.getString("Authentication", "")
            val authDebug = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTY2Mzk4OTg4OTgyNiwibmlja19uYW1lIjoiYm9uZyJ9.FULK5UjhV7UnoRa8lUP7MrW0wccROJf9GUp7bac1tvo"
            val auth = MainActivity.prefs.getString("Authentication", authDebug)
            Log.d("계정 버튼", auth)
            //setFrag(FragmentLogin(), fragIndex=-1)
            if (auth == "" || auth == "null")
                setFrag(FragmentLogin(), fragIndex=-1)
            else
                setFrag(FragmentAdmin(), fragIndex=-1)    // setFrag(FragmentAccount(), fragIndex=-1)

        }

        /** api test */
        //binding.lytApiTest.visibility = View.GONE

        binding.btnRemoveCache.setOnClickListener {
            MainActivity.prefs.removeString("Authentication")

            var auth = MainActivity.prefs.getString("Authentication", "")
            Log.d("캐시삭제", auth)
        }

        binding.btnTest1.setOnClickListener {
            /*baeminApi.getMenuDetail(10087212, 12476325).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    val detail = response.body()!!
                    Log.d("log", response.toString())
                    Log.d("log", detail.toString())

                    val b = JSONObject(detail.toString())
                    println(b)
                    println(b.getString("status"))
                    println(b.getString("message"))
                    println(b.getJSONObject("data"))

                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    // 실패
                    Log.d("log",t.message.toString())
                    Log.d("log","fail")
                }
            })*/
        }

        binding.btnTest2.setOnClickListener {
            val a = "[" +
                    "{\"optionId\": 1000559268, \"name\": \"zz\",\"discountPrice\": 0,\"price\": 24900,\"soldOut\": false}," +
                    "{\"optionId\": 1000559269, \"name\": \"xx\",\"discountPrice\": 0,\"price\": 25900,\"soldOut\": false}," +
                    "{\"optionId\": 1000559280, \"name\": \"cc\",\"discountPrice\": 0,\"price\": 26900,\"soldOut\": false}," +
                    "]"
            val b = JSONArray(a)
            println(b)
            println(b.getJSONObject(0))
            println(b.getJSONObject(0).getString("name"))
        }


        /** 배달 */
        binding.btnBaedalAdd.setOnClickListener { setFrag(FragmentBaedalAdd(), fragIndex=1) }
        getBaedalPostPreview()

        /* 택시 */
        binding.btnTaxiAdd.setOnClickListener { setFrag(FragmentTaxiAdd(), fragIndex=2) }
        getTaxiPostPreview()

        /* 노래방 */
        val karaList = arrayListOf(
            KaraPre(1, LocalDateTime.now(), LocalDateTime.now().plusMinutes(20), true),
            KaraPre(5, LocalDateTime.now(), LocalDateTime.now().plusMinutes(20), false),
            KaraPre(2, LocalDateTime.now(), LocalDateTime.now().plusMinutes(20), true),
            KaraPre(6, LocalDateTime.now(), LocalDateTime.now().plusMinutes(20), false),
            KaraPre(3, LocalDateTime.now(), LocalDateTime.now().plusMinutes(20), true),
            KaraPre(7, LocalDateTime.now(), LocalDateTime.now().plusMinutes(20), true),
            KaraPre(4, LocalDateTime.now(), LocalDateTime.now().plusMinutes(20), false)
        )
        binding.rvKara.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvKara.setHasFixedSize(true)
        binding.rvKara.adapter = KaraPreAdapter(karaList)


        /* 자유게시판 */
        val freeBoardList = arrayListOf(
            BoardPre("자유게시판입니다.", LocalDateTime.now()),
            BoardPre("자유게시판입니다.222", LocalDateTime.now()),
            BoardPre("자유게시판입니다.33333", LocalDateTime.parse("2022-04-04T15:10:00"))
        )
        binding.rvFreeBoard.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvFreeBoard.setHasFixedSize(true)

        val freeBoardAdapter = BoardPreAdapter(freeBoardList)
        binding.rvFreeBoard.adapter = freeBoardAdapter

        /*freeBoardAdapter.setItemClickListener(object: BoardPreAdapter.OnItemClickListener{
            override fun onClick(position: Int) {
                Log.d("홈프래그먼트 온클릭", "${baedalList[position].postId}")
                setFrag(FragmentFreeBoardPost(), mapOf("postNum" to baedalList[position].postId.toString()), fragIndex=4)
            }
        })*/


        /* 구인게시판 */
        binding.lytHomeClubboard.setVisibility(View.GONE)
        /*val clubBoardList = arrayListOf(
            ClubBoardPre("구인구직게시판입니다.", LocalDateTime.now()),
            ClubBoardPre("구인게시판입니다.222", LocalDateTime.now()),
            ClubBoardPre("구인게시판입니다.33333", LocalDateTime.parse("2022-04-05T00:00:01")),
            ClubBoardPre("구인게시판입니다.3333344", LocalDateTime.parse("2022-04-04T23:59:59")),
        )
        binding.rvClubBoard.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvClubBoard.setHasFixedSize(true)
        binding.rvClubBoard.adapter = ClubBoardPreAdapter(clubBoardList)*/

    }

    fun getBaedalPostPreview() {
        val loopingDialog = looping()
        api.getBaedalOrderListPreview().enqueue(object : Callback<List<BaedalPostPreviewModel>> {
            override fun onResponse(call: Call<List<BaedalPostPreviewModel>>, response: Response<List<BaedalPostPreviewModel>>) {
                val baedalPosts = response.body()!!
                mappingBaedalAdapter(baedalPosts)
                Log.d("log", response.toString())
                Log.d("log", baedalPosts.toString())
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<List<BaedalPostPreviewModel>>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
                looping(false, loopingDialog)
            }
        })
    }

    fun mappingBaedalAdapter(baedalPosts: List<BaedalPostPreviewModel>) {
        binding.rvBaedal.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvBaedal.setHasFixedSize(true)
        val baedalAdapter = BaedalPreAdapter(baedalPosts)
        binding.rvBaedal.adapter = baedalAdapter

        baedalAdapter.setItemClickListener(object: BaedalPreAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                //Toast.makeText(v.context, "${baedalList[position].postId}번", Toast.LENGTH_SHORT).show()
                Log.d("홈프래그먼트 온클릭", "${baedalPosts[position]._id}")
                setFrag(
                    FragmentBaedalPost(),
                    mapOf("postId" to baedalPosts[position]._id),
                    fragIndex = 1
                )
            }
        })
    }

    fun getTaxiPostPreview() {
        val loopingDialog = looping()
        api.getTaxiPostListPreview().enqueue(object : Callback<List<TaxiPostPreviewModel>> {
            override fun onResponse(call: Call<List<TaxiPostPreviewModel>>, response: Response<List<TaxiPostPreviewModel>>) {
                val taxiPosts = response.body()!!
                mappingTaxiAdapter(taxiPosts)
                Log.d("log", response.toString())
                Log.d("log", taxiPosts.toString())
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<List<TaxiPostPreviewModel>>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
                looping(false, loopingDialog)
            }
        })
    }

    fun mappingTaxiAdapter(taxiPosts: List<TaxiPostPreviewModel>) {
        binding.rvTaxi.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTaxi.setHasFixedSize(true)
        val taxiAdapter = TaxiPreAdapter(taxiPosts)
        binding.rvTaxi.adapter = taxiAdapter

        taxiAdapter.setItemClickListener(object: TaxiPreAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                //Toast.makeText(v.context, "${baedalList[position].postId}번", Toast.LENGTH_SHORT).show()
                Log.d("홈프래그먼트 온클릭", "${taxiPosts[position]._id}")
                setFrag(
                    FragmentTaxiPost(),
                    mapOf("postId" to taxiPosts[position]._id),
                    fragIndex = 2
                )
            }
        })
    }


    fun looping(loopStart: Boolean = true, loopingDialog: LoopingDialog? = null): LoopingDialog? {
        val mActivity = activity as MainActivity
        return mActivity.looping(loopStart, loopingDialog)
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, fragIndex: Int) {
        println("setIndex = ${fragIndex}")
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, fragIndex=fragIndex)
    }
}