package com.example.kuntan

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kuntan.adapter.DashboardScheduleAdapter
import com.example.kuntan.adapter.PaymentMethodSpinnerAdapter
import com.example.kuntan.entity.History
import com.example.kuntan.lib.CategoryBottomSheet
import com.example.kuntan.utility.AppUtil
import com.example.kuntan.utility.KuntanRoomDatabase
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.layout_main_menu.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private val TAG = "DashboardActivity"
    private val database by lazy { KuntanRoomDatabase(this) }
    private var isMenuHidden = false
    private var isAnimating = false
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

    private lateinit var dashboardScheduleAdapter: DashboardScheduleAdapter
    private lateinit var constraintActivityMain: ConstraintLayout
    private lateinit var constraintMainMenu: ConstraintLayout
    private lateinit var constraintMainMenuContainer: ConstraintLayout

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        constraintActivityMain = findViewById(R.id.constraintActivityMain)
        constraintMainMenu = findViewById(R.id.constraintMainMenu)
        constraintMainMenuContainer = findViewById(R.id.constraintMainMenuContainer)

        constraintActivityMain.viewTreeObserver.addOnGlobalLayoutListener {
            if (AppUtil.isKeyboardVisible(constraintActivityMain)) {
                iconArrows.isEnabled = false
                hideMenu()
                isMenuHidden = true
            } else {
                iconArrows.isEnabled = true
                editTextGoods.isFocusable = false
                editTextAmount.isFocusable = false
                editTextNote.isFocusable = false
            }
        }

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textViewCategory, 1,
            14, 1, TypedValue.COMPLEX_UNIT_SP)
        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("EEEE, dd MMMM, yyyy").format(calendar.time)
        currentDate = SimpleDateFormat("dd-MMMM-yyyy").format(calendar.time)
        textDate.text = date
        time.text = arrayTimes[index]

        setupRecyclerView()
        initListener()
    }

    override fun onStart() {
        super.onStart()
        refreshSchedule()
    }

    private fun refreshSchedule() {
        CoroutineScope(Dispatchers.IO).launch {
            val schedules = database.scheduleDao().getSchedule(arrayTimes[index])
            Log.d(TAG, "onStart - schedules: $schedules")
            withContext(Dispatchers.Main) {
                dashboardScheduleAdapter.setData(schedules)
            }
        }
    }

    private fun setupRecyclerView() {
        dashboardScheduleAdapter = DashboardScheduleAdapter(arrayListOf())
        recyclerviewSchedule.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = dashboardScheduleAdapter
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SimpleDateFormat")
    private fun initListener() {
        setting.setOnClickListener {
            //goToSettings
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
        book.setOnClickListener {
            //goToBookPage
            startActivity(Intent(this@DashboardActivity, WebViewActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }
        question.setOnClickListener {
            //goToQuestionPage
        }
        cart.setOnClickListener {
            //goToCartPage
        }
        history.setOnClickListener {
            startActivity(Intent(this@DashboardActivity, HistoryActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }
        iconArrows.setOnClickListener {
            //show-hide Menu
            Log.d(TAG, "onCreate - isMenuHidden: $isMenuHidden")
            isMenuHidden = !isMenuHidden
            if (!isMenuHidden) showMenu() else hideMenu()
        }
        textViewSave.setOnClickListener {
            val isGoodsFilled: Boolean
            val isAmountFilled: Boolean
            if (editTextGoods.text.toString().isEmpty()) {
                textViewGoodsAlert.visibility = VISIBLE
                isGoodsFilled = false
            } else {
                textViewGoodsAlert.visibility = GONE
                isGoodsFilled = true
            }
            if (editTextAmount.text.toString().isEmpty()) {
                textViewAmountAlert.visibility = VISIBLE
                isAmountFilled = false
            } else {
                textViewAmountAlert.visibility = GONE
                isAmountFilled = true
            }
            if (isGoodsFilled && isAmountFilled) {
                AppUtil.hideSoftKeyboard(constraintActivityMain, applicationContext)
                CoroutineScope(Dispatchers.IO).launch {
                    database.historyDao().insert(
                        History(
                            0,
                            currentDate.split("-")[2],
                            currentDate.split("-")[1],
                            currentDate.split("-")[0],
                            SimpleDateFormat("HH:mm").format(Date()),
                            editTextGoods.text.toString(),
                            editTextAmount.text.toString(),
                            editTextNote.text.toString(),
                            textViewCategory.text.toString(),
                            paymentMethod, "20000"
                        )
                    )
                }
            }
        }
        textViewCategoryTitle.setOnClickListener {
            CategoryBottomSheet().addCategoryListener(object : CategoryBottomSheet.CategoryListener {
                override fun onCategorySelected(category: String) {
                    textViewCategory.text = category
                }
            }).show(supportFragmentManager, "category_bottom_sheet")
        }
        editTextGoods.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.isNotEmpty()) textViewGoodsAlert.visibility = GONE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
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
        editTextAmount.addTextChangedListener(onTextChangedListener())

        val sortType = arrayListOf(getString(R.string.method_cash), getString(R.string.method_debit), getString(R.string.method_transfer))
        val spinnerPaymentMethodAdapter = PaymentMethodSpinnerAdapter(applicationContext,
            sortType, object : PaymentMethodSpinnerAdapter.ItemSelectedListener {
                override fun onItemSelected(selectedItem: String) {
                    when (selectedItem) {
                        sortType[0] -> {
                            paymentMethod = getString(R.string.method_cash)
                            Log.d(TAG, "onItemSelected cash")
                        }
                        sortType[1] -> {
                            paymentMethod = getString(R.string.method_debit)
                            Log.d(TAG, "onItemSelected debit")
                        }
                        sortType[2] -> {
                            paymentMethod = getString(R.string.method_transfer)
                            Log.d(TAG, "onItemSelected trf")
                        }
                    }
                }
            })
        layoutPaymentMethod.adapter = spinnerPaymentMethodAdapter

    }

    private fun onTextChangedListener() : TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.isNotEmpty()) textViewAmountAlert.visibility = GONE
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
        }
    }
    private fun showMenu() {
        if (!isAnimating) {
            val xTarget = constraintActivityMain.x
            val objectAnimator =
                ObjectAnimator.ofFloat(constraintMainMenuContainer, "translationX", xTarget)
                    .setDuration(200)
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
            val objectAnimator =
                ObjectAnimator.ofFloat(constraintMainMenuContainer, "translationX", xTarget)
                    .setDuration(300)
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