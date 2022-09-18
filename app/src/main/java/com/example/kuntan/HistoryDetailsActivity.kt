package com.example.kuntan

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kuntan.adapter.HistoryDetailAdapter
import com.example.kuntan.entity.History
import com.example.kuntan.lib.MonthlyExpensesEditor
import com.example.kuntan.utility.AppUtil
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_history_details.*
import org.json.JSONArray

class HistoryDetailsActivity : AppCompatActivity(), HistoryDetailAdapter.HistoryDetailListener {

    private val TAG = "HistoryDetailsActivity"
    private var histories = ArrayList<History>()
    private lateinit var historyDetailAdapter: HistoryDetailAdapter
    private var monthlyExpensesEditor: MonthlyExpensesEditor? = null
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_details)
        rootHistory.viewTreeObserver.addOnGlobalLayoutListener {
            if (!AppUtil.isKeyboardVisible(rootHistory)) editTextSearch.isFocusable = false
        }

        init()
        setupRecyclerView()
        initListener()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun init() {
        val month = intent.extras?.getString("month").toString()
        val history = intent.extras?.getString("history").toString()

        textViewMonthName.text = AppUtil.convertMonthNameFromCode(applicationContext, month)
        val array = JSONArray(history)
        if (array.length() > 0) {
            for (i in 0 until array.length()) {
                val jsonObject = array.getJSONObject(i)
                histories.add(Gson().fromJson(jsonObject.toString(), History::class.java))
            }
        } else {
            layoutExport.background = resources.getDrawable(R.drawable.background_text_export_disabled, null)
            TextViewCompat.setTextAppearance(textViewExport, R.style.TextRegularGrey14)
            iconExport.setImageResource(R.drawable.ic_export_gray)
            layoutExport.isEnabled = false
            layoutExport.isFocusable = false
            layoutExport.isClickable = false
        }

        histories.reverse()
        refreshSummary()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        editTextSearch.setOnTouchListener { _, _ ->
            editTextSearch.isFocusableInTouchMode = true
            false
        }
        layoutExport.setOnClickListener {
            //preview bottom sheet

        }
    }

    private fun refreshSummary() {
        if (histories.isNotEmpty()) {
            var sum = 0
            for (i in histories.indices) {
                val amount = histories[i].amount.replace(",", "")
                if (amount.isNotEmpty()) sum += amount.toInt()
            }
            val summary = "Rp ${String.format("%,d", sum)}"
            textViewAmount.text = summary.replace(",", ".")
        }
    }

    private fun setupRecyclerView() {
        historyDetailAdapter = HistoryDetailAdapter(applicationContext, histories, this)
        recyclerviewPaymentDetail.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = historyDetailAdapter
        }
    }

    override fun onItemExpensesClicked(history: History) {
        isEditing = true
        monthlyExpensesEditor = MonthlyExpensesEditor(this@HistoryDetailsActivity, history, supportFragmentManager)
            .addMonthlyExpensesEditorListener(object : MonthlyExpensesEditor.MonthlyExpensesEditorListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onSaveClicked(id: Int, history: History) {
                    for (i in 0 until histories.size - 1) {
                        if (histories[i].id == id) {
                            histories[i] = history
                            break
                        }
                    }
                    refreshSummary()
                    setupRecyclerView()
                    onBackPressed()
                }

                override fun onDeleteClicked(id: Int) {
                    for (i in 0 until histories.size - 1) {
                        if (histories[i].id == id) {
                            histories.removeAt(i)
                            break
                        }
                    }
                    refreshSummary()
                    setupRecyclerView()
                    onBackPressed()
                }
            })
        layoutMonthlyExpensesEditor.removeAllViews()
        layoutMonthlyExpensesEditor.addView(monthlyExpensesEditor)
        layoutMonthlyExpensesEditor.visibility = View.VISIBLE
        layoutMonthlyExpensesEditor.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
    }

    override fun onBackPressed() {
        if (isEditing) {
            isEditing = false
            layoutMonthlyExpensesEditor.visibility = View.GONE
            layoutMonthlyExpensesEditor.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out))
            monthlyExpensesEditor = null
        } else {
            startActivity(Intent(this@HistoryDetailsActivity, HistoryActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }
    }
}