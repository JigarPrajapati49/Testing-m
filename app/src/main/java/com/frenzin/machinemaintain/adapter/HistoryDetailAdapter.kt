package com.frenzin.machinemaintain.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.frenzin.machinemaintain.databinding.AdapterHistoryRowFileBinding
import com.frenzin.machinemaintain.model.resModel.HistoryDetail

class HistoryDetailAdapter(
    private var historyDetailList: ArrayList<HistoryDetail>
) : RecyclerView.Adapter<HistoryDetailAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(val binding: AdapterHistoryRowFileBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {

        val binding = AdapterHistoryRowFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)

    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        with(holder) {
            with(historyDetailList[position]) {
                binding.txtName.text = this.name
                binding.txtModelName.text = this.machineName
            }
        }
    }

    override fun getItemCount(): Int {
        return historyDetailList.size
    }
}