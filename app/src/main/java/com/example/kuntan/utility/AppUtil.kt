package com.example.kuntan.utility

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
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
        fun getHeightOfSoftKeyboard(view: View) : Int {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            return (screenHeight - rect.bottom)
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
        @SuppressLint("UseCompatLoadingForDrawables")
        fun convertDrawableFromTheme(c: Context, theme: String): Drawable {
            when(theme) {
                Constant.DASHBOARD_CLOCK_PRIMARY -> {
                    return c.resources.getDrawable(R.drawable.background_analog_clock_primary, null)
                }
                Constant.DASHBOARD_CLOCK_SECONDARY -> {
                    return c.resources.getDrawable(R.drawable.background_analog_clock_secondary, null)
                }
                Constant.DASHBOARD_CLOCK_DARK -> {
                    return c.resources.getDrawable(R.drawable.background_analog_clock_dark, null)
                }
                Constant.DASHBOARD_CLOCK_YELLOW -> {
                    return c.resources.getDrawable(R.drawable.background_analog_clock_yellow, null)
                }
                Constant.DASHBOARD_CLOCK_RED_ORANGE -> {
                    return c.resources.getDrawable(R.drawable.background_analog_clock_red_orange, null)
                }
                Constant.DASHBOARD_CLOCK_PINK -> {
                    return c.resources.getDrawable(R.drawable.background_analog_clock_pink, null)
                }
            }
            return c.resources.getDrawable(R.drawable.background_analog_clock_primary, null)
        }
        fun convertResIdFromId(id: Int): Int {
            when(id) {
                0 -> { return R.drawable.ic_clock_theme_primary }
                1 -> { return R.drawable.ic_clock_theme_secondary }
                2 -> { return R.drawable.ic_clock_theme_dark }
                3 -> { return R.drawable.ic_clock_theme_yellow }
                4 -> { return R.drawable.ic_clock_theme_red_orange }
                5 -> { return R.drawable.ic_clock_theme_pink }
            }
            return R.drawable.ic_clock_theme_primary
        }
        fun convertIdAnalogClockFromTheme(theme: String): Int {
            when(theme) {
                Constant.DASHBOARD_CLOCK_PRIMARY -> { return 0 }
                Constant.DASHBOARD_CLOCK_SECONDARY -> { return 1 }
                Constant.DASHBOARD_CLOCK_DARK -> { return 2 }
                Constant.DASHBOARD_CLOCK_YELLOW -> { return 3 }
                Constant.DASHBOARD_CLOCK_RED_ORANGE -> { return 4 }
                Constant.DASHBOARD_CLOCK_PINK -> { return 5 }
            }
            return 0
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
        fun convertPaymentMethodFromType(c: Context, type: String): Int {
            when(type) {
                c.getString(R.string.method_cash) -> {return 0}
                c.getString(R.string.method_debit) -> {return 1}
                c.getString(R.string.method_transfer) -> {return 2}
            }
            return 0
        }
        fun convertCardinalNumber(number: String): String {
            return ""
        }
    }
}