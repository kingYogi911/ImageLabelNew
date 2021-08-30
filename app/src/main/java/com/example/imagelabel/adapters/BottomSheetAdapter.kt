package com.example.imagelabel.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.imagelabel.R
import com.example.imagelabel.activities.MainActivity
import com.example.imagelabel.data.MyColor
import com.example.imagelabel.data.Polygon
import com.example.imagelabel.databinding.PolygonRvItemBinding
import com.example.imagelabel.util.ColorSelectedListener
import com.example.imagelabel.util.PolygonSelectedListener

class BottomSheetAdapter(private val itemList: List<Any>, val type: MainActivity.BOTTOM_SHEET) :
    RecyclerView.Adapter<BottomSheetAdapter.MyViewHolder>() {
    class MyViewHolder(val binding: PolygonRvItemBinding) : RecyclerView.ViewHolder(binding.root)
    private lateinit var context:Context
    var polygonSelectedListener: PolygonSelectedListener? = null
    var colorSelectedListener: ColorSelectedListener?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context=parent.context
        return MyViewHolder(
            PolygonRvItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (type == MainActivity.BOTTOM_SHEET.POLYGON) {
            val temp = itemList[position] as Polygon
            holder.binding.apply {
                item = temp
                rvIv.setImageResource(temp.resId)
                executePendingBindings()
            }
            holder.itemView.setOnClickListener {
                polygonSelectedListener?.polygonSelected(temp)
            }
        }else if(type==MainActivity.BOTTOM_SHEET.COLOR){
            val temp=itemList[position] as MyColor
            holder.binding.apply {
                rvIv.setImageResource(R.drawable.square_filled)
                rvIv.setColorFilter(ContextCompat.getColor(context,temp.colorRes))
                executePendingBindings()
            }
            holder.itemView.setOnClickListener {
                colorSelectedListener?.colorSelected(temp)
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}