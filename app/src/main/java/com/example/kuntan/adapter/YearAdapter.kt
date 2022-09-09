package com.example.kuntan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kuntan.R
import kotlinx.android.synthetic.main.recyclerview_item_year.view.*

class YearAdapter(year: Int) : RecyclerView.Adapter<YearAdapter.YearViewHolder>() {

    private val yearArrayList = arrayListOf(
        "January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YearViewHolder {
        return YearViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_year, parent, false))
    }

    override fun onBindViewHolder(holder: YearViewHolder, position: Int) {
        holder.itemView.textViewMonthName.text = yearArrayList[position]
        holder.itemView.layoutItemYear.setOnClickListener {

        }
    }

    override fun getItemCount(): Int = yearArrayList.size

    class YearViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}