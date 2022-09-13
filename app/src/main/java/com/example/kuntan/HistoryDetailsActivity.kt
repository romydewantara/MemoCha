package com.example.kuntan

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kuntan.adapter.ExpensesDetailAdapter
import com.example.kuntan.utility.AppUtil
import com.example.kuntan.utility.KuntanRoomDatabase
import kotlinx.android.synthetic.main.activity_history_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryDetailsActivity : AppCompatActivity() {

    private val database by lazy { KuntanRoomDatabase(this) }
    private lateinit var month: String
    private lateinit var historyAdapter: ExpensesDetailAdapter
    private var isHistoryEmpty = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_details)
        month = intent.getStringExtra("month")!!

        rootHistory.viewTreeObserver.addOnGlobalLayoutListener {
            if (!AppUtil.isKeyboardVisible(rootHistory)) editTextSearch.isFocusable = false
        }

        setupRecyclerView()
        initListener()
    }

    override fun onStart() {
        super.onStart()
        refreshHistories()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        editTextSearch.setOnTouchListener { _, _ ->
            editTextSearch.isFocusableInTouchMode = true
            false
        }
    }

    private fun refreshHistories() {
        CoroutineScope(Dispatchers.IO).launch {
            val history = database.historyDao().getHistory(month)
            isHistoryEmpty = history.isEmpty()
            runOnUiThread {
                textViewMonthName.text = month
                //if (isHistoryEmpty) textViewEmpty.visibility = View.VISIBLE  else textViewEmpty.visibility = View.GONE
            }
            withContext(Dispatchers.Main) {
                historyAdapter.setData(history)
            }
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = ExpensesDetailAdapter(applicationContext, arrayListOf())
        recyclerviewPaymentDetail.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = historyAdapter
        }
    }
}