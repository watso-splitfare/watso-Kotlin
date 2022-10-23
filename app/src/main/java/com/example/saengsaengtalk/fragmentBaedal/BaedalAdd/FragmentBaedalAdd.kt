package com.example.saengsaengtalk.fragmentBaedal.BaedalAdd

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.APIS.*
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.databinding.FragBaedalAddBinding
import com.example.saengsaengtalk.fragmentBaedal.BaedalConfirm.SelectedMenuAdapter
import com.example.saengsaengtalk.fragmentBaedal.BaedalMenu.FragmentBaedalMenu
import com.example.saengsaengtalk.fragmentBaedal.BaedalPost.FragmentBaedalPost
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentBaedalAdd :Fragment() {
    var baedalfee = 0
    var orderPrice = 0
    var orders = JSONArray()


    var stores = listOf<StoreListModel>()
    var storeIds = mutableListOf<String>()
    var storeNames = mutableListOf<String>()
    var storeFees = mutableListOf<Int>()
    var selectedIdx = 0

    private var mBinding: FragBaedalAddBinding? = null
    private val binding get() = mBinding!!
    val api = APIS.create()
    var decDt = DecimalFormat("00")
    val decPrice = DecimalFormat("#,###")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalAddBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }
        val now = LocalDateTime.now()
        binding.tvOrderTime.text = getDateTimeString(now)
        binding.lytTime.setOnClickListener { showCalendar() }
        setMenuSpinner()

        val places = listOf("생자대", "기숙사")
        val placeSpinerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, places)
        binding.spnPlace.adapter = placeSpinerAdapter

        binding.lytChoice.setOnClickListener {
            setFrag(FragmentBaedalMenu(), mapOf(
                "member" to "0",
                "isPosting" to "true",
                "storeName" to storeNames[selectedIdx],
                "storeId" to storeIds[selectedIdx].toString(),
                "baedalFee" to storeFees[selectedIdx].toString(),
                "orders" to orders.toString()
            ))
        }

        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("ConfirmToPosting", this) { requestKey, bundle ->
            orders = JSONArray(bundle.getString("ordersString"))

            binding.rvMenuList.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val adapter = SelectedMenuAdapter(requireContext(), orders, false)
            binding.rvMenuList.adapter = adapter

            orderPrice = 0
            for (i in 0 until orders.length()){
                val order = orders.getJSONObject(i)
                orderPrice += order.getInt("sumPrice") * order.getInt("count")
            }
            setBindText()

            if (orderPrice > 0) {
                binding.btnPostAdd.isEnabled = true
                binding.btnPostAdd.setBackgroundResource(R.drawable.btn_baedal_confirm)
                binding.btnPostAdd.setOnClickListener {
                    if (binding.etTitle.text.toString() == "") {
                        makeToast("제목을 입력해주세요.")
                    } else {
                        val minMember = if (binding.cbMinMember.isChecked)
                            binding.etMinMember.text.toString().toInt() else null
                        val maxMember = if (binding.cbMaxMember.isChecked)
                            binding.etMaxMember.text.toString().toInt() else null

                        val baedalPostModel = BaedalPostingModel(
                            storeIds[selectedIdx],
                            binding.etTitle.text.toString(),
                            binding.etContent.text.toString(),
                            binding.tvOrderTime.text.toString(),
                            binding.spnPlace.selectedItem.toString(),
                            minMember,
                            maxMember
                        )

                        lateinit var postingResponse: BaedalPostingResponse

                        val job = GlobalScope.launch {
                            async {
                                postingResponse = baedalPosting(baedalPostModel)
                                println("바깥: ${postingResponse}")
                            }.await()
                        }
                        runBlocking {
                            job.join()
                        }
                        if (!postingResponse.sucess) {
                            makeToast("게시글을 작성하지 못 했습니다.\n다시 시도해 주세요.")

                        } else {
                            makeToast("작성 성공")
                        }
                        //setFrag(FragmentBaedalPost(), mapOf("postNum" to "0"))
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showCalendar() {
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            run {
                var dateString = "${year}-${decDt.format(month + 1)}-${decDt.format(dayOfMonth)}T"

                val timeSetListener =
                    TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                        run {
                            var timeString = "${decDt.format(hourOfDay)}:${decDt.format(minute)}:00"
                            var orderTime = LocalDateTime.parse(dateString+timeString)
                            binding.tvOrderTime.text = getDateTimeString(orderTime)
                        }
                    }

                TimePickerDialog(requireContext(), timeSetListener,
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
            }
        }

        val dpd = DatePickerDialog(requireContext(), dateSetListener,
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dpd.datePicker.minDate = System.currentTimeMillis() - 1000;
        dpd.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDateTimeString(dateTime: LocalDateTime): String {
        return dateTime.format(DateTimeFormatter.ofPattern(
            "MM/dd(E) HH:mm").withLocale(Locale.forLanguageTag("ko")))
    }

    fun setMenuSpinner(){
        api.getStoreList().enqueue(object : Callback<List<StoreListModel>> {
            override fun onResponse(
                call: Call<List<StoreListModel>>,
                response: Response<List<StoreListModel>>
            ) {
                Log.d("log", response.toString())
                Log.d("log", response.body().toString())
                stores = response.body()!!
                stores.forEach {
                    storeIds.add(it.store_id)
                    storeNames.add(it.store_name)
                    storeFees.add(it.fee)
                }

                val searchmethod =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, storeNames)

                searchmethod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spnStore!!.adapter = searchmethod
                binding.spnStore.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        baedalfee = storeFees[position]
                        setBindText()
                        selectedIdx = position
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) { }
                }
            }

            override fun onFailure(call: Call<List<StoreListModel>>, t: Throwable) {
                // 실패
                Log.d("log", t.message.toString())
                Log.d("log", "fail")
            }
        })
    }

    fun setBindText() {
        binding.tvBaedalStoreFee.text = "${decPrice.format(baedalfee)}원"
        binding.tvBaedalFee.text = "${decPrice.format(baedalfee)}원"
        binding.tvOrderPrice.text = "${decPrice.format(orderPrice)}원"
        binding.tvTotalPrice.text = "${decPrice.format(baedalfee+orderPrice)}원"//"${decPrice.format(totalPrice)}원"
    }

    suspend fun baedalPosting(baedalPostModel: BaedalPostingModel): BaedalPostingResponse {
        lateinit var result: BaedalPostingResponse
        //var flag = false

        runBlocking {
            val channel = Channel<BaedalPostingResponse>()

            api.baedalPosting(baedalPostModel).enqueue(object : Callback<BaedalPostingResponse> {
                override fun onResponse(
                    call: Call<BaedalPostingResponse>,
                    response: Response<BaedalPostingResponse>
                ) {
                    println("성공")
                    Log.d("log", response.toString())
                    Log.d("log", response.body().toString())
                    launch {
                        channel.send(response.body()!!)
                        channel.close()
                    }
                    //result = response.body()!!
                    //flag = true
                }

                override fun onFailure(call: Call<BaedalPostingResponse>, t: Throwable) {
                    println("실패")
                    Log.d("log", t.message.toString())
                    Log.d("log", "fail")
                    launch {
                        channel.send(BaedalPostingResponse(false, "-1"))
                        channel.close()
                    }
                    //BaedalPostingResponse(false, "-1")
                    //flag = true
                }
            })
            result = channel.receive()
        }

        println("안: ${result}")
        return result
    }

    fun baedalOrdering(baedalOrderModel: OrderingModel): OrderingResponse{
        lateinit var result: OrderingResponse
        api.baedalOrdering(baedalOrderModel).enqueue(object : Callback<OrderingResponse> {
            override fun onResponse(call: Call<OrderingResponse>, response: Response<OrderingResponse>) {
                println("성공")
                Log.d("log", response.toString())
                Log.d("log", response.body().toString())
                result = response.body()!!
            }

            override fun onFailure(call: Call<OrderingResponse>, t: Throwable) {
                println("실패")
                Log.d("log", t.message.toString())
                Log.d("log", "fail")
                result = OrderingResponse(false, "-1")
            }
        })
        return result
    }

    fun makeToast(message: String) {
        val mActivity = activity as MainActivity
        mActivity.makeToast(message)
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}