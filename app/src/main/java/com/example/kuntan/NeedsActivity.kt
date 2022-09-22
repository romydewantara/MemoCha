package com.example.kuntan

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.TextViewCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.kuntan.entity.Needs
import com.example.kuntan.lib.KuntanPopupDialog
import com.example.kuntan.lib.NeedsItem
import com.example.kuntan.utility.AppUtil
import com.example.kuntan.utility.KuntanRoomDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_needs.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
    private var tempDate = ""
    private val arrayListOfNeedsLayout = arrayListOf<LinearLayout>()

    private lateinit var needsListener: NeedsListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_needs)

        init()
        initListener()
    }

    private fun init() {
        needsListener = this
        val needsHeader = findViewById<ConstraintLayout>(R.id.layoutNeedsHeader)
        val needsFooter = findViewById<ConstraintLayout>(R.id.layoutNeedsFooter)
        needsHeader.post { updateLayoutSize(HEADER, needsHeader.height, needsHeader.y) }
        needsFooter.post { updateLayoutSize(FOOTER, needsFooter.height, needsFooter.y) }

        rootLayoutNeeds.viewTreeObserver.addOnGlobalLayoutListener {
            if (!AppUtil.isKeyboardVisible(rootLayoutNeeds)) editTextNeedsItem.isFocusable = false
        }
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textViewNeedsTitle,
            1, 24, 1, TypedValue.COMPLEX_UNIT_SP)

        hideSendButton()
        populateNeeds()
    }

    @SuppressLint("ClickableViewAccessibility", "SimpleDateFormat")
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
                val currentDate = SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time)
                val currentTime = SimpleDateFormat("HH:mm").format(Date())
                var isDateShown = false
                /*if (tempDate != currentDate) {
                    tempDate = currentDate
                    isDateShown = true
                }*/
                val needsObj = Needs(0, editTextNeedsItem.text.toString().trim(), currentDate, currentTime, isDateShown, false, null)
                generateNeedsItem(needsObj)
                CoroutineScope(Dispatchers.IO).launch {
                    Log.d("Needs", "initListener - needs: ${Gson().toJson(needsObj)}")
                    database.needsDao().insert(needsObj)
                }
            }
            editTextNeedsItem.setText("")
        }
        iconAddImage.setOnClickListener {
            //editTextNeedsItem.setText(Constant.TEST_TEXT)
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
        if (headerHeight > 0 && footerHeight > 0) needsListener.onLayoutDrawn()
    }

    private fun populateNeeds() {
        CoroutineScope(Dispatchers.IO).launch {
            val needs = database.needsDao().getNeeds()
            Log.d("Needs", "populateNeeds (setUpdate()) - needs: ${Gson().toJson(needs)}")
            if (needs.isNotEmpty()) {
                val amount = if (needs.isNotEmpty()) needs.size else 0
                runOnUiThread {
                    for (i in needs.indices) {
                        generateNeedsItem(needs[i])
                    }
                    textViewNeedsAmount.text = String.format(getString(R.string.needs_today_list), amount)
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun generateNeedsItem(needs: Needs) {
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
                                CoroutineScope(Dispatchers.IO).launch {
                                    database.needsDao().deleteNeeds(id)
                                    runOnUiThread {
                                        /*containerNeedsContent.removeAllViews()
                                        arrayListOfNeedsLayout.clear()
                                        populateNeeds()*/
                                    }
                                }
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
                    val marginTop = AppUtil.dpToPx(this@NeedsActivity, 10f)

                    needsItem.y = (previousYLayout + marginTop).toFloat()
                    previousYLayout += (marginTop + needsItem.height)
                    needsContainerHeight += needsItem.height + marginTop

                    val containerParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                    containerParams.height = needsContainerHeight + AppUtil.dpToPx(this@NeedsActivity, 10f)
                    containerNeedsContent.layoutParams = containerParams
                    scrollViewNeeds.scrollTo(0, needsContainerHeight + AppUtil.dpToPx(this@NeedsActivity, 10f))
                    containerNeedsContent.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        containerNeedsContent.addView(needsItem)

        /*========================  LAYOUT TEXT-NEEDS  ========================*//*
        val textItem = AppCompatTextView(this)
        val textItemParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        textItem.layoutParams = textItemParams
        TextViewCompat.setTextAppearance(textItem, R.style.TextRegularBlack16)
        textItem.text = needs.item
        textItem.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val textParams = textItem.layoutParams
                    textParams.height = textItem.measuredHeight
                    textItem.layoutParams = textParams
                    textItem.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })

        val textTime = AppCompatTextView(this)
        val textTimeParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        textTimeParams.setMargins(
            AppUtil.dpToPx(this, 0f), AppUtil.dpToPx(this, 5f),
            AppUtil.dpToPx(this, 0f), AppUtil.dpToPx(this, 0f)
        )
        textTime.layoutParams = textTimeParams
        TextViewCompat.setTextAppearance(textTime, R.style.TextRegularDarkGrey14)
        textTime.text = needs.time

        val layoutTextItem = LinearLayout(this)
        val latestLayoutItemParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutTextItem.setPadding(
            AppUtil.dpToPx(this, 10f), AppUtil.dpToPx(this, 8f),
            AppUtil.dpToPx(this, 10f), AppUtil.dpToPx(this, 8f)
        )
        layoutTextItem.layoutParams = latestLayoutItemParams
        layoutTextItem.orientation = LinearLayout.VERTICAL
        layoutTextItem.gravity = Gravity.END
        layoutTextItem.background = getDrawable(R.drawable.background_item_needs_rounded)
        layoutTextItem.addView(textItem)
        layoutTextItem.addView(textTime)

        *//*========================  LAYOUT IMAGE CHECKED  ========================*//*
        val imageSize = AppUtil.getWidthPercent(this, 8f).toInt()
        val lottieImageChecked = LottieAnimationView(this)
        val imageCheckedParams = LinearLayout.LayoutParams(imageSize, imageSize)
        lottieImageChecked.layoutParams = imageCheckedParams
        lottieImageChecked.background = resources.getDrawable(R.drawable.ic_unchecked, null)
        lottieImageChecked.scaleType = ImageView.ScaleType.FIT_CENTER
        lottieImageChecked.setAnimation("lottie_checked.json")
        var checked = needs.checked
        if (checked) lottieImageChecked.playAnimation()
        lottieImageChecked.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                checked = !checked
                Log.d(TAG, "generateNeedsItem checked: $checked")
                database.needsDao().updateChecked(needs.id, checked)
                runOnUiThread {
                    if (checked) {
                        lottieImageChecked.speed = 2f
                        lottieImageChecked.playAnimation()
                    } else {
                        lottieImageChecked.speed = -25f
                        lottieImageChecked.playAnimation()
                    }
                }
            }
        }

        val containerTextAndCheckedItem = LinearLayout(this)
        val containerTextAndCheckedItemParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        containerTextAndCheckedItem.layoutParams = containerTextAndCheckedItemParams
        containerTextAndCheckedItem.orientation = LinearLayout.HORIZONTAL
        containerTextAndCheckedItem.gravity = Gravity.END
        containerTextAndCheckedItem.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                var itemWidth = layoutTextItem.width
                val itemMaxWidth = containerTextAndCheckedItem.width - (lottieImageChecked.width + AppUtil.dpToPx(this@NeedsActivity, 35f))
                if (layoutTextItem.width >= itemMaxWidth) {
                    itemWidth = (containerTextAndCheckedItem.width - lottieImageChecked.width)
                    val layoutItemParams = layoutTextItem.layoutParams
                    layoutItemParams.width = itemMaxWidth
                    layoutTextItem.layoutParams = layoutItemParams
                }

                val xLayoutItem = (containerTextAndCheckedItem.width - itemWidth - AppUtil.dpToPx(this@NeedsActivity, 10f)).toFloat()
                layoutTextItem.x = xLayoutItem
                lottieImageChecked.x = xLayoutItem - lottieImageChecked.width - AppUtil.dpToPx(this@NeedsActivity, 8f)
                lottieImageChecked.y = (layoutTextItem.height / 2f) - (lottieImageChecked.height / 2f)

                val needsItemParams = containerTextAndCheckedItem.layoutParams
                needsItemParams.height = layoutTextItem.height
                containerTextAndCheckedItem.layoutParams = needsItemParams
                containerTextAndCheckedItem.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        *//*========================  LAYOUT MASTER-NEEDS  ========================*//*
        val needsLayout = LinearLayout(this)
        val needsLayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        needsLayout.layoutParams = needsLayoutParams
        needsLayout.orientation = LinearLayout.VERTICAL
        needsLayout.gravity = Gravity.CENTER

        val textNeedsDate = AppCompatTextView(this)
        val textNeedsDateParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        textNeedsDateParams.setMargins(
            AppUtil.dpToPx(this, 10f), AppUtil.dpToPx(this, 16f),
            AppUtil.dpToPx(this, 10f), AppUtil.dpToPx(this, 13f)
        )
        textNeedsDate.setPadding(
            AppUtil.dpToPx(this, 10f), AppUtil.dpToPx(this, 8f),
            AppUtil.dpToPx(this, 10f), AppUtil.dpToPx(this, 8f)
        )
        textNeedsDate.layoutParams = textNeedsDateParams
        textNeedsDate.background = resources.getDrawable(R.drawable.background_text_view_date_gray, null)
        TextViewCompat.setTextAppearance(textNeedsDate, R.style.TextBoldWhite12)
        textNeedsDate.text = needs.date
        if (needs.isDateShown) textNeedsDate.visibility = View.VISIBLE else textNeedsDate.visibility = View.GONE
        textNeedsDate.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val textParams = textNeedsDate.layoutParams
                    textParams.height = textNeedsDate.measuredHeight
                    textNeedsDate.layoutParams = textParams
                    textNeedsDate.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })

        *//*========================  ADDED TO CONTAINER TIME !!!  ========================*//*
        containerTextAndCheckedItem.addView(lottieImageChecked)
        containerTextAndCheckedItem.addView(layoutTextItem)
        needsLayout.addView(textNeedsDate)
        needsLayout.addView(containerTextAndCheckedItem)
        layoutTextItem.setOnLongClickListener {
            val kuntanPopupDialog = KuntanPopupDialog.newInstance().setContent("Remove \"${needs.item}\"?",
                "Be careful, it will be deleted permanently!",
                getString(R.string.button_delete), getString(R.string.button_cancel), object : KuntanPopupDialog.KuntanPopupDialogListener {
                    override fun onNegativeButton() {
                        CoroutineScope(Dispatchers.IO).launch {
                            database.needsDao().deleteNeeds(needs.id)
                            runOnUiThread {
                                containerNeedsContent.removeAllViews()
                                arrayListOfNeedsLayout.clear()
                                populateNeeds()
                            }
                        }
                    }
                    override fun onPositiveButton() {

                    }

                })
            kuntanPopupDialog.show(supportFragmentManager, kuntanPopupDialog.tag)
            false
        }
        arrayListOfNeedsLayout.add(needsLayout)
        containerNeedsContent.addView(needsLayout)

        containerNeedsContent.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    var previousYLayout = 0
                    var needsContainerHeight = 0
                    if (arrayListOfNeedsLayout.isNotEmpty()) {
                        for (i in 0 until arrayListOfNeedsLayout.size) {
                            val masterNeedsItem = arrayListOfNeedsLayout[i]
                            var marginTop = AppUtil.dpToPx(this@NeedsActivity, 10f)
                            if (i > 0) marginTop = AppUtil.dpToPx(this@NeedsActivity, 3f)

                            masterNeedsItem.y = (previousYLayout + marginTop).toFloat()
                            previousYLayout += (marginTop + masterNeedsItem.height)
                            needsContainerHeight += masterNeedsItem.height + marginTop
                        }
                        val containerParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                        containerParams.height = needsContainerHeight + AppUtil.dpToPx(this@NeedsActivity, 10f)
                        containerNeedsContent.layoutParams = containerParams
                        scrollViewNeeds.scrollTo(0, needsContainerHeight + AppUtil.dpToPx(this@NeedsActivity, 10f))
                    }
                    containerNeedsContent.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })*/
    }

    private fun updateChecked(id: Int, checked: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            database.needsDao().updateChecked(id, checked)
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