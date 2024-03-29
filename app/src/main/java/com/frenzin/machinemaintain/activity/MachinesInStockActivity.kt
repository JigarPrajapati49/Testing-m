package com.frenzin.machinemaintain.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.base.BaseActivity
import com.frenzin.machinemaintain.adapter.MachinesStockAdapter
import com.frenzin.machinemaintain.model.resModel.MachinesStockDetail
import com.frenzin.machinemaintain.databinding.ActivityMachinesInStockBinding
import com.frenzin.machinemaintain.model.resModel.GetMachineDetail
import com.frenzin.machinemaintain.model.resModel.GetMachineDetailList
import com.frenzin.machinemaintain.utills.PreferenceKeys
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MachinesInStockActivity : BaseActivity(),MachinesStockAdapter.onItemClicked{

    private lateinit var binding : ActivityMachinesInStockBinding
    private lateinit var lm : LinearLayoutManager
    private lateinit var machinesStockAdapter: MachinesStockAdapter
    private var machinesDetailList = ArrayList<GetMachineDetailList>()
    private var TAG = "MachinesInStockActivity"
    private var page = 1
    private var isLoading: Boolean = false
    private var isLastPage: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMachinesInStockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setAdapter()
        setMachinesData()
        recycleViewScrollingHandle()

        binding.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setAdapter(){
        lm = LinearLayoutManager(this)
        binding.rvMachinesStock.layoutManager = lm
        machinesStockAdapter = MachinesStockAdapter(machinesDetailList,this,this)
        binding.rvMachinesStock.adapter = machinesStockAdapter
    }

    private fun setMachinesData(){
        getMachineDetailApiCall(page)
    }

    private fun recycleViewScrollingHandle(){
        binding.rvMachinesStock.addOnScrollListener(object : RecyclerView.OnScrollListener() {

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
                        if (page == 10) {
                            isLastPage = true
                        } else {
                            page++
                            Log.e(TAG, " page number $page");
                            isLoading = true

                            showLoading("Loading...")
                            getMachineDetailApiCall(page)
                        }
                    }
                }
            }
        })
    }

    private fun getMachineDetailApiCall(page : Int){
        if (isOnline()) {
            showLoading("Please wait...")
            val addMachineDetailApiCall = apiService?.getMachineDetail(page)

            addMachineDetailApiCall?.enqueue(object : Callback<GetMachineDetail?> {
                override fun onResponse(call: Call<GetMachineDetail?>, response: Response<GetMachineDetail?>) {
                    Log.e(TAG, "getMachineDetailApiCall onResponse : BODY " + Gson().toJson(response.body()))
                    Log.e(TAG, "getMachineDetailApiCall onResponse : CODE " + Gson().toJson(response.code()))
                    Log.e(TAG, "getMachineDetailApiCall onResponse : MESSAGE " + Gson().toJson(response.message()))
                    Log.e(TAG, "getMachineDetailApiCall onResponse : ERROR_BODY " + Gson().toJson(response.errorBody()))
                    hideLoading()
                    if (response.body() != null && response.code() == 200) {
                        machinesDetailList.addAll(response.body()!!.data!!.data)
                        machinesStockAdapter.notifyDataSetChanged()
                        isLoading = false
                        Log.e(TAG, "getMachineDetailApiCall onResponse Successfully")
                        Log.e(TAG, "getMachineDetailApiCall onResponse machinesDetailList $machinesDetailList")
                    } else {
                        hideLoading()
                        Log.e(TAG, "getMachineDetailApiCall onResponse Else Part")
                    }
                }

                override fun onFailure(call: Call<GetMachineDetail?>, t: Throwable) {
                    t.printStackTrace()
                    hideLoading()
                    Log.e(TAG, "getMachineDetailApiCall onFailure: ${t.message}")
                    showSnackBar(binding.root, t.message.toString())
                }
            })
        } else {
            showSnackBar(binding.root,getString(R.string.msg_no_network))
        }
    }

    override fun itemClicked(position: Int,machineId : String) {
        Bundle().let {
            Log.e(TAG, "MachinesInStockActivity itemClicked: $machineId", )
            it.putString(PreferenceKeys.MACHINE_ID_BY_SERIAL_NUMBER,machineId)
            gotoActivity(BookActivity::class.java,it,false)
        }
    }
}