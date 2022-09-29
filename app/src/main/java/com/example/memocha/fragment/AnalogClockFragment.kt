package com.example.memocha.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.memocha.R
import com.example.memocha.utility.Constant
import kotlinx.android.synthetic.main.fragment_analog_clock.*

class AnalogClockFragment(private val mClockTheme: String) : Fragment(R.layout.fragment_analog_clock) {

    var clockTheme = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectClockTheme(mClockTheme)

        clockThemePrimary.setOnClickListener { selectClockTheme(Constant.DASHBOARD_CLOCK_PRIMARY) }
        clockThemeSecondary.setOnClickListener { selectClockTheme(Constant.DASHBOARD_CLOCK_SECONDARY) }
        clockThemeDark.setOnClickListener { selectClockTheme(Constant.DASHBOARD_CLOCK_DARK) }
        clockThemeYellow.setOnClickListener { selectClockTheme(Constant.DASHBOARD_CLOCK_YELLOW) }
        clockThemeRedOrange.setOnClickListener { selectClockTheme(Constant.DASHBOARD_CLOCK_RED_ORANGE) }
        clockThemePink.setOnClickListener { selectClockTheme(Constant.DASHBOARD_CLOCK_PINK) }
    }

    private fun selectClockTheme(clockThemeSelected: String) {
        clockTheme = clockThemeSelected
        when(clockThemeSelected) {
            Constant.DASHBOARD_CLOCK_PRIMARY -> { setChecked(checkedPrimary) }
            Constant.DASHBOARD_CLOCK_SECONDARY -> { setChecked(checkedSecondary) }
            Constant.DASHBOARD_CLOCK_DARK -> { setChecked(checkedDark) }
            Constant.DASHBOARD_CLOCK_YELLOW -> { setChecked(checkedYellow) }
            Constant.DASHBOARD_CLOCK_RED_ORANGE -> { setChecked(checkedRedOrange) }
            Constant.DASHBOARD_CLOCK_PINK -> { setChecked(checkedPink) }
        }
    }

    private fun setChecked(checkedTheme: View) {
        val viewArray = arrayListOf(checkedPrimary, checkedSecondary, checkedDark, checkedYellow, checkedRedOrange, checkedPink)
        for (i in 0 until viewArray.size) {
            Log.d("ACF", "setChecked - i: $i checked: $checkedTheme")
            if (viewArray[i] == checkedTheme) viewArray[i].visibility = View.VISIBLE
            else viewArray[i].visibility = View.GONE
        }
    }
}