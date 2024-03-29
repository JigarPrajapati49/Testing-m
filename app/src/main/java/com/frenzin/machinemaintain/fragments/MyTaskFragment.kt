package com.frenzin.machinemaintain.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.activity.MyTaskDetailActivity
import com.frenzin.machinemaintain.adapter.MyTaskDetailAdapter
import com.frenzin.machinemaintain.base.BaseActivity.Companion.apiService
import com.frenzin.machinemaintain.base.BaseFragment
import com.frenzin.machinemaintain.databinding.FragmentMyTaskBinding
import com.frenzin.machinemaintain.model.resModel.GetMyTickets
import com.frenzin.machinemaintain.model.resModel.TicketsDetail
import com.frenzin.machinemaintain.utills.Constant
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyTaskFragment : BaseFragment(), MyTaskDetailAdapter.OnItemClickLListener {

    private lateinit var binding: FragmentMyTaskBinding
    private lateinit var myTaskDetailAdapter: MyTaskDetailAdapter
    private lateinit var contextHistoryFragment: Context
    private var myTaskDetailList = ArrayList<TicketsDetail>()
    private var TAG = "HistoryFragment"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contextHistoryFragment = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        getMyTicketsApiCall()

        binding.imgBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
    }

    private fun setAdapter() {
        binding.rvMyTasks.layoutManager = LinearLayoutManager(contextHistoryFragment)
        myTaskDetailAdapter = MyTaskDetailAdapter(myTaskDetailList, this)
        binding.rvMyTasks.adapter = myTaskDetailAdapter
        binding.rvMyTasks.setHasFixedSize(true)
    }

    override fun onItemClicked(position: Int, myTaskList: ArrayList<TicketsDetail>) {
        Bundle().let {
            Log.e(TAG, "HistoryFragment onItemClicked: Ticket ID ${myTaskList[position].id}")
            Log.e(TAG, "HistoryFragment onItemClicked: Status ${myTaskList[position].status}")
            it.putString(Constant.CUSTOMER_NAME, myTaskList[position].customerDetail?.name)
            it.putString(Constant.TICKET_NUMBER, myTaskList[position].ticketNumber.toString())
            it.putString(Constant.MACHINE_NAME, myTaskList[position].machineDetail?.machineName)
            it.putString(Constant.INVERT_DATE, myTaskList[position].machineDetail?.invertDate)
            it.putString(Constant.STATUS, myTaskList[position].status)
            it.putString(Constant.MOBILE_NUMBER, myTaskList[position].customerDetail?.phoneNumber)
            it.putString(Constant.TICKET_ID, myTaskList[position].id.toString())
            it.putString(Constant.CREATED_DATE, myTaskList[position].createdAt.toString())
            gotoActivity(MyTaskDetailActivity::class.java, it, false)
        }
    }

    override fun phoneClicked(position: Int, phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse("tel:$phoneNumber")
        startActivity(callIntent)
    }

    private fun getMyTicketsApiCall() {
        if (isOnline()) {
            showLoading("Please wait...")
            val getLoginUserDetail = apiService?.getMyTickets()

            getLoginUserDetail?.enqueue(object : Callback<GetMyTickets?> {
                override fun onResponse(call: Call<GetMyTickets?>, response: Response<GetMyTickets?>) {
                    Log.e(TAG, "getMyTicketsApiCall onResponse : BODY " + Gson().toJson(response.body()))
                    Log.e(TAG, "getMyTicketsApiCall onResponse : CODE " + Gson().toJson(response.code()))
                    Log.e(TAG, "getMyTicketsApiCall onResponse : MESSAGE " + Gson().toJson(response.message()))
                    Log.e(TAG, "getMyTicketsApiCall onResponse : ERROR_BODY " + Gson().toJson(response.errorBody()))
                    hideLoading()
                    if (response.body() != null && response.code() == 200) {
                        Log.e(TAG, "getMyTicketsApiCall onResponse Successfully")
                        myTaskDetailList.addAll(response.body()!!.data)
                        myTaskDetailAdapter.notifyDataSetChanged()
                    } else {
                        hideLoading()
                        Log.e(TAG, "getMyTicketsApiCall onResponse Else Part")
                    }
                }

                override fun onFailure(call: Call<GetMyTickets?>, t: Throwable) {
                    t.printStackTrace()
                    hideLoading()
                    Log.e(TAG, "getMyTicketsApiCall onFailure: ${t.message}")
                    showSnackBar(binding.root, t.message.toString())
                }
            })

        } else {
            showSnackBar(binding.root, getString(R.string.msg_no_network))
        }
    }

    override fun onResume() {
        super.onResume()
        getMyTicketsApiCall()
    }

}