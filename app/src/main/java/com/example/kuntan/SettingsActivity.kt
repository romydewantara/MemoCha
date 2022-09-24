package com.example.kuntan

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import com.example.kuntan.adapter.ApplicationLanguageAdapter
import com.example.kuntan.adapter.ClockThemeSpinnerAdapter
import com.example.kuntan.entity.Settings
import com.example.kuntan.lib.KuntanPopupDialog
import com.example.kuntan.lib.SelectorItemsBottomSheet
import com.example.kuntan.utility.AppUtil
import com.example.kuntan.utility.Constant
import com.example.kuntan.utility.KuntanRoomDatabase
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    companion object {
        private var settings: Settings? = null
    }
    private val database by lazy { KuntanRoomDatabase(this) }
    private lateinit var appTheme: String
    private lateinit var analogClockTheme: String
    private lateinit var backgroundAnimation: String
    private lateinit var dashboardBackground: String
    private lateinit var backgroundMusicState: String
    private lateinit var surename: String

    private var isAppThemeChanged = false
    private var isLanguageChanged = false
    private var isAnalogClockThemeChanged = false
    private var isAnimationSwitched = false
    private var isAudioSwitched = false
    private var isDashboardBackgroundChanged = false
    private var isSurenameChanged = false
    private var isSurenameFilled = false
    private var isReset = false

    private lateinit var selectorItemsBottomSheet: SelectorItemsBottomSheet
    private lateinit var spinnerClockThemeAdapter: ClockThemeSpinnerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        init()
        initListener()
    }

    private fun init() {
        rootLayoutSetting.viewTreeObserver.addOnGlobalLayoutListener {
            if (!AppUtil.isKeyboardVisible(rootLayoutSetting)) editTextSurname.isFocusable = false
        }
        analogClockTheme = Constant.DASHBOARD_CLOCK_PRIMARY
        backgroundAnimation = getString(R.string.setting_background_animation_off)
        backgroundMusicState = getString(R.string.setting_background_music_off)
        dashboardBackground = Constant.DASHBOARD_BACKGROUND_AUTUMN

        val clockTheme = arrayListOf(
            Constant.DASHBOARD_CLOCK_PRIMARY, Constant.DASHBOARD_CLOCK_SECONDARY, Constant.DASHBOARD_CLOCK_DARK,
            Constant.DASHBOARD_CLOCK_YELLOW, Constant.DASHBOARD_CLOCK_RED_ORANGE, Constant.DASHBOARD_CLOCK_PINK)
        spinnerClockThemeAdapter = ClockThemeSpinnerAdapter(applicationContext, clockTheme)
        analogClockSpinner.adapter = spinnerClockThemeAdapter

        CoroutineScope(Dispatchers.IO).launch {
            settings = database.settingsDao().getSettings()
            textViewLanguage.text = settings?.language

            when(settings?.analogClockTheme) {
                Constant.DASHBOARD_CLOCK_PRIMARY -> { analogClockTheme = Constant.DASHBOARD_CLOCK_PRIMARY }
                Constant.DASHBOARD_CLOCK_SECONDARY -> { analogClockTheme = Constant.DASHBOARD_CLOCK_SECONDARY }
                Constant.DASHBOARD_CLOCK_DARK -> { analogClockTheme = Constant.DASHBOARD_CLOCK_DARK }
                Constant.DASHBOARD_CLOCK_YELLOW -> { analogClockTheme = Constant.DASHBOARD_CLOCK_YELLOW }
                Constant.DASHBOARD_CLOCK_RED_ORANGE -> { analogClockTheme = Constant.DASHBOARD_CLOCK_RED_ORANGE }
                Constant.DASHBOARD_CLOCK_PINK -> { analogClockTheme = Constant.DASHBOARD_CLOCK_PINK }
            }
            analogClockSpinner.setSelection(AppUtil.convertIdAnalogClockFromTheme(analogClockTheme))


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

    @SuppressLint("UseCompatLoadingForDrawables", "ClickableViewAccessibility")
    private fun initListener() {
        textViewReset.setOnClickListener {
            val kuntanPopupDialog = KuntanPopupDialog.newInstance().setContent(getString(R.string.dialog_title_warning), getString(R.string.dialog_message_ask_reset),
                getString(R.string.dialog_button_reset), getString(R.string.dialog_button_cancel), object : KuntanPopupDialog.KuntanPopupDialogListener {
                    override fun onNegativeButton() {
                        CoroutineScope(Dispatchers.IO).launch {
                            database.settingsDao().updateSetting(
                                "",
                                Constant.APP_THEME_LIGHT,
                                getString(R.string.setting_language_english),
                                Constant.DASHBOARD_CLOCK_PRIMARY,
                                getString(R.string.setting_background_animation_off),
                                Constant.DASHBOARD_BACKGROUND_AUTUMN,
                                getString(R.string.setting_background_music_off)
                            )
                            settings = database.settingsDao().getSettings()
                            runOnUiThread {
                                textViewReset.isEnabled = false
                                TextViewCompat.setTextAppearance(textViewReset, R.style.TextRegularGrey14)
                                textViewLanguage.text = getString(R.string.setting_language_english)
                                audioOnOff.isChecked = false
                                animationOnOff.isChecked = false
                                textViewApply.isEnabled = false
                                textViewApply.background = applicationContext.resources.getDrawable(R.drawable.background_button_apply_disabled, null)
                                appTheme = Constant.APP_THEME_LIGHT
                                analogClockTheme = Constant.DASHBOARD_CLOCK_PRIMARY
                                backgroundAnimation = getString(R.string.setting_background_animation_off)
                                backgroundMusicState = getString(R.string.setting_background_music_off)
                                dashboardBackground = Constant.DASHBOARD_BACKGROUND_AUTUMN
                                isAppThemeChanged = false
                                isLanguageChanged = false
                                isAnalogClockThemeChanged = false
                                isAnimationSwitched = false
                                isAudioSwitched = false
                                isDashboardBackgroundChanged = false
                                isReset = true
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
        spinnerClockThemeAdapter.addOnItemSelectedListener(object :
            ClockThemeSpinnerAdapter.ItemSelectedListener {
            override fun onItemSelected(clockTheme: String) {
                analogClockTheme = clockTheme
                isAnalogClockThemeChanged = clockTheme != settings?.analogClockTheme
                checkApplyButtonEnable()
            }
        })
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
        surnameOnOff.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editTextSurname.visibility = View.VISIBLE
                editTextSurname.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
            } else {
                editTextSurname.visibility = View.GONE
                editTextSurname.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out))
            }
        }
        editTextSurname.addTextChangedListener(onTextChangedListener())
        editTextSurname.setOnTouchListener { _, _, ->
            editTextSurname.isFocusableInTouchMode = true
            false
        }
        textViewApply.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                database.settingsDao().updateSetting("", Constant.APP_THEME_LIGHT, textViewLanguage.text.toString(),
                    analogClockTheme, backgroundAnimation, dashboardBackground, backgroundMusicState)
                settings = database.settingsDao().getSettings()
                runOnUiThread {
                    textViewApply.isEnabled = false
                    textViewApply.background = applicationContext.resources.getDrawable(R.drawable.background_button_apply_disabled, null)
                    checkResetButtonEnable()
                    Snackbar.make(rootLayoutSetting, getString(R.string.snackbar_setting_applied), Snackbar.LENGTH_SHORT).setAction("DISMISS") {}.show()
                    if (isLanguageChanged) showRestartAppDialog(String.format(getString(R.string.dialog_message_switch_language), settings?.language))
                }
            }
        }
    }

    private fun onTextChangedListener() : TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (editTextSurname.text.toString().isNotEmpty()) {
                    isSurenameFilled = true
                } else {
                    isSurenameFilled = false
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun checkApplyButtonEnable() {
        Log.d("SA", "checkApplyButtonEnable isAnalogChanged: $isAnalogClockThemeChanged")
        if (isLanguageChanged || isAnalogClockThemeChanged || isAnimationSwitched || isAudioSwitched || isDashboardBackgroundChanged) {
            textViewApply.isEnabled = true
            textViewApply.background = applicationContext.resources.getDrawable(R.drawable.selector_button_apply_settings, null)
        } else {
            textViewApply.isEnabled = false
            textViewApply.background = applicationContext.resources.getDrawable(R.drawable.background_button_apply_disabled, null)
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

    override fun onBackPressed() {
        if (isReset) goToSplashActivity() else super.onBackPressed()
    }

    private fun goToSplashActivity() {
        startActivity(
            Intent(this@SettingsActivity, SplashActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()
    }

    private fun showRestartAppDialog(message: String) {
        val kuntanPopupDialog = KuntanPopupDialog.newInstance().setContent(getString(R.string.dialog_title_information),
            message, "", "OK", object : KuntanPopupDialog.KuntanPopupDialogListener {
                override fun onNegativeButton() {}
                override fun onPositiveButton() {
                    goToSplashActivity()
                }
            })
        kuntanPopupDialog.show(supportFragmentManager, kuntanPopupDialog.tag)
    }
}