package com.example.kuntan.lib

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentManager
import com.example.kuntan.R
import com.example.kuntan.adapter.CategoryAdapter
import com.example.kuntan.adapter.PaymentMethodSpinnerAdapter
import com.example.kuntan.entity.History
import com.example.kuntan.utility.AppUtil
import com.example.kuntan.utility.KuntanRoomDatabase
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.layout_monthly_expenses_notes.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("ViewConstructor")
class MonthlyExpensesEditor(
    context: Context, private val history: History, private val fragmentManager: FragmentManager
): RelativeLayout(context) {

    companion object {
        const val EDIT_TEXT_GOODS = "GOODS"
        const val EDIT_TEXT_AMOUNT = "AMOUNT"
        const val EDIT_TEXT_NOTE = "NOTE"
    }

    init {
        init()
    }

    private val database by lazy { KuntanRoomDatabase(context) }
    private val mContext = context
    private var isInit = true
    private var isCategoryChanged = false
    private var isPaymentMethodChanged = false
    private var isGoodsChanged = false
    private var isAmountChanged = false
    private var isNoteChanged = false

    private lateinit var monthlyExpensesEditorListener: MonthlyExpensesEditorListener
    private lateinit var selectorItemsBottomSheet: SelectorItemsBottomSheet
    private lateinit var paymentMethod: String

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun init() {
        val monthlyExpensesLayoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams = monthlyExpensesLayoutParams
        background = context.resources.getDrawable(R.drawable.background_monthly_expenses_editor_transparent, null)
        setOnClickListener { AppUtil.hideSoftKeyboard(this@MonthlyExpensesEditor, context) }

        val view = LayoutInflater.from(context).inflate(R.layout.layout_monthly_expenses_notes, this, false)
        view.measure(view.width, view.height)

        viewTreeObserver.addOnGlobalLayoutListener {
            view.y = (this@MonthlyExpensesEditor.height - view.measuredHeight).toFloat()
            if (isInit) {
                editTextGoods.requestFocus()
                editTextGoods.setSelection(history.goods.length)
                AppUtil.showSoftKeyboard(editTextGoods, context)
                isInit = false
            }
        }

        addView(view)
        initListener()
    }

    @SuppressLint("ClickableViewAccessibility", "SimpleDateFormat", "UseCompatLoadingForDrawables")
    private fun initListener() {
        textViewNotes.text = context.getString(R.string.history_edit_monthly_expenses)
        imageAdd.setImageResource(R.drawable.ic_save)
        imageAdd.background = context.resources.getDrawable(R.drawable.background_button_send_disabled, null)
        textViewCategory.text = history.category
        editTextGoods.setText(history.goods)
        editTextAmount.setText(history.amount)
        editTextNote.setText(history.description)
        imageDelete.visibility = View.VISIBLE
        imageDelete.isEnabled = true

        imageDelete.setOnClickListener {
            val kuntanPopupDialog = KuntanPopupDialog.newInstance().setContent(context.getString(R.string.dialog_title_delete_from_history),
                context.getString(R.string.dialog_message_delete_from_history), "OK", context.getString(R.string.button_cancel), object : KuntanPopupDialog.KuntanPopupDialogListener {
                    override fun onNegativeButton() {
                        CoroutineScope(Dispatchers.IO).launch {
                            database.historyDao().deleteSchedule(history.id)
                            (mContext as Activity).runOnUiThread {
                                monthlyExpensesEditorListener.onDeleteClicked(history.id)
                                Snackbar.make(this@MonthlyExpensesEditor, context.getString(R.string.snackbar_monthly_expenses_deleted),
                                    Snackbar.LENGTH_LONG).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).setAction("DISMISS") {}.show()
                            }
                        }
                    }
                    override fun onPositiveButton() {}
                })
            kuntanPopupDialog.show(fragmentManager, kuntanPopupDialog.tag)
            AppUtil.hideSoftKeyboard(this, context)
        }
        imageAdd.setOnClickListener {
            AppUtil.hideSoftKeyboard(this, context)
            CoroutineScope(Dispatchers.IO).launch {
                val currentDate = SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().time)
                val currentTime = SimpleDateFormat("HH:mm").format(Date())
                database.historyDao().updateHistory(history.id, currentDate.split("-")[2],
                    currentDate.split("-")[1], currentDate.split("-")[0],
                    currentTime, editTextGoods.text.toString().trim(), editTextAmount.text.toString().trim(),
                    editTextNote.text.toString().trim(), textViewCategory.text.toString(), paymentMethod)

                (mContext as Activity).runOnUiThread {
                    history.goods = editTextGoods.text.toString().trim()
                    history.amount = editTextAmount.text.toString().trim()
                    history.description = editTextNote.text.toString().trim()
                    history.category = textViewCategory.text.toString()
                    history.method = paymentMethod
                    history.yearEdited = currentDate.split("-")[2]
                    history.monthEdited = currentDate.split("-")[1]
                    history.dateEdited = currentDate.split("-")[0]
                    history.timeEdited = currentTime

                    textViewCategory.text = context.getString(R.string.category_others)
                    layoutPaymentMethod.setSelection(0)
                    Snackbar.make(this@MonthlyExpensesEditor, context.getString(R.string.snackbar_monthly_expenses_updated),
                        Snackbar.LENGTH_LONG).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).setAction("DISMISS") {}.show()
                    monthlyExpensesEditorListener.onSaveClicked(history.id, history)
                }
            }
        }
        textViewCategoryTitle.setOnClickListener {
            selectorItemsBottomSheet = SelectorItemsBottomSheet(context.getString(R.string.category_title),
                CategoryAdapter(context, object : CategoryAdapter.CategoryAdapterListener {
                    override fun onCategoryClicked(category: String) {
                        selectorItemsBottomSheet.dismiss()
                        textViewCategory.text = category
                        isCategoryChanged = category != history.category
                        setSaveButtonEnable(isGoodsChanged || isAmountChanged || isNoteChanged || isCategoryChanged || isPaymentMethodChanged)
                    }
                })
            )
            selectorItemsBottomSheet.show(fragmentManager, "selector_bottom_sheet")
        }
        editTextGoods.addTextChangedListener(onTextChangedListener(EDIT_TEXT_GOODS))
        editTextGoods.setOnTouchListener { _, _ ->
            editTextGoods.isFocusableInTouchMode = true
            false
        }
        editTextAmount.addTextChangedListener(onTextChangedListener(EDIT_TEXT_AMOUNT))
        editTextAmount.setOnTouchListener { _, _ ->
            editTextAmount.isFocusableInTouchMode = true
            false
        }
        editTextNote.addTextChangedListener(onTextChangedListener(EDIT_TEXT_NOTE))
        editTextNote.setOnTouchListener { _, _ ->
            editTextNote.isFocusableInTouchMode = true
            false
        }

        val sortType = arrayListOf(context.getString(R.string.method_cash), context.getString(R.string.method_debit), context.getString(R.string.method_transfer))
        val spinnerPaymentMethodAdapter = PaymentMethodSpinnerAdapter(context,
            sortType, object : PaymentMethodSpinnerAdapter.ItemSelectedListener {
                override fun onItemSelected(selectedItem: String) {
                    paymentMethod = selectedItem
                    isPaymentMethodChanged = selectedItem != history.method
                    setSaveButtonEnable(isGoodsChanged || isAmountChanged || isNoteChanged || isCategoryChanged || isPaymentMethodChanged)
                }
            })
        layoutPaymentMethod.adapter = spinnerPaymentMethodAdapter
        layoutPaymentMethod.setSelection(AppUtil.convertPaymentMethodFromType(context, history.method))
    }

    private fun onTextChangedListener(type: String) : TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                when(type) {
                    EDIT_TEXT_GOODS -> {

                    }
                    EDIT_TEXT_AMOUNT -> {
                        editTextAmount.removeTextChangedListener(this)
                        try {
                            var originalString = s.toString()
                            if (originalString.contains(",")) originalString = originalString.replace(",", "")
                            val longValue: Long = java.lang.Long.parseLong(originalString)
                            val formatter = NumberFormat.getInstance(Locale.US) as DecimalFormat
                            formatter.applyPattern("#,###,###,###")
                            val formattedString = formatter.format(longValue)
                            editTextAmount.setText(formattedString)
                            editTextAmount.setSelection(editTextAmount.text.length)

                        } catch (nfe: NumberFormatException) {
                            nfe.printStackTrace()
                        }
                        editTextAmount.addTextChangedListener(this)
                    }
                    EDIT_TEXT_NOTE -> {}
                }

                val goodsValue = editTextGoods.text.toString()
                val amountValue = editTextAmount.text.toString()
                val noteValue = editTextNote.text.toString()
                isGoodsChanged = goodsValue != history.goods
                isAmountChanged = amountValue != history.amount
                isNoteChanged = noteValue != history.description
                setSaveButtonEnable((isGoodsChanged || isAmountChanged || isNoteChanged) && (goodsValue.isNotEmpty() && amountValue.isNotEmpty()))
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setSaveButtonEnable(isEnable: Boolean) {
        if (isEnable) {
            imageAdd.isEnabled = true
            imageAdd.background = context.resources.getDrawable(R.drawable.selector_button_send_needs_item, null)
        } else {
            imageAdd.isEnabled = false
            imageAdd.background = context.resources.getDrawable(R.drawable.background_button_send_disabled, null)
        }
    }

    fun addMonthlyExpensesEditorListener(monthlyExpensesEditorListener: MonthlyExpensesEditorListener): MonthlyExpensesEditor {
        this.monthlyExpensesEditorListener = monthlyExpensesEditorListener
        return this@MonthlyExpensesEditor
    }

    interface MonthlyExpensesEditorListener {
        fun onSaveClicked(id: Int, history: History)
        fun onDeleteClicked(id: Int)
    }
}