package com.example.memocha

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.View.INVISIBLE
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memocha.adapter.CategoryAdapter
import com.example.memocha.adapter.DashboardScheduleAdapter
import com.example.memocha.adapter.PaymentMethodSpinnerAdapter
import com.example.memocha.entity.History
import com.example.memocha.entity.Settings
import com.example.memocha.fragment.IdentityScreenFragment
import com.example.memocha.fragment.NeedsScreenFragment
import com.example.memocha.lib.CalendarDialog
import com.example.memocha.lib.SelectorItemsBottomSheet
import com.example.memocha.utility.AppUtil
import com.example.memocha.utility.Constant
import com.example.memocha.utility.MemoChaRoomDatabase
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.layout_monthly_expenses_notes.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

@SuppressLint("ClickableViewAccessibility", "UseCompatLoadingForDrawables", "SimpleDateFormat", "NewApi")
class DashboardActivity : AppCompatActivity() {

    companion object {
        const val TAG = "DashboardActivity"
        const val PAGE_DASHBOARD = "dashboard"
        const val PAGE_MONTH_CHOOSER = "month_chooser"
    }

    private val database by lazy { MemoChaRoomDatabase(this) }

    //Wisdom Text
    private var timer: Timer? = null
    private var task: TimerTask? = null
    private val listOfWisdomUsed = arrayListOf<String>()

    //Settings
    private var surnameState = false
    private var backgroundAnimationState = false
    private var backgroundMusicState = false
    private var notificationState = false
    private var badgeState = false
    private var surname = ""
    private var applicationTheme = ""
    private var applicationLanguage = ""
    private var clockTheme = ""
    private var backgroundAnimation = ""

    //History
    private var currentDate = ""
    private var currentDay = ""
    private var currentTime = ""
    private var customDate = ""
    private var paymentMethod = ""

    //Schedule
    private var timeId = 0
    private var timeList = arrayListOf(
        ScheduleActivity.subuh,
        ScheduleActivity.dzuhur,
        ScheduleActivity.ashar,
        ScheduleActivity.maghrib,
        ScheduleActivity.isya
    )

    //Dashboard Flags
    private var isMenuHidden = false
    private var isMenuAnimating = false
    private var isFragmentShown = false

    //Common of Dashboard Environment
    private lateinit var fragment: Fragment
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var dashboardScheduleAdapter: DashboardScheduleAdapter
    private lateinit var paymentMethodSpinnerAdapter: PaymentMethodSpinnerAdapter
    private lateinit var selectorCategory: SelectorItemsBottomSheet

