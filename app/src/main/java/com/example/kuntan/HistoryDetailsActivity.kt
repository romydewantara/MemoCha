package com.example.kuntan

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kuntan.adapter.HistoryDetailAdapter
import com.example.kuntan.entity.History
import com.example.kuntan.utility.AppUtil
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_history_details.*
import org.json.JSONArray

class HistoryDetailsActivity : AppCompatActivity(), HistoryDetailAdapter.HistoryDetailListener {

    private val TAG = "HistoryDetailsActivity"
    private var histories = ArrayList<History>()
    private lateinit var historyDetailAdapter: HistoryDetailAdapter

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

    private fun init() {
        val month = intent.extras?.getString("month").toString()
        val history = intent.extras?.getString("history").toString()

        textViewMonthName.text = AppUtil.convertMonthNameFromCode(applicationContext, month)
        val array = JSONArray(history)
        for (i in 0 until array.length()) {
            val jsonObject = array.getJSONObject(i)
            histories.add(Gson().fromJson(jsonObject.toString(), History::class.java))
        }

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

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        editTextSearch.setOnTouchListener { _, _ ->
            editTextSearch.isFocusableInTouchMode = true
            false
        }
    }

    private fun setupRecyclerView() {
        histories.reverse()
        historyDetailAdapter = HistoryDetailAdapter(applicationContext, histories, this)
        recyclerviewPaymentDetail.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = historyDetailAdapter
        }
    }

    override fun onItemExpensesClicked(history: History) {
        Log.d("HDA", "onItemExpensesClicked - item selected: ${Gson().toJson(history)}")
    }
}