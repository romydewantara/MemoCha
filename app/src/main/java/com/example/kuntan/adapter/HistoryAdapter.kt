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
import com.example.kuntan.entity.History
import com.example.kuntan.utility.AppUtil
import com.google.gson.Gson
import kotlinx.android.synthetic.main.recyclerview_item_month.view.*
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val context: Context,
    private val histories: ArrayList<List<History>>,
    private val currentMonth: String,
    private val currentYear: String,
    historyAdapterListener: HistoryAdapterListener
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    @SuppressLint("SimpleDateFormat")
    private val currentMonthYear = SimpleDateFormat("MM-yyyy").format(Calendar.getInstance().time)
    private val mListener = historyAdapterListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_month, parent, false))
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val monthCode = AppUtil.convertMonthCodeFromId(position)
        val monthName = AppUtil.convertMonthNameFromCode(context, monthCode)

        holder.itemView.textViewMonthCode.text = monthCode
        holder.itemView.textViewMonthName.text = monthName
        holder.itemView.textViewTotal.text = getTotalExpenses(position)

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(holder.itemView.textViewMonthCode,
            1, 40, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(holder.itemView.textViewTotalTitle,
            1, 12, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(holder.itemView.textViewTotal,
            1, 18, 1, TypedValue.COMPLEX_UNIT_SP)

        if (currentMonth == monthCode && currentYear == currentMonthYear.split("-")[1])
            holder.itemView.imageTagLabel.visibility = View.VISIBLE

        holder.itemView.layoutItemMonth.setOnClickListener {
            mListener.onMonthSelected(monthCode, Gson().toJson(histories[position]))
        }
    }

    override fun getItemCount(): Int = 12

    private fun getTotalExpenses(position: Int): String {
        var sum = BigInteger("0")
        if (histories[position].isNotEmpty()) {
            for (i in 0 until histories[position].size) {
                val amount = histories[position][i].amount.replace(",", "").toBigInteger()
                sum += amount
            }
        }
        return "Rp ${String.format("%,d", sum)}".replace(",", ".")
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface HistoryAdapterListener {
        fun onMonthSelected(month: String, history: String)
    }
}