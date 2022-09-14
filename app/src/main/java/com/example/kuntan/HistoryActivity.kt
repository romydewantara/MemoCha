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
import kotlinx.android.synthetic.main.activity_history.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity(), HistoryAdapter.YearAdapterListener {

    @SuppressLint("SimpleDateFormat")
    private val currentMonthYear = SimpleDateFormat("MMMM-yyyy").format(Calendar.getInstance().time)
    private var year = currentMonthYear.split("-")[1]
    private lateinit var yearAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        textViewYear.text = year
        setupRecyclerView(currentMonthYear.split("-")[0], currentMonthYear.split("-")[1])
        initListener()
    }

    override fun onStart() {
        super.onStart()
        initPosition()
        slideUpHistoryLayout()
    }

    private fun initPosition() {
        val height = resources.displayMetrics.heightPixels
        layoutHistory.y = height.toFloat()
        imagePeople.visibility = View.INVISIBLE
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
                    imagePeople.visibility = View.VISIBLE
                    previousYear.visibility = View.VISIBLE
                    textViewYear.visibility = View.VISIBLE
                    nextYear.visibility = View.VISIBLE
                    cardViewMonths.visibility = View.VISIBLE
                    imagePeople.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
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
        yearAdapter = HistoryAdapter(this, month, year, this@HistoryActivity)
        recyclerviewHistory.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = yearAdapter
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun customHistoryLayout() {
        var month = ""
        if (year == currentMonthYear.split("-")[1]) {
            month = currentMonthYear.split("-")[0]
            layoutHistory.background = applicationContext.resources.getDrawable(
                R.drawable.background_white_page_top_rounded, null)
            imagePeople.visibility = View.VISIBLE
            imagePeople.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
        } else {
            layoutHistory.background = applicationContext.resources.getDrawable(
                R.drawable.background_grey_page_top_rounded, null)
            imagePeople.visibility = View.INVISIBLE
        }
        textViewYear.text = year
        setupRecyclerView(month, year)
        cardViewMonths.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
    }

    override fun onMonthSelected(month: String, history: String) {
        startActivity(Intent(this@HistoryActivity, HistoryDetailsActivity::class.java)
            .putExtra("month", month).putExtra("history", history).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
    }

    override fun onBackPressed() {
        startActivity(Intent(this@HistoryActivity, DashboardActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        finish()
    }
}