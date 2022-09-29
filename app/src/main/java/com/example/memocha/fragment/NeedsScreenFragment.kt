package com.example.memocha.fragment

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import com.example.memocha.DashboardActivity
import com.example.memocha.R
import com.example.memocha.entity.Needs
import com.example.memocha.lib.MemoChaPopupDialog
import com.example.memocha.lib.NeedsItem
import com.example.memocha.lib.NeedsMonthChooserBottomSheet
import com.example.memocha.utility.AppUtil
import com.example.memocha.utility.MemoChaRoomDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_fragment_needs.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@SuppressLint("ClickableViewAccessibility", "SimpleDateFormat")
class NeedsScreenFragment(context: Context) : Fragment(R.layout.layout_fragment_needs) {

    companion object {
        const val TAG = "Needs"
    }

    private val database by lazy { MemoChaRoomDatabase(requireContext()) }
    private var previousYLayout = 0
    private var needsContainerHeight = 0
    private var previousTextLength = 0
    private var scrollDuration = 0L
    private var tempDate = ""
    private var currentMonth = ""
    private var currentYear = ""
    private var dataSize = 0
    private var dataSizeCount = 0
    private var isInit = true

    private lateinit var invisibleBackground: View
    private lateinit var needsScreenListener: NeedsScreenListener
    private lateinit var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener

    private lateinit var previousPage: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        needsScreenListener.onScreenCreated()

