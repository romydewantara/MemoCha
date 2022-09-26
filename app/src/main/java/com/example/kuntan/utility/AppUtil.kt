package com.example.kuntan.utility

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.kuntan.R
import java.util.Random
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class AppUtil {
    companion object {
        fun writeFileToStorage(c: Context, folderName: String, fileName: String, body: String) {
            val dir = File(c.externalCacheDir?.path + File.separator + folderName + File.separator)
            if (!dir.exists()) {
                dir.mkdir()
            }
            try {
                val file = File(dir, "$fileName.txt")
                val fileWriter = FileWriter(file)
                fileWriter.append(body)
                fileWriter.flush()
                fileWriter.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        fun readFileFromStorage(c: Context, folderName: String, fileName: String): String {
            val file = File(c.externalCacheDir?.path + File.separator + folderName + File.separator, "$fileName.txt")
            val sb = StringBuilder()
            try {
                val br = BufferedReader(FileReader(file))
                sb.append(br.readLine())
                sb.append('\n')
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return sb.toString()
        }
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
        fun getWidthPercent(c: Context, percent: Float) : Float {
            return c.resources.displayMetrics.widthPixels * (percent / 100)
        }
        fun getHeightPercent(c: Context, percent: Float) : Float {
            return c.resources.displayMetrics.heightPixels * (percent / 100)
        }
        @SuppressLint("UseCompatLoadingForDrawables")
        fun convertDrawableFromTheme(c: Context, theme: String): Drawable {
            when(theme) {
                Constant.DASHBOARD_CLOCK_PRIMARY -> {
                    return c.resources.getDrawable(R.drawable.background_analog_clock_primary, null) }
                Constant.DASHBOARD_CLOCK_SECONDARY -> {
                    return c.resources.getDrawable(R.drawable.background_analog_clock_secondary, null) }
                Constant.DASHBOARD_CLOCK_DARK -> {
                    return c.resources.getDrawable(R.drawable.background_analog_clock_dark, null) }
                Constant.DASHBOARD_CLOCK_YELLOW -> {
                    return c.resources.getDrawable(R.drawable.background_analog_clock_yellow, null) }
                Constant.DASHBOARD_CLOCK_RED_ORANGE -> {
                    return c.resources.getDrawable(R.drawable.background_analog_clock_red_orange, null) }
                Constant.DASHBOARD_CLOCK_PINK -> {
                    return c.resources.getDrawable(R.drawable.background_analog_clock_pink, null) }
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
        fun convertMonthCodeFromName(context: Context, monthName: String): String {
            when (monthName) {
                context.getString(R.string.month_january) -> { return "01" }
                context.getString(R.string.month_february) -> { return "02" }
                context.getString(R.string.month_march) -> { return "03" }
                context.getString(R.string.month_april) -> { return "04" }
                context.getString(R.string.month_may) -> { return "05" }
                context.getString(R.string.month_june) -> { return "06" }
                context.getString(R.string.month_july) -> { return "07" }
                context.getString(R.string.month_august) -> { return "08" }
                context.getString(R.string.month_september) -> { return "09" }
                context.getString(R.string.month_october) -> { return "10" }
                context.getString(R.string.month_november) -> { return "11" }
                context.getString(R.string.month_december) -> { return "12" }
            }
            return "01"
        }
        fun convertMonthNameFromCode(context: Context, month: String): String {
            when (month) {
                "01" -> { return context.getString(R.string.month_january) }
                "02" -> { return context.getString(R.string.month_february) }
                "03" -> { return context.getString(R.string.month_march) }
                "04" -> { return context.getString(R.string.month_april) }
                "05" -> { return context.getString(R.string.month_may) }
                "06" -> { return context.getString(R.string.month_june) }
                "07" -> { return context.getString(R.string.month_july) }
                "08" -> { return context.getString(R.string.month_august) }
                "09" -> { return context.getString(R.string.month_september) }
                "10" -> { return context.getString(R.string.month_october) }
                "11" -> { return context.getString(R.string.month_november) }
                "12" -> { return context.getString(R.string.month_december) }
            }
            return "empty"
        }
        fun convertMonthCodeFromId(id: Int): String {
            when (id) {
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
            when (type) {
                c.getString(R.string.method_cash) -> {return 0}
                c.getString(R.string.method_debit) -> {return 1}
                c.getString(R.string.method_transfer) -> {return 2}
            }
            return 0
        }
        fun convertCardinalNumber(number: String): String {
            return ""
        }
        fun randomIcon(context: Context, arrayListIconUsed: ArrayList<Int>): Int {
            var icon: Int
            val icons = arrayListOf<Int>()
            icons.add(R.mipmap.ic_needs_accordion)
            icons.add(R.mipmap.ic_needs_cactus)
            icons.add(R.mipmap.ic_needs_canjica)
            icons.add(R.mipmap.ic_needs_corn)
            icons.add(R.mipmap.ic_needs_flowers)
            icons.add(R.mipmap.ic_needs_hotdog)
            icons.add(R.mipmap.ic_needs_kite)
            icons.add(R.mipmap.ic_needs_maracas)
            icons.add(R.mipmap.ic_needs_patch)
            icons.add(R.mipmap.ic_needs_popcorn)
            icons.add(R.mipmap.ic_needs_sunflower)
            icons.add(R.mipmap.ic_needs_ukulele)

            val random = Random()
            icon = icons[random.nextInt(icons.size)]
            if (arrayListIconUsed.isNotEmpty()) {
                for (i in 0 until arrayListIconUsed.size) {
                    if (icon == arrayListIconUsed[i]) {
                        return randomIcon(context, arrayListIconUsed)
                    }
                }
            }
            arrayListIconUsed.add(icon)
            return icon
        }
        @SuppressLint("NewApi", "ResourceType")
        fun border(context: Context, view: View, color: Int) {
            val border = GradientDrawable()
            border.setStroke(5, context.resources.getColor(color, null))
            view.background = border
        }
    }
}