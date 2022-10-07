package com.example.memocha.lib

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentManager
import com.example.memocha.R
import com.example.memocha.adapter.CategoryAdapter
import com.example.memocha.adapter.PaymentMethodSpinnerAdapter
import com.example.memocha.entity.History
import com.example.memocha.utility.AppUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.layout_monthly_expenses_notes.view.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@SuppressLint("ViewConstructor", "UseCompatLoadingForDrawables", "ClickableViewAccessibility", "NewApi", "SimpleDateFormat", "SetTextI18n")
class HistoryDetailsEditor(
    context: Context, private val history: History, private val fragmentManager: FragmentManager
): RelativeLayout(context) {

    init {
        onCreateView()
    }

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
    private lateinit var paymentMethodSpinnerAdapter: PaymentMethodSpinnerAdapter
    private lateinit var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener

    private fun onCreateView() {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_monthly_expenses_notes, this, false)
        view.measure(view.width, view.height)
        addView(view)

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                globalLayoutListener = this

                view.y = (this@HistoryDetailsEditor.height - view.measuredHeight).toFloat()
                if (isInit) {
                    editTextGoods.requestFocus()
                    editTextGoods.setSelection(history.goods.length)
                    AppUtil.showSoftKeyboard(editTextGoods, context)
                    isInit = false
                }
                if (!AppUtil.isKeyboardVisible(this@HistoryDetailsEditor)) {
                    editTextGoods.isFocusable = false
                    editTextAmount.isFocusable = false
                    editTextNote.isFocusable = false
                }
            }
        })

        val sortType = arrayListOf(context.getString(R.string.method_cash), context.getString(R.string.method_debit), context.getString(R.string.method_transfer))
        paymentMethodSpinnerAdapter = PaymentMethodSpinnerAdapter(context, sortType)
        layoutPaymentMethod.adapter = paymentMethodSpinnerAdapter
        layoutPaymentMethod.setSelection(AppUtil.convertPaymentMethodFromType(context, history.method))
        initListener()
    }

    private fun initListener() {
        layoutCalendar.visibility = GONE
        textViewNotes.text = context.getString(R.string.history_edit_monthly_expenses)
        imageAdd.setImageResource(R.drawable.ic_save)
        imageAdd.background = context.resources.getDrawable(R.drawable.background_button_send_disabled, null)
        textViewCategory.text = history.category
        editTextGoods.setText(history.goods)
        editTextAmount.setText("Rp ${history.amount}")
        editTextNote.setText(history.description)
        imageDelete.visibility = View.VISIBLE
        imageDelete.isEnabled = true

        imageDelete.setOnClickListener {
            val mcPopupDialog = MemoChaPopupDialog.newInstance().setContent(context.getString(R.string.dialog_title_delete_from_history),
                context.getString(R.string.dialog_message_delete_from_history), context.getString(R.string.button_ok), context.getString(R.string.button_cancel),
                object : MemoChaPopupDialog.MemoChaPopupDialogListener {
                    override fun onNegativeButton() {
                        monthlyExpensesEditorListener.onDeleteClicked(history.id)
                        Snackbar.make(this@HistoryDetailsEditor, context.getString(R.string.snackbar_monthly_expenses_deleted),
                            Snackbar.LENGTH_SHORT).setAction(context.getString(R.string.snackbar_button_dismiss)) {}.show()
                    }
                    override fun onPositiveButton() {}
                })
            mcPopupDialog.show(fragmentManager, mcPopupDialog.tag)
            AppUtil.hideSoftKeyboard(this, context)
        }
        imageAdd.setOnClickListener {
            AppUtil.hideSoftKeyboard(this, context)
            (mContext as Activity).runOnUiThread {
                val currentDate = SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().time)
                val currentTime = SimpleDateFormat("HH:mm").format(Date())
                history.goods = editTextGoods.text.toString().trim()
                history.amount = editTextAmount.text.toString().replace("Rp ", "").trim()
                history.description = editTextNote.text.toString().trim()
                history.category = textViewCategory.text.toString()
                history.method = paymentMethod
                history.yearEdited = currentDate.split("-")[2]
                history.monthEdited = currentDate.split("-")[1]
                history.dateEdited = currentDate.split("-")[0]
                history.timeEdited = currentTime

                textViewCategory.text = context.getString(R.string.category_others)
                layoutPaymentMethod.setSelection(0)
                Snackbar.make(this@HistoryDetailsEditor, context.getString(R.string.snackbar_monthly_expenses_updated), Snackbar.LENGTH_SHORT)
                    .setAction(context.getString(R.string.snackbar_button_dismiss)) {}.show()
                monthlyExpensesEditorListener.onSaveClicked(history.id, history)
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
        editTextGoods.addTextChangedListener(onTextChangedListener())
        editTextGoods.setOnTouchListener { _, _ ->
            editTextGoods.isFocusableInTouchMode = true
            false
        }
        editTextAmount.addTextChangedListener(HistoryEditorMoneyTextWatcher(context, editTextAmount))
        editTextAmount.setOnTouchListener { _, _ ->
            editTextAmount.isFocusableInTouchMode = true
            false
        }
        editTextNote.addTextChangedListener(onTextChangedListener())
        editTextNote.setOnTouchListener { _, _ ->
            editTextNote.isFocusableInTouchMode = true
            false
        }
        paymentMethodSpinnerAdapter.addOnPaymentMethodListener(
            object : PaymentMethodSpinnerAdapter.ItemSelectedListener {
            override fun onItemSelected(selectedItem: String) {
                paymentMethod = selectedItem
                isPaymentMethodChanged = selectedItem != history.method
                setSaveButtonEnable(isGoodsChanged || isAmountChanged || isNoteChanged || isCategoryChanged || isPaymentMethodChanged)
            }
        })
    }

    private fun onTextChangedListener() : TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkSaveButtonEnable()
            }
        }
    }

    fun checkSaveButtonEnable() {
        val goodsValue = editTextGoods.text.toString().trim()
        val amountValue = editTextAmount.text.toString().replace("Rp ", "").trim()
        val noteValue = editTextNote.text.toString().trim()
        isGoodsChanged = goodsValue != history.goods
        isAmountChanged = amountValue != history.amount
        isNoteChanged = noteValue != history.description
        setSaveButtonEnable((isGoodsChanged || isAmountChanged || isNoteChanged) && (goodsValue.isNotEmpty() && amountValue.isNotEmpty()))
    }

    private fun setSaveButtonEnable(isEnable: Boolean) {
        if (isEnable) {
            imageAdd.isEnabled = true
            imageAdd.background = context.resources.getDrawable(R.drawable.selector_button_send_needs_item, null)
        } else {
            imageAdd.isEnabled = false
            imageAdd.background = context.resources.getDrawable(R.drawable.background_button_send_disabled, null)
        }
    }

    fun addMonthlyExpensesEditorListener(monthlyExpensesEditorListener: MonthlyExpensesEditorListener): HistoryDetailsEditor {
        this.monthlyExpensesEditorListener = monthlyExpensesEditorListener
        return this@HistoryDetailsEditor
    }

    interface MonthlyExpensesEditorListener {
        fun onSaveClicked(id: Int, history: History)
        fun onDeleteClicked(id: Int)
    }
}