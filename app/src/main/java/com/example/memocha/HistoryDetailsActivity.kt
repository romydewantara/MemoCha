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
import com.example.memocha.lib.MonthlyExpensesEditor
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
        const val TAG = "HistoryDetailsActivity"
    }
    private val database by lazy { MemoChaRoomDatabase(this) }
    private var monthlyExpensesEditor: MonthlyExpensesEditor? = null

    private lateinit var month: String
    private lateinit var year: String
    private lateinit var history: List<History>
    private lateinit var historyDetailAdapter: HistoryDetailAdapter

    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_details)
        month = intent.extras?.getString("month").toString()
        year = intent.extras?.getString("year").toString()
        rootHistory.viewTreeObserver.addOnGlobalLayoutListener {
            if (!AppUtil.isKeyboardVisible(rootHistory)) editTextSearch.isFocusable = false
        }

        textViewMonthName.text = AppUtil.convertMonthNameFromCode(this, month)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            textViewAmount, 1, 26, 1, TypedValue.COMPLEX_UNIT_SP)

        setupRecyclerView()
        init()
        initListener()
    }

    private fun init() {
        CoroutineScope(Dispatchers.IO).launch {
            history = database.historyDao().getHistory(year, month)
            Log.d(TAG, "getHistoryDetails - histories: $history")
            if (history.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    historyDetailAdapter.setData(history)
                    runOnUiThread {
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
                    layoutExport.background =
                        resources.getDrawable(R.drawable.background_text_export_disabled, null)
                    layoutExport.isEnabled = false
                    layoutExport.isFocusable = false
                    layoutExport.isClickable = false
                    iconExport.setImageResource(R.drawable.ic_export_gray)
                    TextViewCompat.setTextAppearance(textViewExport, R.style.TextRegularGrey14)

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
            init()
        }
    }

    private fun deleteFromHistory(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            database.historyDao().deleteFromHistory(id)
            init()
        }
    }

    private fun initListener() {
        historyDetailAdapter.addOnHistoryDetailListener(
            object : HistoryDetailAdapter.HistoryDetailListener {
                override fun onItemExpensesClicked(history: History) {
                    showEditor(history)
                }
            }
        )
        imageMenu.setOnClickListener {
            startActivity(Intent(this@HistoryDetailsActivity, HistoryActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }
        editTextSearch.setOnTouchListener { _, _ ->
            editTextSearch.isFocusableInTouchMode = true
            false
        }
        layoutExport.setOnClickListener {
            //preview bottom sheet
        }
    }

    private fun showEditor(history: History) {
        isEditing = true
        monthlyExpensesEditor = MonthlyExpensesEditor(this@HistoryDetailsActivity, history, supportFragmentManager)
            .addMonthlyExpensesEditorListener(object : MonthlyExpensesEditor.MonthlyExpensesEditorListener {
                override fun onSaveClicked(id: Int, history: History) {
                    updateHistory(id, history)
                    closeEditorLayout()
                }
                override fun onDeleteClicked(id: Int) {
                    deleteFromHistory(id)
                    closeEditorLayout()
                }
            })
        layoutMonthlyExpensesEditor.removeAllViews()
        layoutMonthlyExpensesEditor.addView(monthlyExpensesEditor)
        layoutMonthlyExpensesEditor.visibility = View.VISIBLE
        layoutMonthlyExpensesEditor.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
        layoutMonthlyExpensesEditor.setOnClickListener {
            AppUtil.hideSoftKeyboard(layoutMonthlyExpensesEditor, this@HistoryDetailsActivity)
        }
    }

    private fun setupRecyclerView() {
        historyDetailAdapter = HistoryDetailAdapter(this, arrayListOf())
        recyclerviewPaymentDetail.apply {
            layoutManager = LinearLayoutManager(this@HistoryDetailsActivity)
            adapter = historyDetailAdapter
        }
    }

    private fun closeEditorLayout() {
        isEditing = false
        layoutMonthlyExpensesEditor.visibility = View.GONE
        layoutMonthlyExpensesEditor.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out))
        monthlyExpensesEditor = null
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