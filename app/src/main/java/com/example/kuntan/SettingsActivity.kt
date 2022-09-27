package com.example.kuntan

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import com.example.kuntan.entity.Settings
import com.example.kuntan.fragment.AnalogClockFragment
import com.example.kuntan.fragment.ApplicationLanguageFragment
import com.example.kuntan.fragment.BackgroundAnimationFragment
import com.example.kuntan.fragment.SurnameFragment
import com.example.kuntan.lib.KuntanPopupDialog
import com.example.kuntan.utility.Constant
import com.example.kuntan.utility.KuntanRoomDatabase
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_settings.rootLayoutSetting
import kotlinx.android.synthetic.main.activity_settings.textViewApply
import kotlinx.android.synthetic.main.activity_settings.textViewLanguage
import kotlinx.android.synthetic.main.activity_settings.textViewReset
import kotlinx.android.synthetic.main.layout_settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private val database by lazy { KuntanRoomDatabase(this) }

    private var isApplicationThemeChanged = false
    private var isApplicationLanguageChanged = false
    private var isClockThemeChanged = false
    private var isBackgroundAnimationStateChanged = false
    private var isBackgroundAnimationChanged = false
    private var isBackgroundMusicStateChanged = false
    private var isSurnameStateChanged = false
    private var isSurnameChanged = false

    private var isReset = false
    private var isFragmentShown = false

    private lateinit var fragment: Fragment
    private var applicationTheme = ""
    private var applicationLanguage = ""
    private var clockTheme = ""
    private var backgroundAnimation = ""
    private var surname = ""
    private var backgroundAnimationState = false
    private var backgroundMusicState = false
    private var notificationState = false
    private var surnameState = false

    private lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_settings)

        init()
        initListener()
    }

    private fun init() {
        checkApplyButtonEnable()
        CoroutineScope(Dispatchers.IO).launch {
            val tempSettings = database.settingsDao().getSettings()
            if (tempSettings != null) settings = tempSettings
            runOnUiThread {
                checkResetButtonEnable()
                reset()
            }
        }
    }

    private fun reset() {
        applicationTheme = settings.applicationTheme
        applicationLanguage = settings.applicationLanguage
        clockTheme = settings.clockTheme

        textViewAppTheme.text = applicationTheme
        textViewLanguage.text = applicationLanguage
        textViewAnalogClock.text = clockTheme

        notificationState = settings.notificationState
        backgroundAnimationState = settings.backgroundAnimationState
        backgroundMusicState = settings.backgroundMusicState
        surnameState = settings.surnameState
        backgroundAnimation = settings.backgroundAnimation
        surname = settings.surname

        textViewAnimation.text = if (backgroundAnimationState) getBackgroundAnimationName(backgroundAnimation) else
            getString(R.string.setting_background_animation_off)

        textViewSurname.text = if (surnameState) settings.surname else
            getString(R.string.setting_surname_off)

        textViewNotification.text = if (notificationState) getString(R.string.setting_notification_on) else
            getString(R.string.setting_notification_off)
    }

    @SuppressLint("UseCompatLoadingForDrawables", "ClickableViewAccessibility")
    private fun initListener() {
        settingAppTheme.setOnClickListener {
            val kuntanPopupDialog = KuntanPopupDialog.newInstance().setContent("",
                getString(R.string.dialog_message_app_theme), "", getString(R.string.button_ok),
                object : KuntanPopupDialog.KuntanPopupDialogListener {
                    override fun onNegativeButton() {}
                    override fun onPositiveButton() {}
                })
            kuntanPopupDialog.show(supportFragmentManager, kuntanPopupDialog.tag)
        }
        setting1.setOnClickListener {
            fragment = ApplicationLanguageFragment(applicationLanguage)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.containerSetting, fragment, fragment.tag)
                .commit()
            showFragment()
        }

        setting2.setOnClickListener {
            fragment = AnalogClockFragment(clockTheme)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.containerSetting, fragment, fragment.tag)
                .commit()
            showFragment()
        }

        setting3.setOnClickListener {
            fragment = BackgroundAnimationFragment(backgroundAnimationState, backgroundMusicState, backgroundAnimation)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.containerSetting, fragment, fragment.tag)
                .commit()
            showFragment()
        }

        setting4.setOnClickListener {
            fragment = SurnameFragment(surnameState, surname)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.containerSetting, fragment, fragment.tag)
                .commit()
            showFragment()
        }

        textViewApply.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                database.settingsDao().updateSetting(surname, applicationTheme, applicationLanguage,
                    clockTheme, backgroundAnimation, surnameState, backgroundAnimationState,
                    backgroundMusicState, notificationState)
                val tempSettings = database.settingsDao().getSettings()
                if (tempSettings != null) settings = tempSettings
                runOnUiThread {
                    textViewApply.isEnabled = false
                    textViewApply.background = applicationContext.resources.getDrawable(R.drawable.background_button_apply_disabled, null)
                    checkResetButtonEnable()
                    Snackbar.make(rootLayoutSetting, getString(R.string.snackbar_setting_applied), Snackbar.LENGTH_SHORT).setAction("DISMISS") {}.show()
                    if (isApplicationLanguageChanged)
                        showRestartAppDialog(String.format(getString(R.string.dialog_message_switch_language), settings.applicationLanguage))
                }
            }
        }
        textViewReset.setOnClickListener {
            val kPopupDialog = KuntanPopupDialog.newInstance().setContent(
                getString(R.string.dialog_title_warning),
                getString(R.string.dialog_message_ask_reset),
                getString(R.string.dialog_button_reset),
                getString(R.string.dialog_button_cancel),
                object : KuntanPopupDialog.KuntanPopupDialogListener {
                    override fun onNegativeButton() {
                        CoroutineScope(Dispatchers.IO).launch {
                            surname = ""
                            applicationTheme = Constant.APP_THEME_LIGHT
                            applicationLanguage = getString(R.string.setting_language_english)
                            clockTheme = getString(R.string.clock_theme_primary)
                            backgroundAnimation = Constant.DASHBOARD_BACKGROUND_ANIMATION_AUTUMN
                            surnameState = false
                            backgroundAnimationState = false
                            backgroundMusicState = false
                            notificationState = false
                            database.settingsDao().updateSetting(surname, applicationTheme, applicationLanguage,
                                clockTheme, backgroundAnimation, surnameState, backgroundAnimationState,
                                backgroundMusicState, notificationState)
                            val tempSettings = database.settingsDao().getSettings()
                            if (tempSettings != null) settings = tempSettings
                            runOnUiThread {
                                textViewReset.isEnabled = false
                                TextViewCompat.setTextAppearance(textViewReset, R.style.TextRegularGrey14)
                                textViewLanguage.text = getString(R.string.setting_language_english)
                                textViewApply.isEnabled = false
                                textViewApply.background = applicationContext.resources.getDrawable(R.drawable.background_button_apply_disabled, null)
                                reset()
                                isReset = true
                            }
                        }
                    }
                    override fun onPositiveButton() {}
                })
            kPopupDialog.show(supportFragmentManager, kPopupDialog.tag)
        }
    }

    private fun showFragment() {
        showOverlayLayout()
        isFragmentShown = true
        containerSetting.x = resources.displayMetrics.widthPixels.toFloat()
        containerSetting.visibility = View.VISIBLE
        val objectAnimator = ObjectAnimator.ofFloat(containerSetting, "translationX", 0f)
        objectAnimator.duration = 300L
        objectAnimator.start()
    }

    private fun hideFragment() {
        isFragmentShown = false
        val objectAnimator = ObjectAnimator.ofFloat(containerSetting, "translationX", resources.displayMetrics.widthPixels.toFloat())
        objectAnimator.duration = 150L
        objectAnimator.start()
        objectAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                containerSetting.visibility = View.GONE
                hideOverlayLayout()
            }
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}
        })
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun checkApplyButtonEnable() {
        if (isApplicationThemeChanged || isApplicationLanguageChanged || isClockThemeChanged ||
            isBackgroundAnimationStateChanged || isBackgroundAnimationChanged ||
            isBackgroundMusicStateChanged || isSurnameStateChanged || isSurnameChanged
        ) {
            textViewApply.isEnabled = true
            textViewApply.background = applicationContext.resources.getDrawable(R.drawable.selector_button_apply_settings, null)
        } else {
            textViewApply.isEnabled = false
            textViewApply.background = applicationContext.resources.getDrawable(R.drawable.background_button_apply_disabled, null)
        }
    }

    private fun getBackgroundAnimationName(backgroundAnimation: String): String {
        when(backgroundAnimation) {
            Constant.DASHBOARD_BACKGROUND_ANIMATION_AUTUMN -> return getString(R.string.settings_theme_autumn)
            Constant.DASHBOARD_BACKGROUND_ANIMATION_SAKURA -> return getString(R.string.settings_theme_sakura)
            Constant.DASHBOARD_BACKGROUND_ANIMATION_SNOW -> return getString(R.string.settings_theme_snow)
            Constant.DASHBOARD_BACKGROUND_ANIMATION_SUMMER -> return getString(R.string.settings_theme_summer)
        }
        return getString(R.string.settings_theme_autumn)
    }

    private fun checkResetButtonEnable() {
        if (settings.applicationLanguage != getString(R.string.setting_language_english) || settings.backgroundAnimationState) {
            textViewReset.isEnabled = true
            TextViewCompat.setTextAppearance(textViewReset, R.style.TextRegularRed14)
        } else {
            textViewReset.isEnabled = false
            TextViewCompat.setTextAppearance(textViewReset, R.style.TextRegularGrey14)
        }
    }

    private fun showOverlayLayout() {
        settingsOverlayLayout.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.white_overlay_fade_in))
        settingsOverlayLayout.visibility = View.VISIBLE
    }

    private fun hideOverlayLayout() {
        //settingsOverlayLayout.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.white_overlay_fade_out))
        settingsOverlayLayout.visibility = View.GONE
    }

    override fun onBackPressed() {
        if (isFragmentShown) {
            when (fragment) {
                is ApplicationLanguageFragment -> {
                    applicationLanguage = (fragment as ApplicationLanguageFragment).language
                    textViewLanguage.text = applicationLanguage
                    isApplicationLanguageChanged = applicationLanguage != settings.applicationLanguage
                }
                is AnalogClockFragment -> {
                    clockTheme = (fragment as AnalogClockFragment).clockTheme
                    textViewAnalogClock.text = clockTheme
                    isClockThemeChanged = clockTheme != settings.clockTheme
                }
                is BackgroundAnimationFragment -> {
                    backgroundAnimationState = (fragment as BackgroundAnimationFragment).backgroundAnimationState
                    backgroundAnimation = settings.backgroundAnimation
                    if (backgroundAnimationState) {
                        backgroundAnimation = (fragment as BackgroundAnimationFragment).backgroundAnimation
                        backgroundMusicState = (fragment as BackgroundAnimationFragment).backgroundMusicState
                        when(backgroundAnimation) {
                            Constant.DASHBOARD_BACKGROUND_ANIMATION_AUTUMN -> textViewAnimation.text = getString(R.string.settings_theme_autumn)
                            Constant.DASHBOARD_BACKGROUND_ANIMATION_SAKURA -> textViewAnimation.text = getString(R.string.settings_theme_sakura)
                            Constant.DASHBOARD_BACKGROUND_ANIMATION_SNOW -> textViewAnimation.text = getString(R.string.settings_theme_snow)
                            Constant.DASHBOARD_BACKGROUND_ANIMATION_SUMMER -> textViewAnimation.text = getString(R.string.settings_theme_summer)
                        }
                    } else textViewAnimation.text = getString(R.string.setting_background_animation_off)
                    isBackgroundAnimationStateChanged = backgroundAnimationState != settings.backgroundAnimationState
                    isBackgroundMusicStateChanged = backgroundMusicState != settings.backgroundMusicState
                    isBackgroundAnimationChanged = backgroundAnimation != settings.backgroundAnimation
                }
                is SurnameFragment -> {
                    surnameState = (fragment as SurnameFragment).surnameState
                    surname = if ((fragment as SurnameFragment).surnameState) (fragment as SurnameFragment).surname else ""
                    Log.d("SA", "onBackPressed - surnameState: $surnameState | surname: $surname")
                    if (surnameState) {
                        if (surname.isNotEmpty()) textViewSurname.text = surname
                        else {
                            surnameState = false
                            textViewSurname.text = getString(R.string.setting_surname_off)
                        }
                    } else textViewSurname.text = getString(R.string.setting_surname_off)
                    isSurnameStateChanged = surnameState != settings.surnameState
                    isSurnameChanged = surname != settings.surname
                    (fragment as SurnameFragment).removeTextChangedListener()
                }
            }
            hideFragment()
            checkApplyButtonEnable()
        } else {
            if (isReset) goToSplashActivity() else super.onBackPressed()
        }
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

    private fun goToSplashActivity() {
        startActivity(Intent(this@SettingsActivity, SplashActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }

}