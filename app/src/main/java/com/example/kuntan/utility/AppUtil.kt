package com.example.kuntan.utility

import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.kuntan.R

class AppUtil {
    companion object {
        fun isKeyboardVisible(view: View) : Boolean {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            return (keypadHeight > screenHeight * 0.15)
        }
        fun showSoftKeyboard(view: View, context: Context) {
            if (view.requestFocus()) {
                val imm: InputMethodManager =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        fun hideSoftKeyboard(view: View, context: Context) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        fun dpToPx(c: Context, dipValue: Float): Int {
            val metrics = c.resources.displayMetrics
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics).toInt()
        }
        fun convertMonthNameFromCode(context: Context, month: String): String {
            when(month){
                "01" -> {return context.getString(R.string.month_january)}
                "02" -> {return context.getString(R.string.month_february)}
                "03" -> {return context.getString(R.string.month_march)}
                "04" -> {return context.getString(R.string.month_april)}
                "05" -> {return context.getString(R.string.month_may)}
                "06" -> {return context.getString(R.string.month_june)}
                "07" -> {return context.getString(R.string.month_july)}
                "08" -> {return context.getString(R.string.month_august)}
                "09" -> {return context.getString(R.string.month_september)}
                "10" -> {return context.getString(R.string.month_october)}
                "11" -> {return context.getString(R.string.month_november)}
                "12" -> {return context.getString(R.string.month_december)}
            }
            return "empty"
        }
        fun convertMonthCodeFromId(id: Int): String {
            when(id){
                0 -> {return "01"}
                1 -> {return "02"}
                2 -> {return "03"}
                3 -> {return "04"}
                4 -> {return "05"}
                5 -> {return "06"}
                6 -> {return "07"}
                7 -> {return "08"}
                8 -> {return "09"}
                9 -> {return "10"}
                10 -> {return "11"}
                11 -> {return "12"}
            }
            return "empty"
        }
    }
}