        init()
        initListener()
    }

    private fun init() {
        tempDate = SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time)
        currentMonth = tempDate.split("/")[1]
        currentYear = tempDate.split("/")[2]

        invisibleBackground = View(requireContext())
        containerNeedsContent.addView(invisibleBackground)

        rootLayoutNeeds.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                globalLayoutListener = this
                if (!AppUtil.isKeyboardVisible(rootLayoutNeeds)) editTextNeedsItem.isFocusable = false
            }
        })
        //setScrollViewNeedsObserver()

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textViewNeedsTitle,
            1, 24, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            textViewNeedsInfo,
            1, 14, 1, TypedValue.COMPLEX_UNIT_SP)

        textViewNeedsDate.text = AppUtil.convertMonthNameFromCode(
            requireContext(), SimpleDateFormat("MM").format(Calendar.getInstance().time))
        hideSendButton()
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
                val needsObj = Needs(0, editTextNeedsItem.text.toString().trim(), currentTime,
                    currentDate.split("/")[0], currentDate.split("/")[1],
                    currentDate.split("/")[2], isDateShown, false, null
                )
                CoroutineScope(Dispatchers.IO).launch {
                    Log.d("Needs", "initListener - needs: ${Gson().toJson(needsObj)}")
                    database.needsDao().insert(needsObj)
                    val needs = database.needsDao().getNeeds(currentMonth, currentYear)
                    (requireContext() as Activity).runOnUiThread {
                        for (i in needs.indices) {
                            if (i == needs.size - 1) {
                                generateNeedsItem(needs[i])
                            }
                        }
                        textViewNeedsAmount.text = String.format(getString(R.string.needs_today_list), needs.size)
                    }
                }
            }
            editTextNeedsItem.setText("")
        }
        iconAddImage.setOnClickListener {
            //editTextNeedsItem.setText(Constant.TEST_TEXT)
        }
        imageMenu.setOnClickListener {
            needsOverlayLayout.visibility = View.VISIBLE
            val needsMonthChooserBottomSheet = NeedsMonthChooserBottomSheet()
            needsMonthChooserBottomSheet.addOnNeedsDateChooserListener(
                object : NeedsMonthChooserBottomSheet.NeedsDateChooserListener {
                    override fun onDateSelected(year: String, month: String) {
                        Log.d(TAG, "onDateSelected - month: $month | year: $year")
                        textViewNeedsDate.text = AppUtil.convertMonthNameFromCode(requireContext(), month)
                        onBackPressed()
                        previousPage = DashboardActivity.PAGE_MONTH_CHOOSER
                        currentMonth = month
                        currentYear = year
                        populateNeeds()
                    }
                    override fun onBackPressed() {
                        needsOverlayLayout.visibility = View.GONE
                    }
                })
            needsMonthChooserBottomSheet.isCancelable = false
            needsMonthChooserBottomSheet.show(childFragmentManager, needsMonthChooserBottomSheet.tag)
        }
        buttonScrollToTop.setOnClickListener {
            scrollDuration = 600L
            scrollToTop()
        }
        buttonScrollToBottom.setOnClickListener {
            scrollDuration = 600L
            scrollToBottom()
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
                                Toast.makeText(requireContext(), String.format(getString(R.string.needs_text_length_limit), s.length), Toast.LENGTH_LONG).show()
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

    private fun setScrollViewNeedsObserver() {
        Log.d(TAG, "setScrollViewNeedsObserver")
        scrollViewNeeds.setOnTouchListener { _, _ -> false }
        scrollViewNeeds.viewTreeObserver.addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
            override fun onScrollChanged() {
                val view = scrollViewNeeds.getChildAt(scrollViewNeeds.childCount - 1)
                Log.d(TAG, "setScrollViewNeedsObserver - view height: ${view.height} | params: ${view.layoutParams.height}")
                if (view.height > resources.displayMetrics.heightPixels) {
                    val topDetector = scrollViewNeeds.scrollY
                    val bottomDetector: Int = view.bottom - (scrollViewNeeds.height + scrollViewNeeds.scrollY)
                    buttonScrollToTop.visibility = View.GONE
                    buttonScrollToBottom.visibility = View.GONE
                    if (bottomDetector == 0) {
                        buttonScrollToTop.visibility = View.VISIBLE
                        Toast.makeText(requireContext(), "Scroll View bottom reached", Toast.LENGTH_SHORT).show()
                    }
                    if (topDetector <= 0) {
                        buttonScrollToBottom.visibility = View.VISIBLE
                        Toast.makeText(requireContext(), "Scroll View top reached", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
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

    private fun reset() {
        isInit = true
        dataSize = 0
        dataSizeCount = 0
        previousYLayout = 0
        needsContainerHeight = 0
        previousTextLength = 0
        scrollDuration = 0L

        //reset container height size
        containerNeedsContent.removeAllViews()
        val containerParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        containerParams.height = 0
        containerNeedsContent.layoutParams = containerParams
    }

    private fun generateNeedsItem(needs: Needs) {
        Log.d(TAG, "generateNeedsItem - needs: ${Gson().toJson(needs)}")
        val needsItem = NeedsItem(requireContext()).getInstance(needs,
            object : NeedsItem.NeedsItemListener {
                override fun onChecked(id: Int, checked: Boolean) {
                    updateChecked(id, checked)
                }

                override fun onLongItemClicked(id: Int) {
                    val memochaPopupDialog = MemoChaPopupDialog.newInstance().setContent("Remove \"${needs.item}\"?",
                        "Be careful, it will be deleted permanently!",
                        getString(R.string.button_delete), getString(R.string.button_cancel),
                        object : MemoChaPopupDialog.MemoChaPopupDialogListener {
                            override fun onNegativeButton() {
                                CoroutineScope(Dispatchers.IO).launch {
                                    database.needsDao().deleteNeeds(id)
                                    (requireContext() as Activity).runOnUiThread {
                                        populateNeeds()
                                    }
                                }
                            }
                            override fun onPositiveButton() {}
                        })
                    memochaPopupDialog.show(childFragmentManager, memochaPopupDialog.tag)
                }
            })

        containerNeedsContent.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    var marginTop = AppUtil.dpToPx(requireContext(), 10f)
                    if (!needs.isDateShown) marginTop = AppUtil.dpToPx(requireContext(), 3f)

                    needsItem.y = (previousYLayout + marginTop).toFloat()
                    previousYLayout += (marginTop + needsItem.height)
                    needsContainerHeight += needsItem.height + marginTop

                    val containerParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                    containerParams.height = needsContainerHeight + AppUtil.dpToPx(requireContext(), 10f)
                    containerNeedsContent.layoutParams = containerParams
                    Log.d(TAG, "onGlobalLayout - container height: ${containerNeedsContent.height} | params: ${containerNeedsContent.layoutParams.height} | measured: ${containerNeedsContent.measuredHeight}")

                    val scrollBounds = Rect()
                    scrollViewNeeds.getHitRect(scrollBounds)
                    val invisibleBackgroundParams = RelativeLayout.LayoutParams(0, (containerNeedsContent.layoutParams.height / 2))
                    invisibleBackground.layoutParams = invisibleBackgroundParams

                    Log.d(TAG, "onGlobalLayout - isInit: $isInit | dataSizeCount: $dataSizeCount | dataSize: $dataSize")
                    if (isInit) {
                        if (dataSizeCount < dataSize) {
                            dataSizeCount++
                        } else {
                            isInit = false
                            needsScreenListener.onDataPopulated()
                        }
                    }
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

    fun populateNeeds() {
        needsScreenListener.onPopulateData()
        reset()
        if (previousPage == DashboardActivity.PAGE_MONTH_CHOOSER) {
            if ("$currentMonth/$currentYear" == SimpleDateFormat("MM/yyyy").format(Calendar.getInstance().time)) {
                layoutNeedsFooter.visibility = View.VISIBLE
                textViewNeedsInfo.visibility = View.GONE
            } else {
                layoutNeedsFooter.visibility = View.GONE
                textViewNeedsInfo.visibility = View.VISIBLE
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val needs = database.needsDao().getNeeds(currentMonth, currentYear)
            Log.d(TAG, "populateNeeds - data: ${Gson().toJson(needs)}")
            (requireContext() as Activity).runOnUiThread {
                val amount = if (needs.isNotEmpty()) needs.size else 0
                if (needs.isNotEmpty()) {
                    dataSize = (needs.size - 1)
                    for (i in needs.indices) {
                        if (tempDate != needs[i].date) {
                            tempDate = needs[i].date
                            needs[i].isDateShown = true
                        }
                        generateNeedsItem(needs[i])
                    }
                } else needsScreenListener.onDataPopulated()
                textViewNeedsAmount.text = String.format(getString(R.string.needs_today_list), amount)
            }
        }
    }

    fun addOnNeedScreenListener(needsScreenListener: NeedsScreenListener) : Fragment {
        this.needsScreenListener = needsScreenListener
        return this@NeedsScreenFragment
    }

    fun addPreviousPage(previousPage: String) {
        this.previousPage = previousPage
    }

    fun removeListener() {
        rootLayoutNeeds.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        //scrollViewNeeds.viewTreeObserver.removeOnScrollChangedListener(scrollChangedListener) //always null -_-
    }

    interface NeedsScreenListener {
        fun onScreenCreated()
        fun onPopulateData()
        fun onDataPopulated()
    }

}