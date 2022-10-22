package com.example.saengsaengtalk

/*import com.example.saengsaengtalk.APIS.PostModel
import com.example.saengsaengtalk.APIS.PostResult
import com.example.saengsaengtalk.APIS.TestModel
import com.example.saengsaengtalk.APIS.TestResult*/

import APIS
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.APIS.GroupOptionModel
import com.example.saengsaengtalk.APIS.SectionMenuModel
import com.example.saengsaengtalk.APIS.StoreListModel
import com.example.saengsaengtalk.APIS.TestModel
import com.example.saengsaengtalk.adapterHome.*
import com.example.saengsaengtalk.databinding.FragHomeBinding
import com.example.saengsaengtalk.fragmentAccount.FragmentLogin
import com.example.saengsaengtalk.fragmentBaedal.Baedal.FragmentBaedal
import com.example.saengsaengtalk.fragmentBaedal.BaedalAdd.FragmentBaedalAdd
import com.example.saengsaengtalk.fragmentBaedal.BaedalPost.FragmentBaedalPost
import com.example.saengsaengtalk.fragmentFreeBoard.FragmentFreeBoard
import com.example.saengsaengtalk.fragmentFreeBoard.FragmentFreeBoardPost
import com.example.saengsaengtalk.fragmentKara.FragmentKara
import com.example.saengsaengtalk.fragmentTaxi.FragmentTaxi
import com.example.saengsaengtalk.fragmentTaxi.FragmentTaxiAdd
import com.example.saengsaengtalk.fragmentTaxi.FragmentTaxiPost
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime


class FragmentHome :Fragment() {

