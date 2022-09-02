package com.example.kuntan

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private val TAG = "DashboardActivity"
    private var isMenuShowing = false
    private var isAnimating = false
    private lateinit var iconArrows: ImageView
    private lateinit var constraintActivityMain: ConstraintLayout
    private lateinit var constraintMainMenu: ConstraintLayout
    private lateinit var constraintMainMenuContainer: ConstraintLayout

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        iconArrows = findViewById(R.id.iconArrows)
        constraintActivityMain = findViewById(R.id.constraintActivityMain)
        constraintMainMenu = findViewById(R.id.constraintMainMenu)
        constraintMainMenuContainer = findViewById(R.id.constraintMainMenuContainer)

        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("EEEE, dd MMMM, yyyy").format(calendar.time)
        findViewById<AppCompatTextView>(R.id.textDate).text = date

        findViewById<View>(R.id.question).setOnClickListener {
            //goToQuestionPage
        }
        findViewById<View>(R.id.cart).setOnClickListener {
            //goToCartPage
        }
        findViewById<View>(R.id.history).setOnClickListener {
            //goToHistory
        }
        findViewById<View>(R.id.iconArrows).setOnClickListener {
            //show-hide Menu
            Log.d(TAG, "onCreate - isMenuShowing: $isMenuShowing")
            isMenuShowing = !isMenuShowing
            if (!isMenuShowing) showMenu() else hideMenu()
        }
    }

    private fun showMenu() {
        if (!isAnimating) {
            val xTarget = constraintActivityMain.x
            val objectAnimator = ObjectAnimator.ofFloat(constraintMainMenuContainer, "translationX", xTarget).setDuration(200)
            val animatorSet = AnimatorSet()
            animatorSet.play(objectAnimator)
            animatorSet.start()
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    isAnimating = true
                }
                override fun onAnimationEnd(animation: Animator?) {
                    isAnimating = false
                }
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            })
        }
    }

    private fun hideMenu() {
        if (!isAnimating) {
            val xTarget = constraintActivityMain.x + constraintMainMenu.width
            val objectAnimator = ObjectAnimator.ofFloat(constraintMainMenuContainer, "translationX", xTarget).setDuration(300)
            val animatorSet = AnimatorSet()
            animatorSet.play(objectAnimator)
            animatorSet.start()
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    isAnimating = true
                }
                override fun onAnimationEnd(animation: Animator?) {
                    isAnimating = false
                }
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            })
        }
    }

}