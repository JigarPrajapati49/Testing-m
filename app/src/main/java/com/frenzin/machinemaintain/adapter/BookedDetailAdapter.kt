package com.frenzin.machinemaintain.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.frenzin.machinemaintain.model.resModel.BookDetailsImage
import com.frenzin.machinemaintain.databinding.AdapterBookedImageRowFileBinding

class BookedDetailAdapter(
    private var bookedDetailImageLIst: ArrayList<BookDetailsImage>
) : RecyclerView.Adapter<BookedDetailAdapter.BookedDetailViewHolder>() {

    inner class BookedDetailViewHolder(val binding: AdapterBookedImageRowFileBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookedDetailViewHolder {

        val binding = AdapterBookedImageRowFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookedDetailViewHolder(binding)

    }

    override fun onBindViewHolder(holder: BookedDetailViewHolder, position: Int) {
        with(holder) {
            with(bookedDetailImageLIst[position]) {
                Glide.with(binding.root).load(this.image).transition(DrawableTransitionOptions.withCrossFade()).into(binding.imgBookedDetail)
            }
        }
    }

    override fun getItemCount(): Int {
        return bookedDetailImageLIst.size
    }
}