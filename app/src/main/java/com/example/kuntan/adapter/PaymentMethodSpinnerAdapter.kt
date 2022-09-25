package com.example.kuntan.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import com.example.kuntan.R

class PaymentMethodSpinnerAdapter(
    private val context: Context,
    private val listOfItem: List<String>
) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private var selectedItem = ""
    private var itemSelectedListener: ItemSelectedListener? = null

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = layoutInflater.inflate(R.layout.layout_payment_method_item_spinner, parent, false)
        selectedItem = "${getItem(position)}"
        val textViewSpinner = view.findViewById<AppCompatTextView>(R.id.textViewSpinner)
        val imageViewIcon = view.findViewById<ImageView>(R.id.imageSpinnerIcon)
        textViewSpinner.text = selectedItem
        when(getItem(position)) {
            context.getString(R.string.method_cash) -> {
                imageViewIcon.setBackgroundResource(R.drawable.ic_cash)
            }
            context.getString(R.string.method_debit) -> {
                imageViewIcon.setBackgroundResource(R.drawable.ic_debit)
            }
            context.getString(R.string.method_transfer) -> {
                imageViewIcon.setBackgroundResource(R.drawable.ic_transfer)
            }
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = layoutInflater.inflate(R.layout.layout_payment_method_drop_down_item, parent, false)
        val imageItemSelected = view.findViewById<ImageView>(R.id.imageItemSelected)
        val textViewSpinnerItem = view.findViewById<AppCompatTextView>(R.id.textViewSpinnerItem)
        val lineBottom = view.findViewById<View>(R.id.lineBottom)
        if (selectedItem == listOfItem[position]) imageItemSelected.visibility = View.VISIBLE
        if (position == listOfItem.size - 1) lineBottom.visibility = View.INVISIBLE
        textViewSpinnerItem.text = "${getItem(position)}"
        when(getItem(position)) {
            context.getString(R.string.method_cash) -> {
                imageItemSelected.setImageResource(R.drawable.ic_cash)
            }
            context.getString(R.string.method_debit) -> {
                imageItemSelected.setImageResource(R.drawable.ic_debit)
            }
            context.getString(R.string.method_transfer) -> {
                imageItemSelected.setImageResource(R.drawable.ic_transfer)
            }
        }

        return view
    }

    override fun getCount(): Int {
        return listOfItem.size
    }

    override fun getItem(position: Int): Any {
        return listOfItem[position]
    }

    override fun getItemId(position: Int): Long {
        itemSelectedListener?.onItemSelected("${getItem(position)}")
        return position.toLong()
    }

    fun addOnPaymentMethodListener(itemSelectedListener: ItemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener
    }

    interface ItemSelectedListener {
        fun onItemSelected(selectedItem: String)
    }
}