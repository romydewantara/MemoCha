package com.example.kuntan

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kuntan.adapter.YearAdapter
import kotlinx.android.synthetic.main.activity_history.*

class HistoryActivity : AppCompatActivity(), YearAdapter.YearAdapterListener {

    private lateinit var yearAdapter: YearAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        setupRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        initPosition()
        runOpeningAnim()
    }

    private fun initPosition() {
        val height = resources.displayMetrics.heightPixels
        layoutHistory.y = height.toFloat()
        previousYear.visibility = View.INVISIBLE
        textViewYear.visibility = View.INVISIBLE
        nextYear.visibility = View.INVISIBLE
        cardViewMonths.visibility = View.INVISIBLE
    }

    private fun runOpeningAnim() {
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
                    previousYear.visibility = View.VISIBLE
                    textViewYear.visibility = View.VISIBLE
                    nextYear.visibility = View.VISIBLE
                    cardViewMonths.visibility = View.VISIBLE
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

    private fun setupRecyclerView() {
        yearAdapter = YearAdapter(2021, this@HistoryActivity)
        recyclerviewHistory.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = yearAdapter
        }
    }

    override fun onMonthClicked(month: String) {
        startActivity(Intent(this@HistoryActivity, HistoryDetailsActivity::class.java)
            .putExtra("month", month).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
    }
}