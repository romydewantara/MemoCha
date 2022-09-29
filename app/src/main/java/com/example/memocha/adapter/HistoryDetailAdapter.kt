package com.example.memocha.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.memocha.R
import com.example.memocha.entity.History
import com.example.memocha.utility.AppUtil
import kotlinx.android.synthetic.main.recyclerview_item_monthly_expenses.view.*

@SuppressLint("SetTextI18n", "NotifyDataSetChanged")
class HistoryDetailAdapter(
    private val context: Context,
    private val histories: ArrayList<History>
) : RecyclerView.Adapter<HistoryDetailAdapter.ExpensesDetailViewHolder>() {

    private lateinit var historyDetailListener: HistoryDetailListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpensesDetailViewHolder {
        return ExpensesDetailViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_item_monthly_expenses, parent, false))
    }

    override fun onBindViewHolder(holder: ExpensesDetailViewHolder, position: Int) {
        holder.itemView.textViewDate.text = "${histories[position].date} ${AppUtil.convertMonthNameFromCode(context, histories[position].month)} ${histories[position].year}"
        holder.itemView.textViewTime.text = "(${histories[position].time})"
        holder.itemView.textViewMethod.text = histories[position].method
        holder.itemView.textViewCategory.text = histories[position].category
        holder.itemView.textViewAmount.text = "Rp ${histories[position].amount.replace(",", ".")}"
        holder.itemView.textViewGoods.text = histories[position].goods
        holder.itemView.textViewDescription.text = histories[position].description
        when (histories[position].method) {
            context.getString(R.string.method_cash) -> { holder.itemView.iconMethod.setImageResource(R.drawable.ic_cash) }
            context.getString(R.string.method_debit) -> { holder.itemView.iconMethod.setImageResource(R.drawable.ic_debit) }
            context.getString(R.string.method_transfer) -> { holder.itemView.iconMethod.setImageResource(R.drawable.ic_transfer) }
        }
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            holder.itemView.textViewGoods, 1, 14, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            holder.itemView.textViewAmount, 1, 16, 1, TypedValue.COMPLEX_UNIT_SP)
        holder.itemView.layoutItemExpenses.setOnClickListener {
            historyDetailListener.onItemExpensesClicked(histories[position])
        }
    }

    override fun getItemCount(): Int = histories.size

    fun setData(list: List<History>) {
        histories.clear()
        histories.addAll(list)
        histories.reverse()
        notifyDataSetChanged()
    }

    fun addOnHistoryDetailListener(historyDetailListener: HistoryDetailListener) {
        this.historyDetailListener = historyDetailListener
    }

    class ExpensesDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface HistoryDetailListener {
        fun onItemExpensesClicked(history: History)
    }
}