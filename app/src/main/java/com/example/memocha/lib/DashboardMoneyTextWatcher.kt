package com.example.memocha.lib

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.example.memocha.DashboardActivity
import com.example.memocha.HistoryDetailsActivity
import com.example.memocha.utility.Constant
import java.lang.ref.WeakReference
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale
import java.util.Objects

class DashboardMoneyTextWatcher(val c: Context, editText: EditText) : TextWatcher {

    private var parent = c as DashboardActivity
    private val locale = Locale("id", Constant.APP_LANG_INDONESIA)
    private val formatter = NumberFormat.getCurrencyInstance(locale) as DecimalFormat
    private val editTextWeakReference: WeakReference<EditText> = WeakReference(editText)

    init {
        formatter.maximumFractionDigits = 0
        formatter.roundingMode = RoundingMode.FLOOR

        val symbol = DecimalFormatSymbols(locale)
        symbol.currencySymbol = "${symbol.currencySymbol} "
        formatter.decimalFormatSymbols = symbol
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        val editText = editTextWeakReference.get()
        if (editText == null || editText.text.toString().isEmpty()) {
            return
        }
        editText.removeTextChangedListener(this)

        val parsed = parseCurrencyValue(editText.text.toString())
        val formatted = if (parsed == "0".toBigDecimal()) "" else formatter.format(parsed)

        editText.setText(formatted)
        editText.setSelection(formatted.length)
        editText.addTextChangedListener(this)
        parent.checkSendButtonEnable()
    }

    fun parseCurrencyValue(value: String) : BigDecimal {
        try {
            val replaceRegex = String.format("[%s,.\\s]", Objects.requireNonNull(formatter.currency).getSymbol(locale))
            val currencyValue: String = value.replace(replaceRegex.toRegex(), "")
            return BigDecimal(currencyValue)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return BigDecimal.ZERO
    }
}