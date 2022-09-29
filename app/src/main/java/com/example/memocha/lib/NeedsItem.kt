package com.example.memocha.lib

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.memocha.R
import com.example.memocha.entity.Needs
import com.example.memocha.utility.AppUtil

@SuppressLint("UseCompatLoadingForDrawables")
class NeedsItem(context: Context) : LinearLayout(context) {

    companion object {
        const val TAG = "NeedsItem"
    }

    private lateinit var needs: Needs
    private lateinit var textItem: AppCompatTextView
    private lateinit var textTime: AppCompatTextView
    private lateinit var textDate: AppCompatTextView
    private lateinit var textGroupLayout: LinearLayout
    private lateinit var textGroupAndImageLayout: LinearLayout
    private lateinit var lottieChecked: LottieAnimationView
    private lateinit var needsItemListener: NeedsItemListener

    fun getInstance(needs: Needs, needsItemListener: NeedsItemListener): NeedsItem {
        this.needs = needs
        this.needsItemListener = needsItemListener

        generateTextDate()
        generateTextGroupAndImageLayout()

        addView(textDate)
        addView(textGroupAndImageLayout)

        val needsLayoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams = needsLayoutParams
        orientation = VERTICAL
        gravity = Gravity.CENTER

        return this
    }

    private fun generateTextGroupLayout() {
        textItem = AppCompatTextView(context)
        val textItemParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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

        textTime = AppCompatTextView(context)
        val textTimeParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        textTimeParams.setMargins(
            AppUtil.dpToPx(context, 0f), AppUtil.dpToPx(context, 5f),
            AppUtil.dpToPx(context, 0f), AppUtil.dpToPx(context, 0f)
        )
        textTime.layoutParams = textTimeParams
        TextViewCompat.setTextAppearance(textTime, R.style.TextRegularDarkGrey14)
        textTime.text = needs.time

        textGroupLayout = LinearLayout(context)
        val latestLayoutItemParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        textGroupLayout.setPadding(
            AppUtil.dpToPx(context, 10f), AppUtil.dpToPx(context, 8f),
            AppUtil.dpToPx(context, 10f), AppUtil.dpToPx(context, 8f)
        )
        textGroupLayout.layoutParams = latestLayoutItemParams
        textGroupLayout.orientation = VERTICAL
        textGroupLayout.gravity = Gravity.START
        textGroupLayout.background = context.getDrawable(R.drawable.background_group_needs_item_white_rounded)
        textGroupLayout.addView(textItem)
        textGroupLayout.addView(textTime)
        textGroupLayout.setOnLongClickListener {
            needsItemListener.onLongItemClicked(needs.id)
            false
        }
    }

    private fun generateTextGroupAndImageLayout() {
        generateTextGroupLayout()

        lottieChecked = LottieAnimationView(context)
        val imageSize = AppUtil.getWidthPercent(context, 8f).toInt()
        val imageCheckedParams = LayoutParams(imageSize, imageSize)
        lottieChecked.layoutParams = imageCheckedParams
        lottieChecked.background = resources.getDrawable(R.drawable.ic_unchecked, null)
        lottieChecked.scaleType = ImageView.ScaleType.FIT_CENTER
        lottieChecked.setAnimation("lottie_checked.json")
        var checked = needs.checked
        if (checked) lottieChecked.playAnimation()
        lottieChecked.setOnClickListener {
            checked = !checked
            if (checked) {
                lottieChecked.speed = 2f
                lottieChecked.playAnimation()
            } else {
                lottieChecked.speed = -25f
                lottieChecked.playAnimation()
            }
            needsItemListener.onChecked(needs.id, checked)
            Log.d(TAG, "LottieChecked - checked: ${checked}")
        }

        textGroupAndImageLayout = LinearLayout(context)
        val containerTextAndCheckedItemParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        textGroupAndImageLayout.layoutParams = containerTextAndCheckedItemParams
        textGroupAndImageLayout.orientation = HORIZONTAL
        textGroupAndImageLayout.gravity = Gravity.START
        textGroupAndImageLayout.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    var itemWidth = textGroupLayout.width
                    val itemMaxWidth = textGroupAndImageLayout.width -
                            (lottieChecked.width + AppUtil.dpToPx(context, 35f))
                    if (textGroupLayout.width >= itemMaxWidth) {
                        itemWidth = (textGroupAndImageLayout.width - lottieChecked.width)
                        val layoutItemParams = textGroupLayout.layoutParams
                        layoutItemParams.width = itemMaxWidth
                        textGroupLayout.layoutParams = layoutItemParams
                    }

                    val xLayoutItem = AppUtil.dpToPx(context, 10f).toFloat()
                    lottieChecked.x = xLayoutItem
                    lottieChecked.y = (textGroupLayout.height / 2f) - (lottieChecked.height / 2f)
                    textGroupLayout.x = xLayoutItem + lottieChecked.width + AppUtil.dpToPx(context, 10f).toFloat()

                    val needsItemParams = textGroupAndImageLayout.layoutParams
                    needsItemParams.height = textGroupLayout.height
                    textGroupAndImageLayout.layoutParams = needsItemParams
                    textGroupAndImageLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        textGroupAndImageLayout.addView(lottieChecked)
        textGroupAndImageLayout.addView(textGroupLayout)
    }

    private fun generateTextDate() {
        textDate = AppCompatTextView(context)
        val textNeedsDateParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        textNeedsDateParams.setMargins(
            AppUtil.dpToPx(context, 10f), AppUtil.dpToPx(context, 16f),
            AppUtil.dpToPx(context, 10f), AppUtil.dpToPx(context, 13f)
        )
        textDate.setPadding(
            AppUtil.dpToPx(context, 10f), AppUtil.dpToPx(context, 8f),
            AppUtil.dpToPx(context, 10f), AppUtil.dpToPx(context, 8f)
        )
        textDate.layoutParams = textNeedsDateParams
        textDate.background =
            resources.getDrawable(R.drawable.background_needs_text_date_teal_dark, null)
        TextViewCompat.setTextAppearance(textDate, R.style.TextBoldWhite12)
        val date = "${needs.date}/${needs.month}/${needs.year}"
        textDate.text = date
        if (needs.isDateShown) textDate.visibility =
            View.VISIBLE else textDate.visibility = View.GONE
        textDate.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val textParams = textDate.layoutParams
                    textParams.height = textDate.measuredHeight
                    textDate.layoutParams = textParams
                    textDate.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
    }

    fun changeItem(text: String) {
        textItem.text = text
        //show label Edited
    }

    interface NeedsItemListener {
        fun onChecked(id: Int, checked: Boolean)
        fun onLongItemClicked(id: Int)
    }
}