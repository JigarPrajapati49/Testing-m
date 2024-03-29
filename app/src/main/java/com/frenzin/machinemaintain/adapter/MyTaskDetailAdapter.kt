package com.frenzin.machinemaintain.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.frenzin.machinemaintain.databinding.AdapterMyTaskRowFileBinding
import com.frenzin.machinemaintain.model.resModel.GetMyTickets
import com.frenzin.machinemaintain.model.resModel.MyTask
import com.frenzin.machinemaintain.model.resModel.TicketsDetail

class MyTaskDetailAdapter(
    private var myTaskDetailList: ArrayList<TicketsDetail>, var onItemListener : OnItemClickLListener
) : RecyclerView.Adapter<MyTaskDetailAdapter.MyTaskDetailViewHolder>() {

    inner class MyTaskDetailViewHolder(val binding: AdapterMyTaskRowFileBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyTaskDetailViewHolder {

        val binding = AdapterMyTaskRowFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyTaskDetailViewHolder(binding)

    }

    override fun onBindViewHolder(holder: MyTaskDetailViewHolder, position: Int) {
        with(holder) {
            with(myTaskDetailList[position]) {
                binding.txtCustomerName.text = this.customerDetail?.name
                binding.txtTicketNumber.text = this.ticketNumber.toString()
                binding.txtMachineName.text = this.machineDetail?.machineName
                binding.btnNew.text = this.status

                binding.btnNew.setOnClickListener {
                    binding.mainLayout.performClick()
                }

                binding.imgCall.setOnClickListener {
                    onItemListener.phoneClicked(position,myTaskDetailList[position].customerDetail?.phoneNumber!!)
                }

                binding.mainLayout.setOnClickListener {
                    onItemListener.onItemClicked(position,myTaskDetailList)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return myTaskDetailList.size
    }

    interface OnItemClickLListener{
        fun onItemClicked(position: Int,myTaskList : ArrayList<TicketsDetail>)
        fun phoneClicked(position: Int,phoneNumber : String)
    }
}