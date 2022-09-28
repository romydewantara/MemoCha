package com.example.kuntan

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kuntan.adapter.HistoryAdapter
import com.example.kuntan.utility.AppUtil
import com.example.kuntan.utility.KuntanRoomDatabase
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.collections.ArrayList

@SuppressLint("SimpleDateFormat", "UseCompatLoadingForDrawables")
class HistoryActivity : AppCompatActivity(), HistoryAdapter.HistoryAdapterListener {

    private val database by lazy { KuntanRoomDatabase(this) }
    private val currentMonthYear = SimpleDateFormat("MM-yyyy").format(Calendar.getInstance().time)
    private var year = currentMonthYear.split("-")[1]
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        setupRecyclerView()
        initListener()
    }

    override fun onStart() {
        super.onStart()
        fetchHistory()
        initPosition()
        slideUpHistoryLayout()
    }

    private fun initPosition() {
        val height = resources.displayMetrics.heightPixels
        layoutHistory.y = height.toFloat()
        imageRibbon.visibility = View.INVISIBLE
        previousYear.visibility = View.INVISIBLE
        textViewYear.visibility = View.INVISIBLE
        nextYear.visibility = View.INVISIBLE
        cardViewMonths.visibility = View.INVISIBLE
    }

    private fun initListener() {
        nextYear.setOnClickListener {
            year = (year.toInt() + 1).toString()
            fetchHistory()
        }
        previousYear.setOnClickListener {
            year = (year.toInt() - 1).toString()
            fetchHistory()
        }
    }

    private fun slideUpHistoryLayout() {
        Handler().postDelayed({
            val yTarget = rootHistory.y
            val objectAnimator =
                ObjectAnimator.ofFloat(layoutHistory, "translationY", yTarget)
                    .setDuration(300)
            val animatorSet = AnimatorSet()
            animatorSet.play(objectAnimator)
            animatorSet.start()
            animatorSet.addListener(object : Animator.AnimatorListener{
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    imageRibbon.visibility = View.VISIBLE
                    previousYear.visibility = View.VISIBLE
                    textViewYear.visibility = View.VISIBLE
                    nextYear.visibility = View.VISIBLE
                    cardViewMonths.visibility = View.VISIBLE
                    imageRibbon.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
                    previousYear.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
                    textViewYear.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
                    nextYear.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
                    cardViewMonths.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
                }
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            })
        }, 400)
    }

    private fun fetchHistory() {
        CoroutineScope(Dispatchers.IO).launch {
            val historyOfYear = database.historyDao().getHistoryByYear(year)
            val listOfTotal = mutableListOf<String>()
            for (i in 0 until AppUtil.getListOfMonth(this@HistoryActivity).size) {
                val arrayListOfTotal = arrayListOf<String>()
                for (j in historyOfYear.indices) {
                    if (historyOfYear[j].month == AppUtil.convertMonthCodeFromId(i) && historyOfYear[j].year == year) {
                        arrayListOfTotal.add(historyOfYear[j].amount)
                    }
                }
                val total = "Rp ${String.format("%,d", getMonthlyExpenseTotal(arrayListOfTotal))}".replace(",", ".")
                listOfTotal.add(total)
            }
            withContext(Dispatchers.Main) {
                historyAdapter.refreshData(listOfTotal, year)
            }
        }

        if (year == currentMonthYear.split("-")[1]) {
            layoutHistory.background = applicationContext.resources.getDrawable(R.drawable.background_page_white_rounded_top, null)
            imageRibbon.visibility = View.VISIBLE
            imageRibbon.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
        } else {
            layoutHistory.background = applicationContext.resources.getDrawable(R.drawable.background_page_gray_rounded_top, null)
            imageRibbon.visibility = View.INVISIBLE
        }
        textViewYear.text = year
    }

    private fun getMonthlyExpenseTotal(arrayListOfAmount: ArrayList<String>): BigInteger {
        var sum = BigInteger("0")
        if (arrayListOfAmount.isNotEmpty()) {
            for (i in 0 until arrayListOfAmount.size) {
                val amount = arrayListOfAmount[i].replace(",", "").toBigInteger()
                sum += amount
            }
        }
        return sum
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(this, arrayListOf(), this)
        recyclerviewHistory.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = historyAdapter
        }
    }

    override fun onMonthSelected(month: String, year: String) {
        startActivity(Intent(this@HistoryActivity, HistoryDetailsActivity::class.java)
            .putExtra("month", month).putExtra("year", year).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        finish()
    }

    override fun onBackPressed() {
        startActivity(Intent(this@HistoryActivity, HistoryDetailsActivity::class.java)
            .putExtra("month", currentMonthYear.split("-")[0])
            .putExtra("year", currentMonthYear.split("-")[1])
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        finish()
    }
}