package com.example.kuntan

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import com.example.kuntan.adapter.ApplicationLanguageAdapter
import com.example.kuntan.entity.Settings
import com.example.kuntan.lib.KuntanPopupDialog
import com.example.kuntan.lib.SelectorItemsBottomSheet
import com.example.kuntan.utility.Constant
import com.example.kuntan.utility.KuntanRoomDatabase
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SettingsActivity : AppCompatActivity() {

    companion object {
        private var settings: Settings? = null
    }
    private val database by lazy { KuntanRoomDatabase(this) }
    private lateinit var backgroundAnimation: String
    private lateinit var dashboardBackground: String
    private lateinit var backgroundMusicState: String

    private var isLanguageChanged = false
    private var isAnimationSwitched = false
    private var isAudioSwitched = false
    private var isDashboardBackgroundChanged = false

    private lateinit var selectorItemsBottomSheet: SelectorItemsBottomSheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        init()
        initListener()
    }

    private fun init() {
        backgroundAnimation = getString(R.string.setting_background_animation_off)
        backgroundMusicState = getString(R.string.setting_background_music_off)
        dashboardBackground = Constant.DASHBOARD_BACKGROUND_AUTUMN

        CoroutineScope(Dispatchers.IO).launch {
            settings = database.settingsDao().getSettings()
            textViewLanguage.text = settings?.language

            if (settings?.backgroundAnimation == getString(R.string.setting_background_animation_on)) {
                backgroundAnimation = getString(R.string.setting_background_animation_on)
                animationOnOff.isChecked = true

                when(settings?.dashboardBackground) {
                    Constant.DASHBOARD_BACKGROUND_AUTUMN -> {
                        radioButtonAutumn.isChecked = true
                        dashboardBackground = Constant.DASHBOARD_BACKGROUND_AUTUMN
                    }
                    Constant.DASHBOARD_BACKGROUND_SAKURA -> {
                        radioButtonSakura.isChecked = true
                        dashboardBackground = Constant.DASHBOARD_BACKGROUND_SAKURA
                    }
                    Constant.DASHBOARD_BACKGROUND_SNOW -> {
                        radioButtonSnow.isChecked = true
                        dashboardBackground = Constant.DASHBOARD_BACKGROUND_SNOW
                    }
                    Constant.DASHBOARD_BACKGROUND_SUMMER -> {
                        radioButtonSummer.isChecked = true
                        dashboardBackground = Constant.DASHBOARD_BACKGROUND_SUMMER
                    }
                }

                if (settings?.backgroundMusicState == getString(R.string.setting_background_music_on)) {
                    backgroundMusicState = getString(R.string.setting_background_music_on)
                    audioOnOff.isChecked = true
                }
            }
            checkResetButtonEnable()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initListener() {
        textViewReset.setOnClickListener {
            val kuntanPopupDialog = KuntanPopupDialog.newInstance().setContent(getString(R.string.dialog_title_warning), getString(R.string.dialog_message_ask_reset),
                getString(R.string.dialog_button_reset), getString(R.string.dialog_button_cancel), object : KuntanPopupDialog.KuntanPopupDialogListener {
                    override fun onNegativeButton() {
                        CoroutineScope(Dispatchers.IO).launch {
                            database.settingsDao().updateSetting(getString(R.string.setting_language_english), getString(R.string.setting_background_animation_off), Constant.DASHBOARD_BACKGROUND_AUTUMN, getString(R.string.setting_background_music_off))
                            settings = database.settingsDao().getSettings()
                            runOnUiThread {
                                textViewReset.isEnabled = false
                                TextViewCompat.setTextAppearance(textViewReset, R.style.TextRegularGrey14)
                                textViewLanguage.text = getString(R.string.setting_language_english)
                                audioOnOff.isChecked = false
                                animationOnOff.isChecked = false
                                textViewApply.isEnabled = false
                                textViewApply.background = applicationContext.resources.getDrawable(R.drawable.background_button_save_disabled, null)
                                backgroundAnimation = getString(R.string.setting_background_animation_off)
                                backgroundMusicState = getString(R.string.setting_background_music_off)
                                dashboardBackground = Constant.DASHBOARD_BACKGROUND_AUTUMN
                                isLanguageChanged = false
                                isAnimationSwitched = false
                                isAudioSwitched = false
                                isDashboardBackgroundChanged = false
                                showRestartAppDialog(getString(R.string.dialog_message_reset))
                            }
                        }
                    }
                    override fun onPositiveButton() {}
                })
            kuntanPopupDialog.show(supportFragmentManager, kuntanPopupDialog.tag)
        }
        textViewLanguage.setOnClickListener {
            selectorItemsBottomSheet = SelectorItemsBottomSheet(
                getString(R.string.settings_application_language), ApplicationLanguageAdapter(applicationContext,
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
        animationOnOff.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                backgroundAnimation = getString(R.string.setting_background_animation_on)
                dashboardBackground = Constant.DASHBOARD_BACKGROUND_AUTUMN
                layoutAnimationSelection.visibility = View.VISIBLE
                layoutAudioSelection.visibility = View.VISIBLE
                layoutAnimationSelection.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
                layoutAudioSelection.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
            } else {
                layoutAnimationSelection.visibility = View.GONE
                layoutAudioSelection.visibility = View.GONE
                layoutAnimationSelection.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out))
                layoutAudioSelection.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out))
                backgroundAnimation = getString(R.string.setting_background_animation_off)
                dashboardBackground = Constant.DASHBOARD_BACKGROUND_AUTUMN
                isDashboardBackgroundChanged = dashboardBackground != settings?.dashboardBackground
                radioButtonAutumn.isChecked = true
                if (audioOnOff.isChecked) {
                    audioOnOff.isChecked = false
                    backgroundMusicState = getString(R.string.setting_background_music_off)
                    isAudioSwitched = backgroundMusicState != settings?.backgroundMusicState
                }
            }
            isAnimationSwitched = backgroundAnimation != settings?.backgroundAnimation
            checkApplyButtonEnable()
        }
        audioOnOff.setOnCheckedChangeListener { _, isChecked ->
            backgroundMusicState = if (isChecked) {
                getString(R.string.setting_background_music_on)
            } else {
                getString(R.string.setting_background_music_off)
            }
            isAudioSwitched = backgroundMusicState != settings?.backgroundMusicState
            checkApplyButtonEnable()
        }
        radioButtonAutumn.setOnClickListener {
            dashboardBackground = Constant.DASHBOARD_BACKGROUND_AUTUMN
            isDashboardBackgroundChanged = dashboardBackground != settings?.dashboardBackground
            checkApplyButtonEnable()
        }
        radioButtonSakura.setOnClickListener {
            dashboardBackground = Constant.DASHBOARD_BACKGROUND_SAKURA
            isDashboardBackgroundChanged = dashboardBackground != settings?.dashboardBackground
            checkApplyButtonEnable()
        }
        radioButtonSnow.setOnClickListener {
            dashboardBackground = Constant.DASHBOARD_BACKGROUND_SNOW
            isDashboardBackgroundChanged = dashboardBackground != settings?.dashboardBackground
            checkApplyButtonEnable()
        }
        radioButtonSummer.setOnClickListener {
            dashboardBackground = Constant.DASHBOARD_BACKGROUND_SUMMER
            isDashboardBackgroundChanged = dashboardBackground != settings?.dashboardBackground
            checkApplyButtonEnable()
        }
        textViewApply.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                database.settingsDao().updateSetting(textViewLanguage.text.toString(), backgroundAnimation, dashboardBackground, backgroundMusicState)
                settings = database.settingsDao().getSettings()
                runOnUiThread {
                    textViewApply.isEnabled = false
                    textViewApply.background = applicationContext.resources.getDrawable(R.drawable.background_button_save_disabled, null)
                    checkResetButtonEnable()
                    Snackbar.make(rootLayoutSetting, "Perubahan telah diterapkan", Snackbar.LENGTH_LONG).setAction("DISMISS") {}.show()
                    if (isLanguageChanged) showRestartAppDialog(String.format(getString(R.string.dialog_message_switch_language), settings?.language))
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun checkApplyButtonEnable() {
        if (isLanguageChanged || isAnimationSwitched || isAudioSwitched || isDashboardBackgroundChanged) {
            textViewApply.isEnabled = true
            textViewApply.background = applicationContext.resources.getDrawable(R.drawable.selector_button_save_expenses, null)
        } else {
            textViewApply.isEnabled = false
            textViewApply.background = applicationContext.resources.getDrawable(R.drawable.background_button_save_disabled, null)
        }
    }

    private fun checkResetButtonEnable() {
        if (settings?.language != getString(R.string.setting_language_english) || settings?.backgroundAnimation != getString(R.string.setting_background_animation_off)) {
            textViewReset.isEnabled = true
            TextViewCompat.setTextAppearance(textViewReset, R.style.TextRegularRed14)
        } else {
            textViewReset.isEnabled = false
            TextViewCompat.setTextAppearance(textViewReset, R.style.TextRegularGrey14)
        }
    }

    private fun showRestartAppDialog(message: String) {
        val kuntanPopupDialog = KuntanPopupDialog.newInstance().setContent(getString(R.string.dialog_title_information),
            message, "", "OK", object : KuntanPopupDialog.KuntanPopupDialogListener {
                override fun onNegativeButton() {}
                override fun onPositiveButton() {
                    startActivity(Intent(this@SettingsActivity, SplashActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
                    finish()
                }
            })
        kuntanPopupDialog.show(supportFragmentManager, kuntanPopupDialog.tag)
    }
}