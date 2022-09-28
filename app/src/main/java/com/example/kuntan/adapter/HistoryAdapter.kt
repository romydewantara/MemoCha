package com.example.kuntan.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.kuntan.R
import com.example.kuntan.utility.AppUtil
import kotlinx.android.synthetic.main.recyclerview_item_month.view.*
import java.text.SimpleDateFormat
import java.util.Calendar

@SuppressLint("SimpleDateFormat", "NotifyDataSetChanged")
class HistoryAdapter(
    private val context: Context,
    private val totalList: ArrayList<String>,
    private val historyAdapterListener: HistoryAdapterListener
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val currentMonthYear = SimpleDateFormat("MM-yyyy").format(Calendar.getInstance().time)
    private var yearSelected = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_month, parent, false))
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val monthCode = AppUtil.convertMonthCodeFromId(position)
        val monthName = AppUtil.convertMonthNameFromCode(context, monthCode)

        holder.itemView.textViewMonthCode.text = monthCode
        holder.itemView.textViewMonthName.text = monthName
        holder.itemView.textViewTotal.text = totalList[position]

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(holder.itemView.textViewMonthCode,
            1, 40, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(holder.itemView.textViewTotalTitle,
            1, 12, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(holder.itemView.textViewTotal,
            1, 18, 1, TypedValue.COMPLEX_UNIT_SP)

        if (yearSelected == currentMonthYear.split("-")[1] && monthCode == currentMonthYear.split("-")[0])
            holder.itemView.imageTagLabel.visibility = View.VISIBLE else holder.itemView.imageTagLabel.visibility = View.GONE

        holder.itemView.layoutItemMonth.setOnClickListener {
            historyAdapterListener.onMonthSelected(monthCode, yearSelected)
        }
    }

    override fun getItemCount(): Int = totalList.size

    fun refreshData(list: List<String>, year: String) {
        totalList.clear()
        totalList.addAll(list)
        yearSelected = year
        notifyDataSetChanged()
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface HistoryAdapterListener {
        fun onMonthSelected(month: String, year: String)
    }
}