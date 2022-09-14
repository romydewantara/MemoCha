package com.example.kuntan.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.kuntan.R
import com.example.kuntan.utility.KuntanRoomDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.recyclerview_item_year.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val context: Context,
    private val currentMonth: String,
    private val currentYear: String,
    yearAdapterListener: YearAdapterListener
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    @SuppressLint("SimpleDateFormat")
    private val currentMonthYear = SimpleDateFormat("MMMM-yyyy").format(Calendar.getInstance().time)
    private val database by lazy { KuntanRoomDatabase(context) }
    private val mListener = yearAdapterListener
    private val months = arrayListOf(
        context.getString(R.string.month_january),
        context.getString(R.string.month_february),
        context.getString(R.string.month_march),
        context.getString(R.string.month_april),
        context.getString(R.string.month_may),
        context.getString(R.string.month_june),
        context.getString(R.string.month_july),
        context.getString(R.string.month_august),
        context.getString(R.string.month_september),
        context.getString(R.string.month_october),
        context.getString(R.string.month_november),
        context.getString(R.string.month_december)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_year, parent, false))
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        @SuppressLint("SetTextI18n")
        when (position) {
            0 -> { holder.itemView.textViewMonthCode.text = "01" }
            1 -> { holder.itemView.textViewMonthCode.text = "02" }
            2 -> { holder.itemView.textViewMonthCode.text = "03" }
            3 -> { holder.itemView.textViewMonthCode.text = "04" }
            4 -> { holder.itemView.textViewMonthCode.text = "05" }
            5 -> { holder.itemView.textViewMonthCode.text = "06" }
            6 -> { holder.itemView.textViewMonthCode.text = "07" }
            7 -> { holder.itemView.textViewMonthCode.text = "08" }
            8 -> { holder.itemView.textViewMonthCode.text = "09" }
            9 -> { holder.itemView.textViewMonthCode.text = "10" }
            10 -> { holder.itemView.textViewMonthCode.text = "11" }
            11 -> { holder.itemView.textViewMonthCode.text = "12" }
        }
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(holder.itemView.textViewMonthCode,
            1, 40, 1, TypedValue.COMPLEX_UNIT_SP)
        holder.itemView.textViewMonthName.text = months[position]
        setSummary(months[position], holder.itemView.textViewTotal)
        if (currentYear == currentMonthYear.split("-")[1] && months[position] == currentMonth)
            holder.itemView.imageTagLabel.visibility = View.VISIBLE

        holder.itemView.layoutItemYear.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val history = database.historyDao().getHistory(currentYear, months[position])
                mListener.onMonthSelected(months[position], Gson().toJson(history))
            }
        }
    }

    override fun getItemCount(): Int = months.size

    private fun setSummary(month: String, textViewSummary: AppCompatTextView) {
        CoroutineScope(Dispatchers.IO).launch {
            val history = database.historyDao().getHistory(currentYear, month)
            if (history.isNotEmpty()) {
                var sum = 0
                for (i in history.indices) {
                    val amount = history[i].amount.replace(",", "")
                    if (amount.isNotEmpty()) sum += amount.toInt()
                }
                val summary = "Rp ${String.format("%,d", sum)}"
                (context as Activity).runOnUiThread {
                    textViewSummary.text = summary.replace(",", ".")
                }
            }
        }
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface YearAdapterListener {
        fun onMonthSelected(month: String, history: String)
    }
}