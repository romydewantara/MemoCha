package com.example.kuntan

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
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kuntan.adapter.CategoryAdapter
import com.example.kuntan.adapter.DashboardScheduleAdapter
import com.example.kuntan.adapter.PaymentMethodSpinnerAdapter
import com.example.kuntan.entity.History
import com.example.kuntan.entity.Settings
import com.example.kuntan.fragment.IdentityScreenFragment
import com.example.kuntan.lib.CalendarDialog
import com.example.kuntan.lib.SelectorItemsBottomSheet
import com.example.kuntan.utility.AppUtil
import com.example.kuntan.utility.Constant
import com.example.kuntan.utility.KuntanRoomDatabase
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
import java.util.*

class DashboardActivity : AppCompatActivity() {

    companion object {
        private lateinit var mediaPlayer: MediaPlayer
    }
    private val TAG = "DashboardActivity"
    private val database by lazy { KuntanRoomDatabase(this) }
    private var isMenuHidden = false
    private var isMenuAnimating = false
    private var isIdentityShown = false
    private var isDefaultSchedule = true
    private var username = ""
    private var currentDate = ""
    private var paymentMethod = ""
    private var index = 0
    private var arrayTimes = arrayListOf(
        Times.Subuh.name,
        Times.Dzuhur.name,
        Times.Ashar.name,
        Times.Maghrib.name,
        Times.Isya.name
    )

