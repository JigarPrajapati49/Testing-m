package com.frenzin.machinemaintain.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.frenzin.machinemaintain.R
import com.frenzin.machinemaintain.databinding.AdapterStockDetailRowFileBinding
import com.frenzin.machinemaintain.model.resModel.GetMachineDetailList
import java.text.SimpleDateFormat

class MachinesStockAdapter(
    private var myMachinesStockDetail: ArrayList<GetMachineDetailList>, var context: Context,var onItemClick : onItemClicked
) : RecyclerView.Adapter<MachinesStockAdapter.MachinesStockViewHolder>() {

    inner class MachinesStockViewHolder(val binding: AdapterStockDetailRowFileBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var selectedItemPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MachinesStockViewHolder {

        val binding = AdapterStockDetailRowFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MachinesStockViewHolder(binding)

    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: MachinesStockViewHolder, position: Int) {
        with(holder) {
            with(myMachinesStockDetail[position]) {

                val date = this.invertDate?.let { SimpleDateFormat("yyyy-MM-dd").parse(it) }
                val format = SimpleDateFormat("dd MMM yyyy").format(date!!)

                binding.txtDate.text = format
                binding.txtMachinesName.text = this.machineSerialNumber

                val positions = adapterPosition
                val isSelected = positions == selectedItemPosition
                binding.llMain.setBackgroundDrawable(if (isSelected) context.getDrawable(R.drawable.bg_select_shape) else context.getDrawable(R.drawable.bg_shape))

                binding.root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        setSelectedItemPosition(position)
                        onItemClick.itemClicked(position,this.id.toString())
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return myMachinesStockDetail.size
    }

    fun setSelectedItemPosition(position: Int) {
        val previousPosition = selectedItemPosition
        selectedItemPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(position)
    }

    fun filterList(filterMachineList: ArrayList<GetMachineDetailList>) {
        this.myMachinesStockDetail = filterMachineList
        notifyDataSetChanged()
    }

    interface onItemClicked{
        fun itemClicked(position: Int,machineId : String)
    }

}