package com.frenzin.machinemaintain.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.frenzin.machinemaintain.R


class ImageAdapter(var context: Context) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private var imageList: ArrayList<String> = ArrayList()
    private  var removeImage: RemoveImage? =null

    fun setRemover(removeImage: RemoveImage){
       this.removeImage = removeImage
    }

    fun setList(list: ArrayList<String>) {
        this.imageList.clear()
        this.imageList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.adapter_image_row_file,parent,false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {

        Glide.with(context).load(Uri.parse(imageList[position])).into(holder.image)

        if (removeImage!=null){
            holder.removeImage.setOnClickListener{
                removeImage?.removeImage(position)
            }
        }else{
            holder.removeImage.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
       return imageList.size
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image : ImageView = itemView.findViewById(R.id.image)
        val removeImage : ImageView = itemView.findViewById(R.id.brn_remove)
    }

    interface RemoveImage{
        fun removeImage(position: Int)
    }
}