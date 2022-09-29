package com.example.memocha.lib

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.Window
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.DialogFragment
import com.example.memocha.R
import kotlinx.android.synthetic.main.layout_dialog_calendar.*
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("UseCompatLoadingForDrawables", "StaticFieldLeak")
class CalendarDialog : DialogFragment(R.layout.layout_dialog_calendar) {

    companion object {
        private lateinit var context: Context
        private lateinit var dateChangeListener: DateChangeListener
    }

    private var mDate: String = ""

    fun newInstance(context: Context, date: String): CalendarDialog {
        val fragmentCalendarDialog = CalendarDialog()
        val bundle = Bundle()
        Companion.context = context
        mDate = date

        fragmentCalendarDialog.arguments = bundle
        return fragmentCalendarDialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (dialog != null && dialog?.window != null) {
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(Companion.context.resources.getDrawable(R.drawable.background_calendar_dialog_rounded, null))
        }
        val dateSelected = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(mDate)
        calendarView.setDate(dateSelected!!.time, true, true)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            dateChangeListener.onDateChanges("${String.format("%02d", dayOfMonth)}-${String.format("%02d", month+1)}-$year")
            this.dismiss()
        }
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            textViewInfo, 1, 12, 1, TypedValue.COMPLEX_UNIT_SP)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (Companion.context.applicationContext is DateChangeListener) {
            dateChangeListener = Companion.context as DateChangeListener
        }
    }

    fun onDateChangeListener(dateChangeListener: DateChangeListener): CalendarDialog {
        Companion.dateChangeListener = dateChangeListener
        return this
    }

    interface DateChangeListener {
        fun onDateChanges(dateSelected: String)
    }
}