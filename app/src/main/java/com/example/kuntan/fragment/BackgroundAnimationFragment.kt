package com.example.kuntan.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.kuntan.R
import com.example.kuntan.entity.Settings
import com.example.kuntan.utility.Constant
import kotlinx.android.synthetic.main.fragment_background_animation.*

class BackgroundAnimationFragment(
    private val mBackgroundAnimationState: Boolean,
    private val mBackgroundMusicState: Boolean,
    private val mBackgroundAnimation: String
) : Fragment(R.layout.fragment_background_animation) {

    var backgroundAnimationState = false
    var backgroundMusicState = false
    var backgroundAnimation = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backgroundAnimationState = mBackgroundAnimationState
        backgroundMusicState = mBackgroundMusicState
        backgroundAnimation = mBackgroundAnimation
        if (backgroundAnimationState) {
            cardViewSelectorAnimation.visibility = View.VISIBLE
            cardViewBackgroundMusic.visibility = View.VISIBLE
            textViewMusicInfo.visibility = View.VISIBLE
            animationOnOff.isChecked = true
            when(backgroundAnimation) {
                Constant.DASHBOARD_BACKGROUND_ANIMATION_AUTUMN -> {
                    radioButtonAutumn.isChecked = true
                }
                Constant.DASHBOARD_BACKGROUND_ANIMATION_SAKURA -> {
                    radioButtonSakura.isChecked = true
                }
                Constant.DASHBOARD_BACKGROUND_ANIMATION_SNOW -> {
                    radioButtonSnow.isChecked = true
                }
                Constant.DASHBOARD_BACKGROUND_ANIMATION_SUMMER -> {
                    radioButtonSummer.isChecked = true
                }
            }
            if (backgroundMusicState) backgroundMusicOnOff.isChecked = true
        }

        animationOnOff.setOnCheckedChangeListener { _, isChecked ->
            backgroundAnimationState = isChecked
            if (isChecked) {
                cardViewSelectorAnimation.visibility = View.VISIBLE
                cardViewBackgroundMusic.visibility = View.VISIBLE
                textViewMusicInfo.visibility = View.VISIBLE
            } else {
                cardViewSelectorAnimation.visibility = View.GONE
                cardViewBackgroundMusic.visibility = View.GONE
                textViewMusicInfo.visibility = View.GONE
            }
        }
        backgroundMusicOnOff.setOnCheckedChangeListener {_, isChecked, ->
            backgroundMusicState = isChecked
        }
        radioButtonAutumn.setOnClickListener {
            backgroundAnimation = Constant.DASHBOARD_BACKGROUND_ANIMATION_AUTUMN
        }
        radioButtonSakura.setOnClickListener {
            backgroundAnimation = Constant.DASHBOARD_BACKGROUND_ANIMATION_SAKURA
        }
        radioButtonSnow.setOnClickListener {
            backgroundAnimation = Constant.DASHBOARD_BACKGROUND_ANIMATION_SNOW
        }
        radioButtonSummer.setOnClickListener {
            backgroundAnimation = Constant.DASHBOARD_BACKGROUND_ANIMATION_SUMMER
        }
    }
}