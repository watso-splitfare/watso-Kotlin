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
import com.example.saengsaengtalk.adapterHome.*
import com.example.saengsaengtalk.databinding.FragHomeBinding
import com.example.saengsaengtalk.fragmentAccount.FragmentAccount
import com.example.saengsaengtalk.fragmentAccount.FragmentLogin
import com.example.saengsaengtalk.fragmentBaedal.Baedal.FragmentBaedal
import com.example.saengsaengtalk.fragmentBaedal.BaedalAdd.FragmentBaedalAdd
import com.example.saengsaengtalk.fragmentBaedal.BaedalPost.FragmentBaedalPost
import com.example.saengsaengtalk.fragmentFreeBoard.FragmentFreeBoard
import com.example.saengsaengtalk.fragmentKara.FragmentKara
import com.example.saengsaengtalk.fragmentTaxi.FragmentTaxi
import com.example.saengsaengtalk.fragmentTaxi.FragmentTaxiAdd
import com.example.saengsaengtalk.fragmentTaxi.FragmentTaxiPost
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
        binding.lytHomeKaralist.setOnClickListener { makeToast("게시판 준비중입니다.")/*setFrag(FragmentKara(), fragIndex=3)*/ }
        binding.lytHomeFreeboard.setOnClickListener { makeToast("게시판 준비중입니다.")/*setFrag(FragmentFreeBoard(), fragIndex=4)*/ }

        return binding.root
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnOption.setOnClickListener {
            val userId = MainActivity.prefs.getString("userId", "")
            if (userId == "")
                setFrag(FragmentLogin(), fragIndex=-1)
            else
                setFrag(FragmentAccount(), fragIndex=-1)
        }


        /** 배달 */
        binding.btnBaedalAdd.setOnClickListener { setFrag(FragmentBaedalAdd(), fragIndex=1) }
        getBaedalPostPreview()

        /** 택시 */
        binding.btnTaxiAdd.setOnClickListener { setFrag(FragmentTaxiAdd(), fragIndex=2) }
        getTaxiPostPreview()

        /** 노래방 */
        //binding.divBottom.visibility = View.GONE
        binding.lytHomeKaralist.visibility = View.GONE
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


        /** 자유게시판 */
        binding.lytHomeFreeboard.visibility = View.GONE
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

    }

    fun getBaedalPostPreview() {
        val loopingDialog = looping()
        api.getBaedalOrderListPreview().enqueue(object : Callback<List<BaedalPostPreviewModel>> {
            override fun onResponse(call: Call<List<BaedalPostPreviewModel>>, response: Response<List<BaedalPostPreviewModel>>) {
                if (response.code() == 200) {
                    val baedalPosts = response.body()!!.sortedBy { it.order_time }
                    mappingBaedalAdapter(baedalPosts)
                } else {
                    Log.e("home Fragment - getBaedalOrderListPreview", response.toString())
                    makeToast("배달 게시글 리스트를 조회하지 못했습니다.")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<List<BaedalPostPreviewModel>>, t: Throwable) {
                Log.e("home Fragment - getBaedalOrderListPreview", t.message.toString())
                makeToast("배달 게시글 리스트를 조회하지 못했습니다.")
                looping(false, loopingDialog)
            }
        })
    }

    fun mappingBaedalAdapter(baedalPosts: List<BaedalPostPreviewModel>) {
        binding.rvBaedal.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvBaedal.setHasFixedSize(true)
        var baedalAdapter: BaedalPreAdapter
        if (baedalPosts.isEmpty()) {
            val emptyBaedalPosts = listOf<BaedalPostPreviewModel>(
                BaedalPostPreviewModel("-1", listOf<Long>(), Store("0", "0", 0, 0), "0", "0", false))
            baedalAdapter = BaedalPreAdapter(emptyBaedalPosts)
        } else { baedalAdapter = BaedalPreAdapter(baedalPosts) }

        binding.rvBaedal.adapter = baedalAdapter

        baedalAdapter.setItemClickListener(object: BaedalPreAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                if (baedalPosts.isEmpty()) { setFrag(FragmentBaedalAdd(), fragIndex=1) }
                else { setFrag(FragmentBaedalPost(), mapOf("postId" to baedalPosts[position]._id), fragIndex = 1) }
            }
        })
    }

    fun getTaxiPostPreview() {
        val loopingDialog = looping()
        api.getTaxiPostListPreview().enqueue(object : Callback<List<TaxiPostPreviewModel>> {
            override fun onResponse(call: Call<List<TaxiPostPreviewModel>>, response: Response<List<TaxiPostPreviewModel>>) {
                if (response.code() == 200) {
                    val taxiPosts = response.body()!!.sortedBy { it.depart_time }
                    mappingTaxiAdapter(taxiPosts)
                } else {
                    Log.e("home Fragment - getTaxiPostListPreview", response.toString())
                    makeToast("택시 게시글 리스트를 조회하지 못했습니다.")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<List<TaxiPostPreviewModel>>, t: Throwable) {
                Log.e("home Fragment - getTaxiPostListPreview", t.message.toString())
                makeToast("택시 게시글 리스트를 조회하지 못했습니다.")
                looping(false, loopingDialog)
            }
        })
    }

    fun mappingTaxiAdapter(taxiPosts: List<TaxiPostPreviewModel>) {
        binding.rvTaxi.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTaxi.setHasFixedSize(true)
        var taxiAdapter: TaxiPreAdapter
        if (taxiPosts.isEmpty()) {
            val emptyTaxiPosts = listOf<TaxiPostPreviewModel>(
            TaxiPostPreviewModel("-1", "0", "0", "0", listOf<Long>()))
            taxiAdapter = TaxiPreAdapter(emptyTaxiPosts)
        } else { taxiAdapter = TaxiPreAdapter(taxiPosts) }

        binding.rvTaxi.adapter = taxiAdapter

        taxiAdapter.setItemClickListener(object: TaxiPreAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                //Toast.makeText(v.context, "${baedalList[position].postId}번", Toast.LENGTH_SHORT).show()
                //Log.d("홈프래그먼트 온클릭", "${taxiPosts[position]._id}")
                if (taxiPosts.isEmpty()) { setFrag(FragmentTaxiAdd(), fragIndex=2) }
                else { setFrag(FragmentTaxiPost(), mapOf("postId" to taxiPosts[position]._id), fragIndex = 2) }
            }
        })
    }

    fun looping(loopStart: Boolean = true, loopingDialog: LoopingDialog? = null): LoopingDialog? {
        val mActivity = activity as MainActivity
        return mActivity.looping(loopStart, loopingDialog)
    }

    fun makeToast(message: String){
        val mActivity = activity as MainActivity
        mActivity.makeToast(message)
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, fragIndex: Int) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, fragIndex=fragIndex)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}