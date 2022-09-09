package com.example.kuntan

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kuntan.adapter.YearAdapter
import kotlinx.android.synthetic.main.activity_history.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var yearAdapter: YearAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        initPosition()
        setupRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        runOpeningAnim()
    }

    private fun initPosition() {
        val height = resources.displayMetrics.heightPixels
        layoutHistory.y = height.toFloat()
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
        yearAdapter = YearAdapter(2021)
        recyclerviewHistory.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = yearAdapter
        }
    }
}