    private lateinit var fragment: Fragment
    private lateinit var constraintActivityMain: ConstraintLayout
    private lateinit var constraintMainMenu: ConstraintLayout
    private lateinit var constraintMainMenuContainer: ConstraintLayout
    private lateinit var dashboardScheduleAdapter: DashboardScheduleAdapter
    private lateinit var selectorItemsBottomSheet: SelectorItemsBottomSheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        init()
        setupRecyclerView()
        initListener()
    }

    override fun onStart() {
        super.onStart()
        refreshSchedule()
        adjustSettings()
    }

    override fun onPause() {
        super.onPause()
        lottieDashboard.cancelAnimation()
        lottieDashboard.clearAnimation()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
            mediaPlayer = MediaPlayer()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun init() {
        mediaPlayer = MediaPlayer()
        constraintActivityMain = findViewById(R.id.constraintActivityMain)
        constraintMainMenu = findViewById(R.id.constraintMainMenu)
        constraintMainMenuContainer = findViewById(R.id.constraintMainMenuContainer)
        constraintActivityMain.viewTreeObserver.addOnGlobalLayoutListener {
            if (AppUtil.isKeyboardVisible(constraintActivityMain)) {
                iconArrows.setImageResource(R.drawable.ic_arrows_disabled)
                iconArrows.isEnabled = false
                hideMenu()
                isMenuHidden = true
            } else {
                iconArrows.setImageResource(R.drawable.ic_arrows)
                iconArrows.isEnabled = true
                editTextGoods.isFocusable = false
                editTextAmount.isFocusable = false
                editTextNote.isFocusable = false
            }
        }
        val calendar = Calendar.getInstance() //English: Friday, September 16th 2022 | Indonesia: Jum\'at, 16 September 2022
        currentDate = SimpleDateFormat("dd-MM-yyyy").format(calendar.time)
        textDate.text = SimpleDateFormat("EEEE, MMMM dd yyyy").format(calendar.time)
        textViewCalendar.text = SimpleDateFormat("dd/MM/yyyy").format(calendar.time)
        time.text = arrayTimes[index]
        layoutCalendar.visibility = VISIBLE
        textViewCalendar.text = currentDate.replace("-","/")

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
    }

    private fun setupRecyclerView() {
        dashboardScheduleAdapter = DashboardScheduleAdapter(arrayListOf())
        recyclerviewSchedule.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = dashboardScheduleAdapter
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SimpleDateFormat")
    private fun adjustSettings() {
        CoroutineScope(Dispatchers.IO).launch {
            val settings = database.settingsDao().getSettings()
            Log.d(TAG, "checkSettings - settings: $settings")
            if (settings == null) {
                val setting = Settings(0, username, Constant.APP_THEME_LIGHT, getString(R.string.setting_language_english), Constant.DASHBOARD_CLOCK_PRIMARY, getString(R.string.setting_background_animation_off), Constant.DASHBOARD_BACKGROUND_AUTUMN, getString(R.string.setting_background_music_off))
                database.settingsDao().insertSetting(setting)
            } else {
                runOnUiThread {
                    if (settings.language == getString(R.string.setting_language_bahasa)) {
                        textDate.text = SimpleDateFormat("EEEE, dd MMMM yyyy").format(Calendar.getInstance().time)
                    }
                    analogClock.background = AppUtil.convertDrawableFromTheme(applicationContext, settings.analogClockTheme)
                    if (settings.backgroundAnimation == getString(R.string.setting_background_animation_on)) {
                        lottieDashboard.visibility = VISIBLE
                        lottieDashboard.setAnimation(settings.dashboardBackground)
                        lottieDashboard.playAnimation()
                        if (settings.backgroundMusicState == getString(R.string.setting_background_music_on)) {
                            var fileName = ""
                            when(settings.dashboardBackground) {
                                Constant.DASHBOARD_BACKGROUND_AUTUMN -> {fileName = Constant.BACKGROUND_MUSIC_AUTUMN_THEME}
                                Constant.DASHBOARD_BACKGROUND_SAKURA -> {fileName = Constant.BACKGROUND_MUSIC_SAKURA_THEME}
                                Constant.DASHBOARD_BACKGROUND_SNOW -> {fileName = Constant.BACKGROUND_MUSIC_SNOW_THEME}
                                Constant.DASHBOARD_BACKGROUND_SUMMER -> {fileName = Constant.BACKGROUND_MUSIC_SUMMER_THEME}
                            }
                            Log.d(TAG, "checkSettings: fileName: $fileName")
                            val afd : AssetFileDescriptor = assets.openFd(fileName)
                            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                            mediaPlayer.setVolume(1f, 1f)
                            mediaPlayer.isLooping = true
                            mediaPlayer.prepare()
                            mediaPlayer.start()
                        }
                    } else {
                        lottieDashboard.visibility = GONE
                    }
                }
            }
        }
    }

    private fun refreshSchedule() {
        CoroutineScope(Dispatchers.IO).launch {
            val schedules = database.scheduleDao().getSchedule(arrayTimes[index])
            Log.d(TAG, "onStart - schedules: $schedules")
            if (schedules.isEmpty() && isDefaultSchedule) {

            } else {
                withContext(Dispatchers.Main) {
                    dashboardScheduleAdapter.setData(schedules)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SimpleDateFormat")
    private fun initListener() {
        overlayLayout.setOnClickListener {}
        setting.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, SettingsActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }
        time.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, ScheduleActivity::class.java)
                .putExtra("time", arrayTimes[index]).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }
        previousTime.setOnClickListener {
            index--
            if (index < 0) index = arrayTimes.size - 1
            time.text = arrayTimes[index]
            refreshSchedule()
        }
        nextTime.setOnClickListener {
            index++
            if (index >= arrayTimes.size) index = 0
            time.text = arrayTimes[index]
            refreshSchedule()
        }
        menuBook.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, WebViewActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }
        menuIdentity.setOnClickListener {
            placeHolderLayout.x = resources.displayMetrics.widthPixels.toFloat()
            fragment = IdentityScreenFragment(applicationContext).addIdentityListener(
                object : IdentityScreenFragment.IdentityListener {
                override fun onIdentityScreenCreated() {
                    showOverlayLayout()
                    placeHolderLayout.animate().translationX(0f)
                    isIdentityShown = true
                }
            })
            supportFragmentManager.beginTransaction().replace(R.id.placeHolderLayout, fragment, "identity").commit()
            placeHolderLayout.visibility = VISIBLE
        }
        menuNeeds.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, NeedsActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }
        menuHistory.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, HistoryActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }
        iconArrows.setOnClickListener {
            //show-hide Menu
            Log.d(TAG, "onCreate - isMenuHidden: $isMenuHidden")
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
            AppUtil.hideSoftKeyboard(constraintActivityMain, applicationContext)
            CoroutineScope(Dispatchers.IO).launch {
                database.historyDao().insert(
                    History(
                        0,
                        currentDate.split("-")[2],
                        currentDate.split("-")[1],
                        currentDate.split("-")[0],
                        SimpleDateFormat("HH:mm").format(Date()),
                        currentDate.split("-")[2],
                        currentDate.split("-")[1],
                        currentDate.split("-")[0],
                        SimpleDateFormat("HH:mm").format(Date()),
                        editTextGoods.text.toString().trim(),
                        editTextAmount.text.toString().trim(),
                        editTextNote.text.toString().trim(),
                        textViewCategory.text.toString(),
                        paymentMethod
                    )
                )
                runOnUiThread {
                    //Reset form
                    editTextGoods.setText("")
                    editTextAmount.setText("")
                    editTextNote.setText("")
                    textViewCategory.text = getString(R.string.category_others)
                    layoutPaymentMethod.setSelection(0)
                    Snackbar.make(constraintActivityMain, getString(R.string.snackbar_monthly_expenses_added),
                        Snackbar.LENGTH_INDEFINITE).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).setAction("DISMISS") {}.show()
                }
            }
        }
        textViewCategoryTitle.setOnClickListener {
            selectorItemsBottomSheet = SelectorItemsBottomSheet(getString(R.string.category_title),
                CategoryAdapter(applicationContext, object : CategoryAdapter.CategoryAdapterListener {
                override fun onCategoryClicked(category: String) {
                    selectorItemsBottomSheet.dismiss()
                    textViewCategory.text = category
                }
            }))
            selectorItemsBottomSheet.show(supportFragmentManager, "selector_bottom_sheet")
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

        val sortType = arrayListOf(getString(R.string.method_cash), getString(R.string.method_debit), getString(R.string.method_transfer))
        val spinnerPaymentMethodAdapter = PaymentMethodSpinnerAdapter(applicationContext,
            sortType, object : PaymentMethodSpinnerAdapter.ItemSelectedListener {
                override fun onItemSelected(selectedItem: String) {
                    paymentMethod = selectedItem
                }
            })
        layoutPaymentMethod.adapter = spinnerPaymentMethodAdapter

        layoutCalendar.setOnClickListener {
            val calendarDialog = CalendarDialog()
            calendarDialog.newInstance(applicationContext, SimpleDateFormat("yyyy-MM-dd").format(Date()))
                .onDateChangeListener(object : CalendarDialog.DateChangeListener {
                    override fun onDateChanges(dateSelected: String) {
                        currentDate = dateSelected
                        textViewCalendar.text = dateSelected.replace("-","/")
                    }
                })
            calendarDialog.show(supportFragmentManager, calendarDialog.tag)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
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

    private fun showMenu() {
        if (!isMenuAnimating) {
            val xTarget = constraintActivityMain.x
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
            val xTarget = constraintActivityMain.x + constraintMainMenu.width
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

    override fun onBackPressed() {
        if (isIdentityShown) {
            placeHolderLayout.animate().translationX(resources.displayMetrics.widthPixels.toFloat()).setListener(object : Animator.AnimatorListener{
                override fun onAnimationStart(animation: Animator?) {
                    hideOverlayLayout()
                }
                override fun onAnimationEnd(animation: Animator?) {
                    placeHolderLayout.visibility = GONE
                    isIdentityShown = false
                }
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            })
        } else {
            super.onBackPressed()
        }
    }
}