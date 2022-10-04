package com.example.memocha.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
        val dateAdded = "${histories[position].date}/${histories[position].month}/${histories[position].year}"
        val dateModified = "${histories[position].dateEdited}/${histories[position].monthEdited}/${histories[position].yearEdited}"
        holder.itemView.textViewDate.text = dateAdded
        holder.itemView.textViewTime.text = "(${histories[position].time})"
        holder.itemView.textViewMethod.text = histories[position].method
        holder.itemView.textViewCategory.text = histories[position].category
        holder.itemView.textViewAmount.text = "Rp ${histories[position].amount.replace(",", ".")}"
        holder.itemView.textViewGoods.text = histories[position].goods
        holder.itemView.textViewDescription.text = histories[position].description

        holder.itemView.layoutEdited.visibility = if (histories[position].isEdited) View.VISIBLE else View.GONE
        holder.itemView.textViewDateAdded.text = "${AppUtil.convertDayCodeToName(context, histories[position].day)}, $dateAdded (${histories[position].time})"
        holder.itemView.textViewDateModified.text = "${AppUtil.convertDayCodeToName(context, histories[position].dayEdited)}, $dateModified (${histories[position].timeEdited})"
        holder.itemView.layoutInfo.visibility = if (histories[position].isShownInfo) View.VISIBLE else View.GONE

        when (histories[position].method) {
            context.getString(R.string.method_cash) -> { holder.itemView.iconMethod.setImageResource(R.drawable.ic_cash) }
            context.getString(R.string.method_debit) -> { holder.itemView.iconMethod.setImageResource(R.drawable.ic_debit) }
            context.getString(R.string.method_transfer) -> { holder.itemView.iconMethod.setImageResource(R.drawable.ic_transfer) }
        }
        if (histories[position].description.isEmpty()) holder.itemView.textViewDescription.visibility = View.GONE
        else holder.itemView.textViewDescription.visibility = View.VISIBLE
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            holder.itemView.textViewGoods, 1, 14, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            holder.itemView.textViewAmount, 1, 16, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            holder.itemView.textViewDateAdded, 1, 14, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            holder.itemView.textViewDateModified, 1, 14, 1, TypedValue.COMPLEX_UNIT_SP)

        holder.itemView.layoutEdited.setOnClickListener {
            histories[position].isShownInfo = true
            holder.itemView.layoutInfo.visibility = View.VISIBLE
            holder.itemView.layoutInfo.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
        }
        holder.itemView.layoutInfo.setOnClickListener {
            histories[position].isShownInfo = false
            holder.itemView.layoutInfo.visibility = View.GONE
            holder.itemView.layoutInfo.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
        }
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