    private var mBinding: FragHomeBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()

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
            var auth = MainActivity.prefs.getString("Authentication", "")
            Log.d("계정 버튼", auth)
            setFrag(FragmentLogin(), fragIndex=-1)
            /*if (auth == "" || auth == "null")
                setFrag(FragmentLogin(), fragIndex=-1)
            else
                setFrag(FragmentAccount(), fragIndex=-1)*/
        }

        /** api test */
        //binding.lytApiTest.visibility = View.GONE

        binding.btnRemoveCache.setOnClickListener {
            MainActivity.prefs.removeString("Authentication")

            var auth = MainActivity.prefs.getString("Authentication", "")
            Log.d("캐시삭제", auth)
        }

        /*binding.btnTest1.setOnClickListener {
            /*val data = PostModel(binding.etTest1.text.toString(),binding.etTest2.text.toString()
                ,binding.etTest3.text.toString(),binding.etTest4.text.toString(),binding.etTest5.text.toString())*/
            GlobalScope.launch() {
                api.getStoreList().enqueue(object : Callback<List<StoreListModel>> {
                    override fun onResponse(
                        call: Call<List<StoreListModel>>,
                        response: Response<List<StoreListModel>>
                    ) {
                        Log.d("log", response.toString())
                        Log.d("log", response.body().toString())
                        if (!response.body().toString().isEmpty())
                            binding.tvTest.setText(response.body().toString());
                    }

                    override fun onFailure(call: Call<List<StoreListModel>>, t: Throwable) {
                        // 실패
                        Log.d("log", t.message.toString())
                        Log.d("log", "fail")
                    }
                })
            }
        }*/

        binding.btnTest2.setOnClickListener {
            //val data = binding.etTest2.text.toString().toInt()
            api.getTest().enqueue(object : Callback<TestModel> {
                override fun onResponse(call: Call<TestModel>, response: Response<TestModel>) {
                    Log.d("log",response.toString())
                    Log.d("log", response.body().toString())
                }

                override fun onFailure(call: Call<TestModel>, t: Throwable) {
                    // 실패
                    Log.d("log",t.message.toString())
                    Log.d("log","fail")
                }
            })
        }

        /*binding.btnTest3.setOnClickListener {
            val data = binding.etTest3.text.toString().toInt()
            api.getGroupOption(data).enqueue(object : Callback<List<GroupOptionModel>> {
                override fun onResponse(call: Call<List<GroupOptionModel>>, response: Response<List<GroupOptionModel>>) {
                    Log.d("log",response.toString())
                    Log.d("log", response.body().toString())
                    if(!response.body().toString().isEmpty())
                        binding.tvTest.setText(response.body().toString());
                }

                override fun onFailure(call: Call<List<GroupOptionModel>>, t: Throwable) {
                    // 실패
                    Log.d("log",t.message.toString())
                    Log.d("log","fail")
                }
            })
        }*/

        /*binding.btnTest1.setOnClickListener {
            val data = TestModel("테스트 바디")
            api.test("gdfdgd").enqueue(object : Callback<TestResult> {
                override fun onResponse(call: Call<TestResult>, response: Response<TestResult>) {
                    Log.d("log",response.toString())
                    Log.d("log", response.body().toString())
                    if(!response.body().toString().isEmpty())
                        binding.tvTest.setText(response.body().toString());
                }

                override fun onFailure(call: Call<TestResult>, t: Throwable) {
                    // 실패
                    Log.d("log",t.message.toString())
                    Log.d("log","fail")
                }
            })
        }*/

        /*binding.btnTest1.setOnClickListener {
            val data = TestModel("테스트 바디")
            api.test(data).enqueue(object: Callback<TestResult> {
                override fun onResponse(call: Call<TestResult>, response: Response<TestResult>) {
                    Log.d("log", response.toString())
                    Log.d("log", response.body().toString())
                }
            })
        }*/

        /** */

        /* 배달 */
        binding.btnBaedalAdd.setOnClickListener { setFrag(FragmentBaedalAdd(), fragIndex=1) }

        val baedalList = arrayListOf(
            BaedalPre(LocalDateTime.now(), "네네치킨", 2, 10000, 0),
            BaedalPre(LocalDateTime.now(), "BBQ", 3, 10000, 0),
            BaedalPre(LocalDateTime.now(), "마라탕", 2, 10000, 0),
            BaedalPre(LocalDateTime.now(), "피자", 3, 9000, 0),
            BaedalPre(LocalDateTime.now(), "치킨", 4, 6000, 0)
        )

        binding.rvBaedal.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvBaedal.setHasFixedSize(true)
        val baedalAdapter = BaedalPreAdapter(baedalList)
        binding.rvBaedal.adapter = baedalAdapter

        baedalAdapter.setItemClickListener(object: BaedalPreAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                Toast.makeText(v.context, "${baedalList[position].postId}번", Toast.LENGTH_SHORT).show()
                Log.d("홈프래그먼트 온클릭", "${baedalList[position].postId}")
                setFrag(FragmentBaedalPost(), mapOf("postNum" to baedalList[position].postId.toString()), fragIndex=1)
            }
        })
        //baedalAdapter.notifyDataSetChanged()


        /* 택시 */
        binding.btnTaxiAdd.setOnClickListener { setFrag(FragmentTaxiAdd(), fragIndex=1) }
        val taxiList = arrayListOf(
            TaxiPre(LocalDateTime.now(), "생자대", "밀양역", 1, 6600, 1),
            TaxiPre(LocalDateTime.now(), "생자대", "밀양역", 2, 6600, 2),
            TaxiPre(LocalDateTime.now(), "밀양역", "생자대", 3, 6600, 3),
            TaxiPre(LocalDateTime.now(), "밀양역", "생자대", 3, 6600, 4),
            TaxiPre(LocalDateTime.now(), "생자대", "영남루", 1, 6600, 5)
        )
        val taxiAdapter = TaxiPreAdapter(taxiList)
        binding.rvTaxi.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTaxi.setHasFixedSize(true)
        binding.rvTaxi.adapter = taxiAdapter

        taxiAdapter.setItemClickListener(object: TaxiPreAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                Toast.makeText(v.context, "${taxiList[position].postNum}번", Toast.LENGTH_SHORT).show()
                Log.d("홈프래그먼트 택시 온클릭", "${taxiList[position].postNum}")
                setFrag(FragmentTaxiPost(), mapOf("postNum" to taxiList[position].postNum.toString()), fragIndex=2)
            }
        })
        taxiAdapter.notifyDataSetChanged()


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

        freeBoardAdapter.setItemClickListener(object: BoardPreAdapter.OnItemClickListener{
            override fun onClick(position: Int) {
                Log.d("홈프래그먼트 온클릭", "${baedalList[position].postId}")
                setFrag(FragmentFreeBoardPost(), mapOf("postNum" to baedalList[position].postId.toString()), fragIndex=4)
            }
        })


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

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, fragIndex: Int) {
        println("setIndex = ${fragIndex}")
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, fragIndex=fragIndex)
    }
}