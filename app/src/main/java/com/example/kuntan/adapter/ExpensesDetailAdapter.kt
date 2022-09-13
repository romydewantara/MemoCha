package com.example.kuntan.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kuntan.R
import com.example.kuntan.entity.History
import kotlinx.android.synthetic.main.recyclerview_item_monthly_expenses.view.*

class ExpensesDetailAdapter(
    private val context: Context,
    private val histories: ArrayList<History>
) : RecyclerView.Adapter<ExpensesDetailAdapter.ExpensesDetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpensesDetailViewHolder {
        return ExpensesDetailViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_monthly_expenses, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ExpensesDetailViewHolder, position: Int) {
        holder.itemView.textViewDate.text = "${histories[position].date} ${histories[position].month} ${histories[position].year}"
        holder.itemView.textViewTime.text = "(${histories[position].time})"
        holder.itemView.textViewMethod.text = histories[position].method
        holder.itemView.textViewCategory.text = histories[position].category
        holder.itemView.textViewAmount.text = "Rp ${histories[position].amount}"
        holder.itemView.textViewGoods.text = histories[position].goods
        holder.itemView.textViewDescription.text = histories[position].description
        when(histories[position].method) {
            context.getString(R.string.method_cash) -> {
                holder.itemView.iconMethod.setImageResource(R.drawable.ic_cash)
            }
            context.getString(R.string.method_debit) -> {
                holder.itemView.iconMethod.setImageResource(R.drawable.ic_debit)
            }
            context.getString(R.string.method_transfer) -> {
                holder.itemView.iconMethod.setImageResource(R.drawable.ic_transfer)
            }

        }
    }

    override fun getItemCount(): Int = histories.size

    class ExpensesDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun setData(list: List<History>) {
        histories.clear()
        histories.addAll(list)
        notifyDataSetChanged()
    }
}