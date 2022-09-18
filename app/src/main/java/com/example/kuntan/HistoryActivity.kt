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
import com.example.kuntan.entity.History
import com.example.kuntan.utility.AppUtil
import com.example.kuntan.utility.Constant
import com.example.kuntan.utility.KuntanRoomDatabase
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HistoryActivity : AppCompatActivity(), HistoryAdapter.HistoryAdapterListener {

    private val database by lazy { KuntanRoomDatabase(this) }
    @SuppressLint("SimpleDateFormat")
    private val currentMonthYear = SimpleDateFormat("MM-yyyy").format(Calendar.getInstance().time)
    private val historyOfMonth = ArrayList<List<History>>()
    private var year = currentMonthYear.split("-")[1]
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        init()
        initListener()
    }

    override fun onStart() {
        super.onStart()
        initPosition()
        slideUpHistoryLayout()
    }

    private fun init() {
        textViewYear.text = year
        setupRecyclerView(currentMonthYear.split("-")[0], currentMonthYear.split("-")[1])
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
            customHistoryLayout()
        }
        previousYear.setOnClickListener {
            year = (year.toInt() - 1).toString()
            customHistoryLayout()
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

    private fun setupRecyclerView(month: String, year: String) {
        populateHistories(year)
        historyAdapter = HistoryAdapter(applicationContext, historyOfMonth, month, year, this@HistoryActivity)
        recyclerviewHistory.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = historyAdapter
        }
    }

    private fun populateHistories(year: String) {
        historyOfMonth.clear()
        CoroutineScope(Dispatchers.IO).launch {
            for (i in 0 until Constant.AMOUNT_OF_MONTH) {
                val history = database.historyDao().getHistory(year, AppUtil.convertMonthCodeFromId(i))
                historyOfMonth.add(history)
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun customHistoryLayout() {
        var month = ""
        if (year == currentMonthYear.split("-")[1]) {
            month = currentMonthYear.split("-")[0]
            layoutHistory.background = applicationContext.resources.getDrawable(
                R.drawable.background_white_page_top_rounded, null)
            imageRibbon.visibility = View.VISIBLE
            imageRibbon.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
        } else {
            layoutHistory.background = applicationContext.resources.getDrawable(
                R.drawable.background_grey_page_top_rounded, null)
            imageRibbon.visibility = View.INVISIBLE
        }
        textViewYear.text = year
        setupRecyclerView(month, year)
        cardViewMonths.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
    }

    override fun onMonthSelected(month: String, history: String) {
        startActivity(Intent(this@HistoryActivity, HistoryDetailsActivity::class.java)
            .putExtra("month", month).putExtra("history", history).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        finish()
    }

    override fun onBackPressed() {
        startActivity(Intent(this@HistoryActivity, DashboardActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        finish()
    }
}