    //Simple Date Format
    private lateinit var formatDate: SimpleDateFormat
    private lateinit var formatDay: SimpleDateFormat
    private lateinit var formatTime: SimpleDateFormat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        init()
        initListener()
    }

    override fun onStart() {
        super.onStart()
        startWisdomTask()
        refreshSchedule()
        findCurrentScheduleTime()
        adjustSettings()
    }

    override fun onPause() {
        super.onPause()
        if (task != null) {
            task?.cancel()
            timer?.cancel()
            task = null
            timer = null
        }
        lottieDashboard.cancelAnimation()
        lottieDashboard.clearAnimation()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
            mediaPlayer = MediaPlayer()
        }
    }

    private fun init() {
        /** init settings **/
        mediaPlayer = MediaPlayer()
        surname = ""
        applicationTheme = Constant.APP_THEME_LIGHT
        applicationLanguage = getString(R.string.setting_language_english)
        clockTheme = getString(R.string.clock_theme_primary)
        backgroundAnimation = Constant.DASHBOARD_BACKGROUND_ANIMATION_AUTUMN
        surnameState = false
        backgroundAnimationState = false
        backgroundMusicState = false

        /** add global layout listener to root layout **/
        rootDashboardLayout.viewTreeObserver.addOnGlobalLayoutListener {
            if (AppUtil.isKeyboardVisible(rootDashboardLayout)) {
                iconArrows.setImageResource(R.drawable.ic_arrows_disabled)
                iconArrows.isEnabled = false
                loveMessage.visibility = GONE
                layoutClock.visibility = GONE
                textGreetings.visibility = GONE
                time.visibility = INVISIBLE //only this view is INVISIBLE bcs give space of view to CurrentDate
                previousTime.visibility = GONE
                nextTime.visibility = GONE
                hideMenu()
                isMenuHidden = true
            } else {
                iconArrows.setImageResource(R.drawable.ic_arrows)
                iconArrows.isEnabled = true
                editTextGoods.isFocusable = false
                editTextAmount.isFocusable = false
                editTextNote.isFocusable = false
                loveMessage.visibility = VISIBLE
                if (surnameState) textGreetings.visibility = VISIBLE
                layoutClock.visibility = VISIBLE
                time.visibility = VISIBLE
                previousTime.visibility = VISIBLE
                nextTime.visibility = VISIBLE
            }
        }

        /** init simple date format **/
        val calendar = Calendar.getInstance() //English: Friday, September 16th 2022 | Indonesia: Jum\'at, 16 September 2022
        formatDate = SimpleDateFormat("dd-MM-yyyy", Locale(resources.configuration.locales[0].language))
        formatDay = SimpleDateFormat("EEEE", Locale(resources.configuration.locales[0].language))
        formatTime = SimpleDateFormat("HH:mm", Locale(resources.configuration.locales[0].language))

        currentDate = formatDate.format(calendar.time)
        currentDay = formatDay.format(calendar.time)
        customDate = currentDate

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textViewWisdom, 1,
            12, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textViewCategory, 1,
            14, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textBook, 1,
            14, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textIdentity, 1,
            14, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textNeeds, 1,
            14, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textHistory, 1,
            14, 1, TypedValue.COMPLEX_UNIT_SP)

        val mTextDate = "$currentDay, ${AppUtil.convertMonthNameFromCode(this, currentDate.
        split("-")[1])} ${currentDate.split("-")[0]} ${currentDate.split("-")[2]}"

        textDate.text = mTextDate
        time.text = timeList[timeId]
        textViewCalendar.text = currentDate.replace("-","/")
        layoutCalendar.visibility = VISIBLE

        val sortType = arrayListOf(getString(R.string.method_cash), getString(R.string.method_debit), getString(R.string.method_transfer))
        paymentMethodSpinnerAdapter = PaymentMethodSpinnerAdapter(applicationContext, sortType)
        layoutPaymentMethod.adapter = paymentMethodSpinnerAdapter

        dashboardScheduleAdapter = DashboardScheduleAdapter(this, arrayListOf())
        recyclerviewSchedule.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = dashboardScheduleAdapter
        }
    }

    private fun adjustSettings() {
        CoroutineScope(Dispatchers.IO).launch {
            val settings = database.settingsDao().getSettings()
            Log.d(TAG, "adjustSettings - settings: $settings")
            if (settings == null) {
                applicationTheme = Constant.APP_THEME_LIGHT
                applicationLanguage = getString(R.string.setting_language_english)
                clockTheme = Constant.DASHBOARD_CLOCK_PRIMARY
                val defaultSetting = Settings(0, surname, applicationTheme, applicationLanguage,
                    clockTheme, backgroundAnimation, surnameState, backgroundAnimationState,
                    backgroundMusicState, notificationState, badgeState)
                database.settingsDao().insertSetting(defaultSetting)
            } else {
                runOnUiThread {
                    surnameState = settings.surnameState
                    backgroundAnimationState = settings.backgroundAnimationState
                    backgroundMusicState = settings.backgroundMusicState
                    notificationState = settings.notificationState
                    badgeState = settings.badgeState
                    surname = settings.surname
                    applicationTheme = settings.applicationTheme
                    applicationLanguage = settings.applicationLanguage
                    clockTheme = settings.clockTheme
                    backgroundAnimation = settings.backgroundAnimation

                    analogClock.background = AppUtil.convertDrawableFromTheme(this@DashboardActivity, clockTheme)
                    if (applicationLanguage == getString(R.string.setting_language_bahasa))
                        textDate.text = String.format("$currentDay, ${currentDate.split("-")[0]} ${
                            AppUtil.convertMonthNameFromCode(this@DashboardActivity, currentDate.
                            split("-")[1])} ${currentDate.split("-")[2]}")
                    if (surnameState) {
                        textGreetings.text = String.format(getString(R.string.dasboard_greetings), AppUtil.getTimesName(this@DashboardActivity), surname)
                        textGreetings.visibility = VISIBLE
                    } else textGreetings.visibility = GONE
                    if (backgroundAnimationState) {
                        lottieDashboard.visibility = VISIBLE
                        lottieDashboard.setAnimation(backgroundAnimation)
                        lottieDashboard.playAnimation()
                        if (backgroundMusicState) {
                            var fileName = ""
                            when(backgroundAnimation) {
                                Constant.DASHBOARD_BACKGROUND_ANIMATION_AUTUMN -> {fileName = Constant.BACKGROUND_MUSIC_AUTUMN_THEME}
                                Constant.DASHBOARD_BACKGROUND_ANIMATION_SAKURA -> {fileName = Constant.BACKGROUND_MUSIC_SAKURA_THEME}
                                Constant.DASHBOARD_BACKGROUND_ANIMATION_SNOW -> {fileName = Constant.BACKGROUND_MUSIC_SNOW_THEME}
                                Constant.DASHBOARD_BACKGROUND_ANIMATION_SUMMER -> {fileName = Constant.BACKGROUND_MUSIC_SUMMER_THEME}
                            }
                            val afd : AssetFileDescriptor = assets.openFd(fileName)
                            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                            mediaPlayer.setVolume(1f, 1f)
                            mediaPlayer.isLooping = true
                            mediaPlayer.prepare()
                            mediaPlayer.start()
                        }
                    } else lottieDashboard.visibility = GONE
                }
            }
        }
    }

    private fun startWisdomTask() {
        timer = Timer()
        task = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    textViewWisdom.visibility = GONE
                    textViewWisdom.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out))

                    textViewWisdom.text = AppUtil.randomWisdom(this@DashboardActivity, listOfWisdomUsed)
                    textViewWisdom.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
                    textViewWisdom.visibility = VISIBLE
                }
            }
        }
        timer?.scheduleAtFixedRate(task, 0L, 12000L)
    }

    private fun refreshSchedule() {
        currentTime = formatTime.format(Calendar.getInstance().time)
        CoroutineScope(Dispatchers.IO).launch {
            val schedules = database.scheduleDao().getSchedule(timeList[timeId])
            runOnUiThread {
                if (schedules.isEmpty()) textViewEmptySchedule.visibility = VISIBLE
                else textViewEmptySchedule.visibility = GONE
            }
            withContext(Dispatchers.Main) {
                dashboardScheduleAdapter.setData(currentTime.split(":")[0], schedules)
            }
        }
    }

    private fun findCurrentScheduleTime() {
        val currentHour = formatTime.format(GregorianCalendar.getInstance().time)
        timeId = when(currentHour.split(":")[0].toInt()) {
            in 5..11 -> { 0 }
            in 12..14 -> { 1 }
            in 15..17 -> { 2 }
            in 18..18 -> { 3 }
            else -> { 4 }
        }
        time.text = timeList[timeId]
    }

    private fun initListener() {
        setting.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, SettingsActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }
        time.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, ScheduleActivity::class.java)
                .putExtra("time", timeList[timeId]).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }
        previousTime.setOnClickListener {
            timeId--
            if (timeId < 0) timeId = timeList.size - 1
            time.text = timeList[timeId]
            refreshSchedule()
        }
        nextTime.setOnClickListener {
            timeId++
            if (timeId >= timeList.size) timeId = 0
            time.text = timeList[timeId]
            refreshSchedule()
        }
        menuBook.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, WebViewActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }
        menuIdentity.setOnClickListener {
            isFragmentShown = true
            showOverlayLayout()
            placeHolderLayout.x = resources.displayMetrics.widthPixels.toFloat()
            fragment = IdentityScreenFragment(applicationContext)
                .addIdentityListener(object : IdentityScreenFragment.IdentityListener {
                    override fun onIdentityScreenCreated() {
                        placeHolderLayout.visibility = VISIBLE
                        val objectAnimator = ObjectAnimator.ofFloat(placeHolderLayout, "translationX", 0f)
                        objectAnimator.duration = 500L
                        objectAnimator.start()
                        objectAnimator.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator?) {}
                            override fun onAnimationEnd(animation: Animator?) {
                                resetExpenseNote()
                            }
                            override fun onAnimationCancel(animation: Animator?) {}
                            override fun onAnimationRepeat(animation: Animator?) {}
                        })
                    }
                })
            supportFragmentManager.beginTransaction().replace(R.id.placeHolderLayout, fragment, "identity").commit()
        }
        menuNeeds.setOnClickListener {
            isFragmentShown = true
            showOverlayLayout()
            placeHolderLayout.x = resources.displayMetrics.widthPixels.toFloat()
            fragment = NeedsScreenFragment()
                .addOnNeedScreenListener(object : NeedsScreenFragment.NeedsScreenListener {
                    override fun onScreenCreated() {
                        placeHolderLayout.visibility = VISIBLE
                        val objectAnimator = ObjectAnimator.ofFloat(placeHolderLayout, "translationX", 0f)
                        objectAnimator.duration = 500L
                        objectAnimator.start()
                        objectAnimator.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator?) {}
                            override fun onAnimationEnd(animation: Animator?) {
                                (fragment as NeedsScreenFragment).addPreviousPage(PAGE_DASHBOARD)
                                (fragment as NeedsScreenFragment).populateNeeds()
                                resetExpenseNote()
                            }
                            override fun onAnimationCancel(animation: Animator?) {}
                            override fun onAnimationRepeat(animation: Animator?) {}
                        })
                    }
                    override fun onPopulateData() {
                        needsLoading.visibility = VISIBLE
                    }
                    override fun onDataPopulated() {
                        needsLoading.visibility = GONE
                    }
                })
            supportFragmentManager.beginTransaction().replace(R.id.placeHolderLayout, fragment, "needs").commit()
        }
        menuHistory.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, HistoryDetailsActivity::class.java)
                .putExtra("month", currentDate.split("-")[1])
                .putExtra("year", currentDate.split("-")[2])
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }
        iconArrows.setOnClickListener {
            isMenuHidden = !isMenuHidden
            if (!isMenuHidden) showMenu() else hideMenu()
        }
        loveMessage.setOnClickListener {
            Toast.makeText(applicationContext, getString(R.string.secret_letter_coming_soon), Toast.LENGTH_SHORT).show()
            //val secretLetterDialog = SecretLetterDialog(applicationContext)
            //placeHolderLayout.addView(secretLetterDialog)
            //placeHolderLayout.visibility = VISIBLE
        }
        imageAdd.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                currentTime = formatTime.format(Calendar.getInstance().time)
                database.historyDao().insert(
                    History(
                        0,
                        customDate.split("-")[2],
                        customDate.split("-")[1],
                        customDate.split("-")[0],
                        currentTime,
                        customDate.split("-")[2],
                        customDate.split("-")[1],
                        customDate.split("-")[0],
                        currentTime,
                        editTextGoods.text.toString().trim(),
                        editTextAmount.text.toString().trim(),
                        editTextNote.text.toString().trim(),
                        textViewCategory.text.toString(),
                        paymentMethod, false, false
                    )
                )
                runOnUiThread {
                    //Reset form
                    editTextGoods.setText("")
                    editTextAmount.setText("")
                    editTextNote.setText("")
                    textViewCategory.text = getString(R.string.category_others)
                    layoutPaymentMethod.setSelection(0)
                    editTextGoods.requestFocus()
                    Snackbar.make(rootDashboardLayout, getString(R.string.snackbar_monthly_expenses_added),
                        Snackbar.LENGTH_LONG).setAction(getString(R.string.snackbar_button_dismiss)) {
                            AppUtil.hideSoftKeyboard(rootDashboardLayout, this@DashboardActivity)
                    }.show()
                }
            }
        }
        textViewCategoryTitle.setOnClickListener {
            selectorCategory = SelectorItemsBottomSheet(getString(R.string.category_title),
                CategoryAdapter(applicationContext, object : CategoryAdapter.CategoryAdapterListener {
                override fun onCategoryClicked(category: String) {
                    selectorCategory.dismiss()
                    textViewCategory.text = category
                }
            }))
            selectorCategory.show(supportFragmentManager, "selector_bottom_sheet")
        }
        editTextGoods.addTextChangedListener(onTextChangedListener(false))
        editTextGoods.setOnTouchListener { _, _ ->
            editTextGoods.isFocusableInTouchMode = true
            false
        }
        editTextAmount.setOnTouchListener { _, _ ->
            editTextAmount.isFocusableInTouchMode = true
            false
        }
        editTextNote.setOnTouchListener { _, _ ->
            editTextNote.isFocusableInTouchMode = true
            false
        }
        editTextAmount.addTextChangedListener(onTextChangedListener(true))
        paymentMethodSpinnerAdapter.addOnPaymentMethodListener(
            object : PaymentMethodSpinnerAdapter.ItemSelectedListener {
            override fun onItemSelected(selectedItem: String) {
                paymentMethod = selectedItem
            }
        })
        layoutCalendar.setOnClickListener {
            val calendarDialog = CalendarDialog()
            calendarDialog.newInstance(applicationContext, customDate)
                .onDateChangeListener(object : CalendarDialog.DateChangeListener {
                    override fun onDateChanges(dateSelected: String) {
                        customDate = dateSelected
                        textViewCalendar.text = dateSelected.replace("-","/")
                        if (!isDateEqualsToday()) startPulsateAnimation() else stopPulsateAnimation()
                    }
                })
            calendarDialog.show(supportFragmentManager, calendarDialog.tag)
        }
    }

    private fun onTextChangedListener(isAmount: Boolean) : TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isAmount) {
                    editTextAmount.removeTextChangedListener(this)
                    try {
                        var originalString = s.toString()
                        if (originalString.contains(",")) originalString = originalString.replace(",", "")
                        val longValue: Long = java.lang.Long.parseLong(originalString)
                        val formatter = NumberFormat.getInstance(Locale.US) as DecimalFormat
                        formatter.applyPattern("#,###,###,###")
                        val formattedString = formatter.format(longValue)
                        editTextAmount.setText(formattedString)
                        editTextAmount.setSelection(editTextAmount.text.length)

                    } catch (nfe: NumberFormatException) {
                        nfe.printStackTrace()
                    }
                    editTextAmount.addTextChangedListener(this)
                }
                if (editTextGoods.text.toString().isNotEmpty() && editTextAmount.text.toString().isNotEmpty()) {
                    imageAdd.isEnabled = true
                    imageAdd.background = applicationContext.resources.getDrawable(R.drawable.selector_button_send_needs_item, null)
                } else {
                    imageAdd.isEnabled = false
                    imageAdd.background = applicationContext.resources.getDrawable(R.drawable.background_button_send_disabled, null)
                }
            }
        }
    }

    private fun resetExpenseNote() {
        layoutPaymentMethod.setSelection(0)
        paymentMethod = getString(R.string.method_cash)
        editTextGoods.setText("")
        editTextAmount.setText("")
        editTextNote.setText("")
        if (!isDateEqualsToday()) {
            stopPulsateAnimation()
            customDate = currentDate
            textViewCalendar.text = currentDate
        }
    }

    private fun isDateEqualsToday(): Boolean {
        return customDate == currentDate
    }

    private fun startPulsateAnimation() {
        val firstPulsate = AnimationUtils.loadAnimation(this, R.anim.first_pulsate)
        val secondPulsate = AnimationUtils.loadAnimation(this, R.anim.second_pulsate)
        firstPulse.startAnimation(firstPulsate)
        secondPulse.startAnimation(secondPulsate)
        firstPulse.visibility = VISIBLE
        secondPulse.visibility = VISIBLE
    }

    private fun stopPulsateAnimation() {
        firstPulse.clearAnimation()
        secondPulse.clearAnimation()
        firstPulse.visibility = INVISIBLE
        secondPulse.visibility = INVISIBLE
    }

    private fun showMenu() {
        if (!isMenuAnimating) {
            val xTarget = rootDashboardLayout.x
            val objectAnimator =
                ObjectAnimator.ofFloat(constraintMainMenuContainer, "translationX", xTarget)
                    .setDuration(200)
            val animatorSet = AnimatorSet()
            animatorSet.play(objectAnimator)
            animatorSet.start()
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    isMenuAnimating = true
                }
                override fun onAnimationEnd(animation: Animator?) {
                    isMenuAnimating = false
                }
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            })
        }
    }

    private fun hideMenu() {
        if (!isMenuAnimating) {
            val xTarget = rootDashboardLayout.x + constraintMainMenu.width
            val objectAnimator =
                ObjectAnimator.ofFloat(constraintMainMenuContainer, "translationX", xTarget)
                    .setDuration(300)
            val animatorSet = AnimatorSet()
            animatorSet.play(objectAnimator)
            animatorSet.start()
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    isMenuAnimating = true
                }
                override fun onAnimationEnd(animation: Animator?) {
                    isMenuAnimating = false
                }
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            })
        }
    }

    private fun showOverlayLayout() {
        overlayLayout.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.white_overlay_fade_in))
        overlayLayout.visibility = VISIBLE
    }

    private fun hideOverlayLayout() {
        overlayLayout.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.white_overlay_fade_out))
        overlayLayout.visibility = GONE
    }

    private fun hidePlaceHolderLayout() {
        val objectAnimator = ObjectAnimator.ofFloat(
            placeHolderLayout, "translationX", resources.displayMetrics.widthPixels.toFloat())
        objectAnimator.duration = 500L
        objectAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
                hideOverlayLayout()
            }
            override fun onAnimationEnd(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}
        })
        objectAnimator.start()
    }

    override fun onBackPressed() {
        if (isFragmentShown) {
            when(fragment) {
                is IdentityScreenFragment -> {
                    val frag = (fragment as IdentityScreenFragment)
                    if (frag.isContainerShown) {
                        frag.hideContainerIdentity()
                    } else {
                        isFragmentShown = false
                        hidePlaceHolderLayout()
                    }
                }
                is NeedsScreenFragment -> {
                    (fragment as NeedsScreenFragment).removeListener()
                    isFragmentShown = false
                    hidePlaceHolderLayout()
                }
            }
        } else {
            super.onBackPressed()
        }
    }
}