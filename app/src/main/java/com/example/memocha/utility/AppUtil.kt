package com.example.memocha.utility

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.memocha.R
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Random
import kotlin.collections.ArrayList

@SuppressLint("NewApi", "ResourceType", "SimpleDateFormat")
class AppUtil {
    companion object {
        fun writeFileToStorage(c: Context, folderName: String, fileName: String, body: String) : String {
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
                return dir.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
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
        fun readTextFromAssets(context: Context, fileName: String): String {
            val inputStream: InputStream
            return try {
                inputStream = context.assets.open(fileName)
                val size: Int = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()

                // byte buffer into a string
                String(buffer, charset("UTF-8"))
            } catch (e: IOException) {
                null.toString()
            }
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
        fun getListOfMonth(c: Context) : ArrayList<String> {
            return arrayListOf(
                c.getString(R.string.month_january), c.getString(R.string.month_february),
                c.getString(R.string.month_march), c.getString(R.string.month_april),
                c.getString(R.string.month_may), c.getString(R.string.month_june),
                c.getString(R.string.month_july), c.getString(R.string.month_august),
                c.getString(R.string.month_september), c.getString(R.string.month_october),
                c.getString(R.string.month_november), c.getString(R.string.month_december)
            )
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
        fun getTimesName(c: Context): String {
            val sdf = SimpleDateFormat("HH:mm").format(Calendar.getInstance().time).toString()
            return when (sdf.split(":")[0].toInt()) {
                in 0..8 -> { c.getString(R.string.dasboard_time_morning) }
                in 9..14 -> { c.getString(R.string.dasboard_time_afternoon) }
                in 15..18 -> { c.getString(R.string.dasboard_time_evening) }
                else -> c.getString(R.string.dasboard_time_night)
            }
        }
        fun randomIcon(context: Context, arrayListIconUsed: ArrayList<Int>): Int {
            val icon: Int
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
        fun randomWisdom(c: Context, arrayListWisdomUsed: ArrayList<String>): String {
            val wisdom: String
            val listOfWisdom = arrayListOf<String>()
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_index_0))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_index_1))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_index_2))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_index_3))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_index_4))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_index_5))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_index_6))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_index_7))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_index_8))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_index_9))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_1))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_2))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_3))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_4))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_5))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_6))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_7))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_8))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_9))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_10))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_11))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_12))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_13))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_14))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_15))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_16))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_17))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_18))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_19))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_20))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_21))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_22))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_23))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_24))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_25))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_26))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_27))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_28))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_29))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_30))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_31))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_32))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_33))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_34))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_35))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_36))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_37))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_38))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_39))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_40))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_41))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_42))
            listOfWisdom.add(c.getString(R.string.dashboard_wisdom_43))

            val random = Random()
            wisdom = listOfWisdom[random.nextInt(listOfWisdom.size)]
            if (arrayListWisdomUsed.size == listOfWisdom.size) arrayListWisdomUsed.clear()
            if (arrayListWisdomUsed.isNotEmpty()) {
                for (i in 0 until arrayListWisdomUsed.size) {
                    if (wisdom == arrayListWisdomUsed[i]) {
                        return randomWisdom(c, arrayListWisdomUsed)
                    }
                }
            }
            arrayListWisdomUsed.add(wisdom)
            return wisdom
        }
        fun border(context: Context, view: View, color: Int) {
            val border = GradientDrawable()
            border.setStroke(5, context.resources.getColor(color, null))
            view.background = border
        }
    }
}