package com.example.kuntan.lib

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.kuntan.R
import com.example.kuntan.utility.AppUtil

public class SecretLetterDialog(context: Context): RelativeLayout(context) {

    init {
        init()
    }

    private lateinit var container: RelativeLayout
    private var containerWidth: Int = 0
    private var containerHeight: Int = 0

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun init() {
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels

        val rootLayoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams = rootLayoutParams
        background = context.resources.getDrawable(R.drawable.background_secret_letter, null)

        val lottieAnimationView = LottieAnimationView(context)
        val lottieLayoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        lottieAnimationView.layoutParams = lottieLayoutParams
        lottieAnimationView.setAnimation("confetti.json")
        lottieAnimationView.repeatCount = 1
        lottieAnimationView.scaleType = ImageView.ScaleType.CENTER_CROP
        lottieAnimationView.playAnimation()

        container = RelativeLayout(context)
        container.setPadding(
            AppUtil.dpToPx(context, 10f),
            AppUtil.dpToPx(context, 10f),
            AppUtil.dpToPx(context, 10f),
            AppUtil.dpToPx(context, 10f))
        val containerParams = LayoutParams(AppUtil.dpToPx(context, 350f), height)
        containerParams.setMargins(
            AppUtil.dpToPx(context, 25f),
            AppUtil.dpToPx(context, 85f),
            AppUtil.dpToPx(context, 25f),
            AppUtil.dpToPx(context, 85f)
        )
        container.layoutParams = containerParams
        //border(container, R.color.black)

        val titleStr = context.getString(R.string.secret_letter_title)
        val title = AppCompatTextView(context)
        val titleParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        title.layoutParams = titleParams
        title.text = titleStr
        title.gravity = Gravity.CENTER
        TextViewCompat.setTextAppearance(title, R.style.LobsterTextRegularTeal20)
        //border(title, R.color.blue)

        val contentStr = context.getString(R.string.secret_letter_content)
        val content = AppCompatTextView(context)
        val contentParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        content.layoutParams = contentParams
        content.text = contentStr
        TextViewCompat.setTextAppearance(content, R.style.TextRegularDarkGrey14)
        //border(content, R.color.red_dark)

        val calendarStr = context.getString(R.string.secret_letter_calendar)
        val calendar = AppCompatTextView(context)
        val calendarParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        calendar.layoutParams = calendarParams
        calendar.text = calendarStr
        calendar.gravity = Gravity.END
        TextViewCompat.setTextAppearance(calendar, R.style.TextRegularDarkGrey14)
        //border(calendar, R.color.red_orange)

        val imagePhoto = ImageView(context)
        val imagePhotoParams = ViewGroup.LayoutParams(AppUtil.dpToPx(context, 160f), AppUtil.dpToPx(context, 160f))
        imagePhoto.layoutParams = imagePhotoParams
        //border(imagePhoto, R.color.red_orange)
        imagePhoto.background = context.resources.getDrawable(R.drawable.background_secret_letter, null)
        imagePhoto.setImageResource(R.drawable.rosya_romy_wedding)

        adjustView(title, content, calendar, imagePhoto)
        container.addView(title)
        container.addView(content)
        container.addView(calendar)
        container.addView(imagePhoto)
        addView(lottieAnimationView)
        addView(container)
    }

    private fun adjustView(title: AppCompatTextView, content: AppCompatTextView, calendar: AppCompatTextView, imagePhoto: ImageView) {
        title.measure(title.layoutParams.width, title.layoutParams.height)
        content.measure(content.layoutParams.width, content.layoutParams.height)
        calendar.measure(calendar.layoutParams.width, calendar.layoutParams.height)
        imagePhoto.measure(imagePhoto.layoutParams.width, imagePhoto.layoutParams.height)

        content.y = title.measuredHeight.toFloat() * 3
        calendar.y = content.y + content.measuredHeight + (calendar.measuredHeight * 5)
        imagePhoto.x = container.layoutParams.width.toFloat() - imagePhoto.layoutParams.width
        imagePhoto.y = content.y + content.measuredHeight + (calendar.measuredHeight * 6)
    }

    @SuppressLint("NewApi", "ResourceType")
    fun border(view: View, color: Int) {
        val border = GradientDrawable()
        border.setStroke(5, context.resources.getColor(color, null))
        view.background = border
    }

}