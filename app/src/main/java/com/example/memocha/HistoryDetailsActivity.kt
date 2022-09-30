package com.example.memocha

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memocha.adapter.HistoryDetailAdapter
import com.example.memocha.entity.History
import com.example.memocha.lib.HistoryDetailsEditor
import com.example.memocha.lib.MemoChaPopupDialog
import com.example.memocha.utility.AppUtil
import com.example.memocha.utility.MemoChaRoomDatabase
import kotlinx.android.synthetic.main.activity_history_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger

@SuppressLint("UseCompatLoadingForDrawables", "ClickableViewAccessibility")
class HistoryDetailsActivity : AppCompatActivity() {

    companion object {
        const val TAG = "HistoryDetailsAct"
    }
    private val database by lazy { MemoChaRoomDatabase(this) }
    private var historyDetailsEditor: HistoryDetailsEditor? = null

    private lateinit var month: String
    private lateinit var year: String
    private lateinit var history: List<History>
    private lateinit var historyDetailAdapter: HistoryDetailAdapter

    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_details)

        init()
        initListener()
        fetchHistoryDetails()
    }

    private fun init() {
        month = intent.extras?.getString("month").toString()
        year = intent.extras?.getString("year").toString()
        rootHistory.viewTreeObserver.addOnGlobalLayoutListener {
            if (!AppUtil.isKeyboardVisible(rootHistory)) editTextSearch.isFocusable = false
        }

        textViewMonthName.text = AppUtil.convertMonthNameFromCode(this, month)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textViewAmount,
                1, 26, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textViewImport,
                1, 12, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textViewExport,
                1, 12, 1, TypedValue.COMPLEX_UNIT_SP)
        setupRecyclerView()
    }

    private fun initListener() {
        imageMenu.setOnClickListener {
            startActivity(Intent(this@HistoryDetailsActivity, HistoryActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }
        imageImport.setOnClickListener {
            val mcPopupDialog = MemoChaPopupDialog.newInstance()
            mcPopupDialog.setContent(getString(R.string.dialog_title_information), getString(R.string.dialog_message_import_history),
                    getString(R.string.button_yes), getString(R.string.button_cancel), object : MemoChaPopupDialog.MemoChaPopupDialogListener {
                        override fun onNegativeButton() {

                        }
                        override fun onPositiveButton() {}
                    })
            mcPopupDialog.show(supportFragmentManager, mcPopupDialog.tag)
        }
        imageExport.setOnClickListener {
            val mcPopupDialog = MemoChaPopupDialog.newInstance()
            mcPopupDialog.setContent(String.format(getString(R.string.dialog_title_export_history),
                    AppUtil.convertMonthNameFromCode(this@HistoryDetailsActivity, month)),
                    getString(R.string.dialog_message_export_history), getString(R.string.button_yes), getString(R.string.button_cancel),
                    object : MemoChaPopupDialog.MemoChaPopupDialogListener {
                        override fun onNegativeButton() {

                        }
                        override fun onPositiveButton() {}
                    })
            mcPopupDialog.show(supportFragmentManager, mcPopupDialog.tag)
        }
        editTextSearch.setOnTouchListener { _, _ ->
            editTextSearch.isFocusableInTouchMode = true
            false
        }
        historyDetailAdapter.addOnHistoryDetailListener(
                object : HistoryDetailAdapter.HistoryDetailListener {
                    override fun onItemExpensesClicked(history: History) {
                        showEditor(history)
                    }
                }
        )
    }

    private fun setupRecyclerView() {
        historyDetailAdapter = HistoryDetailAdapter(this, arrayListOf())
        recyclerviewPaymentDetail.apply {
            layoutManager = LinearLayoutManager(this@HistoryDetailsActivity)
            adapter = historyDetailAdapter
        }
    }

    private fun fetchHistoryDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            history = database.historyDao().getHistory(year, month)
            if (history.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    historyDetailAdapter.setData(history)
                    runOnUiThread {
                        enableExportButton()
                        disableImportButton()
                        var sum = BigInteger("0")
                        for (i in history.indices) {
                            val amount: BigInteger = history[i].amount.replace(",", "").toBigInteger()
                            sum += amount
                        }
                        val summary = "Rp ${String.format("%,d", sum)}"
                        textViewAmount.text = summary.replace(",", ".")
                        textViewHistoryEmpty.visibility = View.GONE
                    }
                }
            } else {
                runOnUiThread {
                    disableExportButton()
                    enableImportButton()
                    val zero = "Rp 0"
                    textViewAmount.text = zero
                    editTextSearch.background =
                        resources.getDrawable(R.drawable.background_edit_text_search_disabled, null)
                    editTextSearch.isEnabled = false
                    editTextSearch.isFocusable = false
                    textViewHistoryEmpty.visibility = View.VISIBLE
                    cardViewPaymentDetail.visibility = View.GONE
                }
            }
        }
    }

    private fun disableExportButton() {
        imageExport.background =
                resources.getDrawable(R.drawable.background_button_export_circle_pressed, null)
        imageExport.isEnabled = false
        imageExport.isFocusable = false
        imageExport.isClickable = false
        imageExport.setImageResource(R.drawable.ic_export_white)
        TextViewCompat.setTextAppearance(textViewExport, R.style.TextRegularGrey12)
    }

    private fun disableImportButton() {
        imageImport.background =
                resources.getDrawable(R.drawable.background_button_export_circle_pressed, null)
        imageImport.isEnabled = false
        imageImport.isFocusable = false
        imageImport.isClickable = false
        imageImport.setImageResource(R.drawable.ic_import_white)
        TextViewCompat.setTextAppearance(textViewImport, R.style.TextRegularGrey12)
    }

    private fun enableExportButton() {
        imageExport.background =
                resources.getDrawable(R.drawable.selector_button_export, null)
        imageExport.isEnabled = true
        imageExport.isFocusable = true
        imageExport.isClickable = true
        imageExport.setImageResource(R.drawable.ic_export)
        TextViewCompat.setTextAppearance(textViewExport, R.style.TextRegularWhite12)
    }

    private fun enableImportButton() {
        imageImport.background =
                resources.getDrawable(R.drawable.selector_button_export, null)
        imageImport.isEnabled = true
        imageImport.isFocusable = true
        imageImport.isClickable = true
        imageImport.setImageResource(R.drawable.ic_import_teal_dark)
        TextViewCompat.setTextAppearance(textViewImport, R.style.TextRegularWhite12)
    }

    private fun showEditor(history: History) {
        isEditing = true
        historyDetailsEditor = HistoryDetailsEditor(this@HistoryDetailsActivity, history, supportFragmentManager)
                .addMonthlyExpensesEditorListener(object : HistoryDetailsEditor.MonthlyExpensesEditorListener {
                    override fun onSaveClicked(id: Int, history: History) {
                        updateHistory(id, history)
                        closeEditorLayout()
                    }
                    override fun onDeleteClicked(id: Int) {
                        deleteFromHistory(id)
                        closeEditorLayout()
                    }
                })
        layoutHistoryDetailsEditor.removeAllViews()
        layoutHistoryDetailsEditor.addView(historyDetailsEditor)
        layoutHistoryDetailsEditor.visibility = View.VISIBLE
        layoutHistoryDetailsEditor.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
        layoutHistoryDetailsEditor.setOnClickListener {
            AppUtil.hideSoftKeyboard(layoutHistoryDetailsEditor, this@HistoryDetailsActivity)
        }
    }

    private fun closeEditorLayout() {
        isEditing = false
        layoutHistoryDetailsEditor.visibility = View.GONE
        layoutHistoryDetailsEditor.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out))
        historyDetailsEditor = null
    }

    private fun updateHistory(id: Int, history: History) {
        CoroutineScope(Dispatchers.IO).launch {
            database.historyDao().updateHistory(
                id,
                history.yearEdited,
                history.monthEdited,
                history.dateEdited,
                history.timeEdited,
                history.goods,
                history.amount,
                history.description,
                history.category,
                history.method
            )
            fetchHistoryDetails()
        }
    }

    private fun deleteFromHistory(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            database.historyDao().deleteFromHistory(id)
            fetchHistoryDetails()
        }
    }

    override fun onBackPressed() {
        if (isEditing) {
            closeEditorLayout()
        } else {
            startActivity(Intent(this@HistoryDetailsActivity, DashboardActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }
    }
}