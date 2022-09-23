package com.example.kuntan

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kuntan.adapter.HistoryDetailAdapter
import com.example.kuntan.entity.History
import com.example.kuntan.lib.MonthlyExpensesEditor
import com.example.kuntan.utility.AppUtil
import com.example.kuntan.utility.KuntanRoomDatabase
import kotlinx.android.synthetic.main.activity_history_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("UseCompatLoadingForDrawables", "ClickableViewAccessibility")
class HistoryDetailsActivity : AppCompatActivity() {

    companion object {
        const val TAG = "HistoryDetailsActivity"
    }
    private val database by lazy { KuntanRoomDatabase(this) }
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
                        var sum = 0
                        for (i in history.indices) {
                            val amount = history[i].amount.replace(",", "")
                            if (amount.isNotEmpty()) sum += amount.toInt()
                        }
                        val summary = "Rp ${String.format("%,d", sum)}"
                        textViewAmount.text = summary.replace(",", ".")
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

                    editTextSearch.background =
                        resources.getDrawable(R.drawable.background_edit_text_search_disabled, null)
                    editTextSearch.isEnabled = false
                    editTextSearch.isFocusable = false

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
            startActivity(Intent(this@HistoryDetailsActivity, HistoryActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }
    }
}