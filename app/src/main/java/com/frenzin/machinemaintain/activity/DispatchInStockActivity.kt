package com.frenzin.machinemaintain.activity

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.adapter.DispatchStockAdapter
import com.frenzin.machinemaintain.base.BaseActivity
import com.frenzin.machinemaintain.databinding.ActivityDispatchInStockBinding
import com.frenzin.machinemaintain.model.resModel.GetMachineBookDetails
import com.frenzin.machinemaintain.model.resModel.MachineBookDetails
import com.frenzin.machinemaintain.utills.Constant
import com.frenzin.machinemaintain.utills.PreferenceKeys
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DispatchInStockActivity : BaseActivity(), DispatchStockAdapter.onItemClicked {

    private lateinit var binding: ActivityDispatchInStockBinding
    private lateinit var dispatchStockAdapter: DispatchStockAdapter
    private lateinit var lm: LinearLayoutManager
    private var TAG = "DispatchInStockActivity"
    private var dispatchDetailList = ArrayList<MachineBookDetails>()
    private var page = 1
    private var isLoading: Boolean = false
    private var isLastPage: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDispatchInStockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setAdapter()
        setMachinesData()
        recycleViewScrollingHandle()

        binding.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setAdapter() {
        lm = LinearLayoutManager(this)
        binding.rvDispatchInStock.layoutManager = lm
        dispatchStockAdapter = DispatchStockAdapter(dispatchDetailList, this, this)
        binding.rvDispatchInStock.adapter = dispatchStockAdapter
    }

    private fun setMachinesData() {
        getMachineBookDetailsApiCall(page)
    }

    private fun recycleViewScrollingHandle() {
        binding.rvDispatchInStock.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = recyclerView.layoutManager!!.childCount
                val totalItemCount: Int = lm.itemCount
                val firstVisibleItemPosition: Int = lm.findFirstVisibleItemPosition()

                Log.e(TAG, "onScrolled: visibleItemCount is  ${visibleItemCount}")
                Log.e(TAG, "onScrolled: totalItemCount is  ${totalItemCount}")
                Log.e(TAG, "onScrolled: firstVisibleItemPosition is  ${firstVisibleItemPosition}")

                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {

                    if (!isLoading && !isLastPage) {

                        page++
                        Log.e(TAG, " page number $page");
                        isLoading = true

                        showLoading("Loading...")
                        getMachineBookDetailsApiCall(page)

                        if (page >= 10) {
                            isLastPage = true
                        }
                    }
                }
            }
        })
    }

    private fun getMachineBookDetailsApiCall(page: Int) {
        if (isOnline()) {
            showLoading("Please wait...")
            val addMachineDetailApiCall = apiService?.getMachineBookDetails(page)

            addMachineDetailApiCall?.enqueue(object : Callback<GetMachineBookDetails?> {
                override fun onResponse(
                    call: Call<GetMachineBookDetails?>,
                    response: Response<GetMachineBookDetails?>
                ) {
                    Log.e(TAG, "getMachineBookDetailsApiCall onResponse : BODY " + Gson().toJson(response.body()))
                    Log.e(TAG, "getMachineBookDetailsApiCall onResponse : CODE " + Gson().toJson(response.code()))
                    Log.e(TAG, "getMachineBookDetailsApiCall onResponse : MESSAGE " + Gson().toJson(response.message()))
                    Log.e(TAG, "getMachineBookDetailsApiCall onResponse : ERROR_BODY " + Gson().toJson(response.errorBody()))
                    hideLoading()
                    if (response.body() != null && response.code() == 200) {
                        dispatchDetailList.addAll(response.body()!!.data!!.data)
                        dispatchStockAdapter.notifyDataSetChanged()
                        isLoading = false
                        Log.e(TAG, "getMachineBookDetailsApiCall onResponse Successfully")
                        Log.e(
                            TAG,
                            "getMachineBookDetailsApiCall onResponse dispatchDetailList $dispatchDetailList"
                        )
                    } else {
                        hideLoading()
                        Log.e(TAG, "getMachineBookDetailsApiCall onResponse Else Part")
                    }
                }

                override fun onFailure(call: Call<GetMachineBookDetails?>, t: Throwable) {
                    t.printStackTrace()
                    hideLoading()
                    Log.e(TAG, "getMachineBookDetailsApiCall onFailure: ${t.message}")
                    showSnackBar(binding.root, t.message.toString())
                }
            })
        } else {
            showSnackBar(binding.root, getString(R.string.msg_no_network))
        }
    }

    override fun itemClicked(position: Int, machineId: String,machineNumber : String,customerName : String) {
        Bundle().let {
            Log.e(TAG, "DispatchInStockActivity itemClicked: $machineId")
            it.putString(PreferenceKeys.MACHINE_ID_BY_SERIAL_NUMBER, machineId)
            it.putString(Constant.MACHINE_NAME, customerName)
            it.putString(Constant.MACHINE_SERIAL_NUMBER,machineNumber)
            gotoActivity(DispatchActivity::class.java, it, false)
        }
    }
}