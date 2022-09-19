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
import com.example.kuntan.utility.AppUtil

class ClockThemeSpinnerAdapter(
    private val context: Context,
    private val listOfItem: List<String>
) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private var selectedItem = ""
    private var selectedListener: ItemSelectedListener? = null

    @SuppressLint("ViewHolder", "UseCompatLoadingForDrawables")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = layoutInflater.inflate(R.layout.layout_analog_clock_theme_item_spinner, parent, false)
        selectedItem = "${getItem(position)}"
        val textViewSpinner = view.findViewById<AppCompatTextView>(R.id.textViewSpinner)
        val imageClockThemeIcon = view.findViewById<ImageView>(R.id.imageClockThemeIcon)
        textViewSpinner.text = selectedItem
        imageClockThemeIcon.background = context.resources.getDrawable(AppUtil.convertResIdFromId(position), null)

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = layoutInflater.inflate(R.layout.layout_clock_theme_drop_down_item, parent, false)
        val imageItemSelected = view.findViewById<ImageView>(R.id.imageItemSelected)
        val textViewSpinnerItem = view.findViewById<AppCompatTextView>(R.id.textViewSpinnerItem)
        val lineBottom = view.findViewById<View>(R.id.lineBottom)
        if (selectedItem == listOfItem[position]) imageItemSelected.visibility = View.VISIBLE
        if (position == listOfItem.size - 1) lineBottom.visibility = View.INVISIBLE
        textViewSpinnerItem.text = "${getItem(position)}"
        imageItemSelected.setImageResource(AppUtil.convertResIdFromId(position))

        return view
    }

    override fun getCount(): Int {
        return listOfItem.size
    }

    override fun getItem(position: Int): Any {
        return listOfItem[position]
    }

    override fun getItemId(position: Int): Long {
        selectedListener?.onItemSelected("${getItem(position)}")
        return position.toLong()
    }

    fun addOnItemSelectedListener(selectedListener: ItemSelectedListener) {
        this.selectedListener = selectedListener
    }

    interface ItemSelectedListener {
        fun onItemSelected(clockTheme: String)
    }
}