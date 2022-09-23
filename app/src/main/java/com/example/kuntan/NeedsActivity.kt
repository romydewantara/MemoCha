package com.example.kuntan

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.TextViewCompat
import com.example.kuntan.entity.Needs
import com.example.kuntan.lib.KuntanPopupDialog
import com.example.kuntan.lib.NeedsItem
import com.example.kuntan.lib.NeedsMonthChooserBottomSheet
import com.example.kuntan.utility.AppUtil
import com.example.kuntan.utility.KuntanRoomDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_needs.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("ClickableViewAccessibility", "SimpleDateFormat", "UseCompatLoadingForDrawables")
class NeedsActivity : AppCompatActivity(), NeedsListener {

    companion object {
        const val TAG = "Needs"
        const val HEADER = "header"
        const val FOOTER = "footer"
    }

    private val database by lazy { KuntanRoomDatabase(this) }
    private var headerHeight: Int = 0
    private var footerHeight: Int = 0
    private var currentYHeader: Float = 0f
    private var currentYFooter: Float = 0f
    private var previousYLayout = 0
    private var needsContainerHeight = 0
    private var previousTextLength = 0
    private var scrollDuration = 0L
    private var tempDate = ""
    private var isInit = true
    private var isToday = true

    private lateinit var invisibleBackground: View
    private lateinit var needsListener: NeedsListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_needs)

        init()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        if (isInit) {
            isInit = false
        }
    }
    private fun init() {
        needsListener = this
        invisibleBackground = View(this@NeedsActivity)
        containerNeedsContent.addView(invisibleBackground)
        tempDate = SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time)
        val needsHeader = findViewById<ConstraintLayout>(R.id.layoutNeedsHeader)
        val needsFooter = findViewById<ConstraintLayout>(R.id.layoutNeedsFooter)
        needsHeader.post { updateLayoutSize(HEADER, needsHeader.height, needsHeader.y) }
        needsFooter.post { updateLayoutSize(FOOTER, needsFooter.height, needsFooter.y) }

        rootLayoutNeeds.viewTreeObserver.addOnGlobalLayoutListener {
            if (!AppUtil.isKeyboardVisible(rootLayoutNeeds)) editTextNeedsItem.isFocusable = false
        }
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textViewNeedsTitle,
            1, 24, 1, TypedValue.COMPLEX_UNIT_SP)

        textViewNeedsDate.text = AppUtil.convertMonthNameFromCode(this, SimpleDateFormat("MM").format(Calendar.getInstance().time))
        hideSendButton()
        populateNeeds()
    }

    private fun initListener() {
        var previousTextLength = 0
        editTextNeedsItem.addTextChangedListener(onTextChangedListener())
        editTextNeedsItem.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousTextLength = if (s != null && s.isNotEmpty()) s.length else 0
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.isNotEmpty()) {
                    if (s.isNotEmpty() && previousTextLength < 1) {
                        showSendButton()
                    }
                } else {
                    hideSendButton()
                }
            }
        })
        editTextNeedsItem.setOnTouchListener { _, _ ->
            editTextNeedsItem.isFocusableInTouchMode = true
            false
        }
        iconSend.setOnClickListener {
            if (editTextNeedsItem.text != null && editTextNeedsItem.text.toString().trim().isNotEmpty()) {
                scrollDuration = 600L
                val currentDate = SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time)
                val currentTime = SimpleDateFormat("HH:mm").format(Date())
                var isDateShown = false
                if (tempDate != currentDate) {
                    tempDate = currentDate
                    isDateShown = true
                }
                val needsObj = Needs(0, editTextNeedsItem.text.toString().trim(), currentDate, currentTime, isDateShown, false, null)
                CoroutineScope(Dispatchers.IO).launch {
                    Log.d("Needs", "initListener - needs: ${Gson().toJson(needsObj)}")
                    database.needsDao().insert(needsObj)
                    val needs = database.needsDao().getNeeds()
                    runOnUiThread {
                        for (i in needs.indices) {
                            if (i == needs.size - 1) {
                                generateNeedsItem(needs[i])
                            }
                        }
                    }
                }
            }
            editTextNeedsItem.setText("")
        }
        iconAddImage.setOnClickListener {
            //editTextNeedsItem.setText(Constant.TEST_TEXT)
        }
        imageThreeDots.setOnClickListener {
            val needsMonthChooserBottomSheet = NeedsMonthChooserBottomSheet()
            needsMonthChooserBottomSheet.addOnNeedsDateChooserListener(
                object : NeedsMonthChooserBottomSheet.NeedsDateChooserListener {
                    override fun onDateSelected(year: String, month: String) {

                    }
                })
            needsMonthChooserBottomSheet.isCancelable = false
            needsMonthChooserBottomSheet.show(supportFragmentManager, needsMonthChooserBottomSheet.tag)
        }
        //setScrollViewNeedsObserver()
        buttonScrollToTop.setOnClickListener {
            scrollToTop()
        }
        buttonScrollToBottom.setOnClickListener {
            scrollToBottom()
        }
    }

    private fun setScrollViewNeedsObserver() {
        scrollViewNeeds.setOnTouchListener { _, _ -> false }
        scrollViewNeeds.viewTreeObserver.addOnScrollChangedListener {
            val view = scrollViewNeeds.getChildAt(scrollViewNeeds.childCount - 1)
            val topDetector = scrollViewNeeds.scrollY
            val bottomDetector: Int = view.bottom - (scrollViewNeeds.height + scrollViewNeeds.scrollY)
            buttonScrollToTop.visibility = View.GONE
            buttonScrollToBottom.visibility = View.GONE
            if (bottomDetector == 0) {
                buttonScrollToTop.visibility = View.VISIBLE
                Toast.makeText(baseContext, "Scroll View bottom reached", Toast.LENGTH_SHORT).show()
            }
            if (topDetector <= 0) {
                buttonScrollToBottom.visibility = View.VISIBLE
                Toast.makeText(baseContext, "Scroll View top reached", Toast.LENGTH_SHORT).show()
            }

            Log.d(TAG, "setScrollViewNeedsObserver.. scrolling…")
        }
    }

    private fun updateLayoutSize(type: String, height: Int, y: Float) {
        when(type) {
            HEADER -> {
                headerHeight = height
                currentYHeader = y
            }
            FOOTER -> {
                footerHeight = height
                currentYFooter = y
            }
        }
        if (headerHeight > 0 && footerHeight > 0) {
            needsListener.onLayoutDrawn()
            //needsLoading.visibility = View.VISIBLE
        }
    }

    private fun populateNeeds() {
        CoroutineScope(Dispatchers.IO).launch {
            val needs = database.needsDao().getNeeds()
            Log.d("Needs", "populateNeeds (setUpdate()) - needs: ${Gson().toJson(needs)}")
            if (needs.isNotEmpty()) {
                val amount = if (needs.isNotEmpty()) needs.size else 0
                runOnUiThread {
                    for (i in needs.indices) {
                        if (tempDate != needs[i].date) {
                            tempDate = needs[i].date
                            needs[i].isDateShown = true
                        }
                        generateNeedsItem(needs[i])
                    }
                    textViewNeedsAmount.text = String.format(getString(R.string.needs_today_list), amount)
                }
            }
        }
    }

    private fun onTextChangedListener() : TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousTextLength = if (s != null && s.isNotEmpty()) s.length else 0
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.isNotEmpty()) {
                    if (s.isNotEmpty() && previousTextLength < 1) {
                        showSendButton()
                        if (s.length > 400) {
                            editTextNeedsItem.removeTextChangedListener(this)
                            try {
                                Toast.makeText(applicationContext, String.format(getString(R.string.needs_text_length_limit), s.length), Toast.LENGTH_LONG).show()
                                editTextNeedsItem.setText(s.toString())
                            } catch (nfe: NumberFormatException) {
                                nfe.printStackTrace()
                            }
                            editTextNeedsItem.addTextChangedListener(this)
                        }
                    }
                } else {
                    hideSendButton()
                }
            }
        }
    }

    private fun generateNeedsItem(needs: Needs) {
        Log.d(TAG, "generateNeedsItem - needs: ${Gson().toJson(needs)}")
        val needsItem = NeedsItem(applicationContext).getInstance(needs,
            object : NeedsItem.NeedsItemListener {
                override fun onChecked(id: Int, checked: Boolean) {
                    updateChecked(id, checked)
                }

                override fun onLongItemClicked(id: Int) {
                    val kuntanPopupDialog = KuntanPopupDialog.newInstance().setContent("Remove \"${needs.item}\"?",
                        "Be careful, it will be deleted permanently! (STILL TESTING | IT DOESN'T AFFECTS RIGHT NOW)",
                        getString(R.string.button_delete), getString(R.string.button_cancel),
                        object : KuntanPopupDialog.KuntanPopupDialogListener {
                            override fun onNegativeButton() {
                                /*CoroutineScope(Dispatchers.IO).launch {
                                    database.needsDao().deleteNeeds(id)
                                    runOnUiThread {
                                        *//*containerNeedsContent.removeAllViews()
                                        arrayListOfNeedsLayout.clear()
                                        populateNeeds()*//*
                                    }
                                }*/
                            }
                            override fun onPositiveButton() {}
                        })
                    kuntanPopupDialog.show(supportFragmentManager, kuntanPopupDialog.tag)
                }
            })

        //arrayListOfNeedsLayout.add(needsItem)
        containerNeedsContent.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    var marginTop = AppUtil.dpToPx(this@NeedsActivity, 10f)
                    if (!needs.isDateShown) marginTop = AppUtil.dpToPx(this@NeedsActivity, 3f)

                    needsItem.y = (previousYLayout + marginTop).toFloat()
                    previousYLayout += (marginTop + needsItem.height)
                    needsContainerHeight += needsItem.height + marginTop

                    val containerParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                    containerParams.height = needsContainerHeight + AppUtil.dpToPx(this@NeedsActivity, 10f)
                    containerNeedsContent.layoutParams = containerParams
                    Log.d(TAG, "onGlobalLayout - container height: ${containerNeedsContent.height} | params: ${containerNeedsContent.layoutParams.height} | measured: ${containerNeedsContent.measuredHeight}")

                    val scrollBounds = Rect()
                    scrollViewNeeds.getHitRect(scrollBounds)
                    val invisibleBackgroundParams = RelativeLayout.LayoutParams(0, (containerNeedsContent.layoutParams.height / 2))
                    invisibleBackground.layoutParams = invisibleBackgroundParams

                    scrollToBottom()
                    containerNeedsContent.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        containerNeedsContent.addView(needsItem)
    }

    private fun updateChecked(id: Int, checked: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            database.needsDao().updateChecked(id, checked)
        }
    }

    private fun scrollToBottom() {
        scrollViewNeeds.post {
            val objectAnimator = ObjectAnimator.ofInt(scrollViewNeeds, "scrollY",
                scrollViewNeeds.getChildAt(scrollViewNeeds.childCount - 1).bottom)
            objectAnimator.duration = scrollDuration
            objectAnimator.start()
        }
    }

    private fun scrollToTop() {
        scrollViewNeeds.post {
            val objectAnimator = ObjectAnimator.ofInt(scrollViewNeeds, "scrollY",
                scrollViewNeeds.getChildAt(scrollViewNeeds.childCount - 1).top)
            objectAnimator.duration = scrollDuration
            objectAnimator.start()
        }
    }

    private fun showSendButton() {
        val animation = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        animation.duration = 120
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                iconSend.visibility = View.VISIBLE
            }
            override fun onAnimationEnd(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        iconSend.startAnimation(animation)
    }

    private fun hideSendButton() {
        val animation = ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        animation.duration = 120
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                iconSend.visibility = View.GONE
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        iconSend.startAnimation(animation)
    }

    override fun onBackPressed() {
        startActivity(Intent(this@NeedsActivity, DashboardActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        finish()
    }

    override fun onLayoutDrawn() {
        val currentYContainer = currentYHeader + headerHeight
        val containerHeight = currentYFooter - currentYContainer
        val layoutParams = containerNeedsContent.layoutParams
        layoutParams.width = resources.displayMetrics.widthPixels
        layoutParams.height = containerHeight.toInt()
        containerNeedsContent.layoutParams = layoutParams
    }

}

interface NeedsListener {
    fun onLayoutDrawn()
}