package com.example.kuntan

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import com.example.kuntan.adapter.ApplicationLanguageAdapter
import com.example.kuntan.entity.Settings
import com.example.kuntan.lib.SelectorItemsBottomSheet
import com.example.kuntan.utility.KuntanRoomDatabase
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    companion object {
        const val DASHBOARD_BACKGROUND_AUTUMN = "autumn_fall.json"
        const val DASHBOARD_BACKGROUND_SAKURA = "sakura_fall.json"
        const val DASHBOARD_BACKGROUND_SNOW = "snow_fall.json"
        const val DASHBOARD_BACKGROUND_SUMMER = "summer_fall.json"

        private var settings: Settings? = null
    }
    private val database by lazy { KuntanRoomDatabase(this) }
    private var backgroundAnimation = ""
    private var dashboardBackground = ""

    private var isLanguageChanged = false
    private var isAnimationSwitched = false
    private var isDashboardBackgroundChanged = false

    private lateinit var selectorItemsBottomSheet: SelectorItemsBottomSheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        init()
        initListener()
    }

    private fun init() {
        //checkResetButtonEnable()
        backgroundAnimation = getString(R.string.setting_background_animation_off)
        CoroutineScope(Dispatchers.IO).launch {
            settings = database.settingsDao().getSettings()
            textViewLanguage.text = settings?.language
            if (settings?.backgroundAnimation == getString(R.string.setting_background_animation_on)) {
                animationOnOff.isChecked = true
                when(settings?.dashboardBackground) {
                    DASHBOARD_BACKGROUND_AUTUMN -> { radioButtonAutumn.isChecked = true }
                    DASHBOARD_BACKGROUND_SAKURA -> { radioButtonSakura.isChecked = true }
                    DASHBOARD_BACKGROUND_SNOW -> { radioButtonSnow.isChecked = true }
                    DASHBOARD_BACKGROUND_SUMMER -> { radioButtonSummer.isChecked = true }
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initListener() {
        textViewReset.setOnClickListener {
            //show dialog first
            CoroutineScope(Dispatchers.IO).launch {
                database.settingsDao().updateSetting(getString(R.string.setting_language_english), getString(R.string.setting_background_animation_off), "")
                TextViewCompat.setTextAppearance(textViewReset, R.style.TextRegularGrey14)
                textViewReset.isEnabled = false
            }
        }
        textViewLanguage.setOnClickListener {
            selectorItemsBottomSheet = SelectorItemsBottomSheet(
                getString(R.string.setting_application_language), ApplicationLanguageAdapter(applicationContext,
                    object : ApplicationLanguageAdapter.ApplicationLanguageListener {
                        override fun onLanguageSelected(language: String) {
                            textViewLanguage.text = language
                            selectorItemsBottomSheet.dismiss()
                            isLanguageChanged = language != settings?.language
                            checkApplyButtonEnable()
                        }
                    }))
            selectorItemsBottomSheet.show(supportFragmentManager, "selector_bottom_sheet")
        }
        animationOnOff.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                backgroundAnimation = getString(R.string.setting_background_animation_on)
                dashboardBackground = DASHBOARD_BACKGROUND_AUTUMN
                layoutAnimationSelection.visibility = View.VISIBLE
                layoutAnimationSelection.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
            } else {
                backgroundAnimation = getString(R.string.setting_background_animation_off)
                dashboardBackground = ""
                layoutAnimationSelection.visibility = View.GONE
                layoutAnimationSelection.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out))
            }
            isAnimationSwitched = backgroundAnimation != settings?.backgroundAnimation
            checkApplyButtonEnable()
        }
        radioButtonAutumn.setOnClickListener {
            dashboardBackground = DASHBOARD_BACKGROUND_AUTUMN
            isDashboardBackgroundChanged = dashboardBackground != settings?.dashboardBackground
            checkApplyButtonEnable()
        }
        radioButtonSakura.setOnClickListener {
            dashboardBackground = DASHBOARD_BACKGROUND_SAKURA
            isDashboardBackgroundChanged = dashboardBackground != settings?.dashboardBackground
            checkApplyButtonEnable()
        }
        radioButtonSnow.setOnClickListener {
            dashboardBackground = DASHBOARD_BACKGROUND_SNOW
            isDashboardBackgroundChanged = dashboardBackground != settings?.dashboardBackground
            checkApplyButtonEnable()
        }
        radioButtonSummer.setOnClickListener {
            dashboardBackground = DASHBOARD_BACKGROUND_SUMMER
            isDashboardBackgroundChanged = dashboardBackground != settings?.dashboardBackground
            checkApplyButtonEnable()
        }
        textViewApply.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                database.settingsDao().updateSetting(textViewLanguage.text.toString(), backgroundAnimation, dashboardBackground)
                settings = database.settingsDao().getSettings()
                runOnUiThread {
                    textViewApply.isEnabled = false
                    textViewApply.background = applicationContext.resources.getDrawable(R.drawable.background_button_save_disabled, null)
                    checkResetButtonEnable()
                    Snackbar.make(rootLayoutSetting, "Perubahan berhasil diterapkan", Snackbar.LENGTH_LONG).setAction("DISMISS") {}.show()
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun checkApplyButtonEnable() {
        if (isLanguageChanged || isAnimationSwitched || isDashboardBackgroundChanged) {
            textViewApply.isEnabled = true
            textViewApply.background = applicationContext.resources.getDrawable(R.drawable.selector_button_save_expenses, null)
        } else {
            textViewApply.isEnabled = false
            textViewApply.background = applicationContext.resources.getDrawable(R.drawable.background_button_save_disabled, null)
        }
    }

    private fun checkResetButtonEnable() {
        if (settings?.language != getString(R.string.setting_language_english)
            || settings?.backgroundAnimation != getString(R.string.setting_background_animation_off)) {
            textViewReset.isEnabled = true
            TextViewCompat.setTextAppearance(textViewReset, R.style.TextRegularRed14)
        }
    }
}