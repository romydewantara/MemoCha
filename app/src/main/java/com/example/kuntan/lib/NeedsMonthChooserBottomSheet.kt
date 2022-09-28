package com.example.kuntan.lib

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.RelativeLayout
import androidx.core.widget.TextViewCompat
import com.example.kuntan.R
import com.example.kuntan.utility.AppUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_needs_month_chooser.view.*
import kotlinx.android.synthetic.main.layout_needs_month_chooser_bottom_sheet.*
import java.text.SimpleDateFormat
import java.util.Calendar

@SuppressLint("SimpleDateFormat")
class NeedsMonthChooserBottomSheet: BottomSheetDialogFragment() {

    companion object {
        const val TAG = "NeedsMonth"
        const val GRID_COLUMNS = 3
    }

    private var year = SimpleDateFormat("yyyy").format(Calendar.getInstance().time)
    private val arrayListIconUsed = arrayListOf<Int>()

    lateinit var needsDateChooserListener: NeedsDateChooserListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_needs_month_chooser_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            textViewHeader, 1, 16,
            1, TypedValue.COMPLEX_UNIT_SP)
        initListener()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState)
        bottomSheetDialog.setOnShowListener {
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
                BottomSheetBehavior.from(bottomSheet).skipCollapsed = false
                BottomSheetBehavior.from(bottomSheet).isDraggable = false
                BottomSheetBehavior.from(bottomSheet).addBottomSheetCallback(
                    object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            lottieHeader.playAnimation()
                            generateLayout()
                        }
                    }
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                })
            }
        }
        bottomSheetDialog.setOnKeyListener { _: DialogInterface?, _: Int, keyEvent: KeyEvent ->
            if (keyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                needsDateChooserListener.onBackPressed()
                lottieHeader.cancelAnimation()
                dismiss()
            }
            false
        }
        return bottomSheetDialog
    }

    private fun generateLayout() {
        var heightNeedsMonthChooser = 0
        val marginStartEnd = (resources.displayMetrics.widthPixels % 3).toFloat()
        val marginTopBottom = (resources.displayMetrics.heightPixels % 4).toFloat()
        var column = 0
        var row = 0

        for (i in 0 until AppUtil.getListOfMonth(requireContext()).size) {
            if (column == GRID_COLUMNS) {
                column = 0
                row++
            }

            val needsMonthChooserParams = GridLayout.LayoutParams()
            needsMonthChooserParams.width = ((resources.displayMetrics.widthPixels / 3) - AppUtil.getWidthPercent(requireContext(), 3.5f)).toInt()
            needsMonthChooserParams.height = ((resources.displayMetrics.heightPixels / 4)  - AppUtil.getHeightPercent(requireContext(), 2.0f)).toInt()
            needsMonthChooserParams.setGravity(Gravity.CENTER)
            needsMonthChooserParams.columnSpec = GridLayout.spec(column)
            needsMonthChooserParams.rowSpec = GridLayout.spec(row)
            needsMonthChooserParams.setMargins(
                AppUtil.getWidthPercent(requireContext(), 1.8f).toInt(),
                AppUtil.getHeightPercent(requireContext(), 1f).toInt(),
                AppUtil.getWidthPercent(requireContext(), 1.8f).toInt(),
                AppUtil.getHeightPercent(requireContext(), 1f).toInt())

            val needsMonthChooser = LayoutInflater.from(requireContext()).inflate(R.layout.layout_needs_month_chooser, needsGridLayout, false)
            needsMonthChooser.layoutParams = needsMonthChooserParams
            needsMonthChooser.imageIcon.setImageResource(AppUtil.randomIcon(requireContext(), arrayListIconUsed))
            needsMonthChooser.textViewNeedsMonthCode.text = AppUtil.convertMonthCodeFromId(i)
            needsMonthChooser.textViewNeedsMonth.text = AppUtil.getListOfMonth(requireContext())[i]
            needsMonthChooser.setOnClickListener {
                needsDateChooserListener.onDateSelected(year,
                    AppUtil.convertMonthCodeFromName(requireContext(), AppUtil.getListOfMonth(requireContext())[i]))
                lottieHeader.cancelAnimation()
                dismiss()
            }
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                needsMonthChooser.textViewNeedsMonth, 1, 16, 1, TypedValue.COMPLEX_UNIT_SP)

            needsGridLayout.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        if (i == 0 || i == 3 || i == 6 || i == 9) {
                            heightNeedsMonthChooser += (needsMonthChooser.layoutParams.height + marginTopBottom).toInt()
                            val containerParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                            containerParams.height = resources.displayMetrics.heightPixels
                            needsGridLayout.layoutParams = containerParams
                        }
                        needsGridLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }})
            needsGridLayout.addView(needsMonthChooser)
            column++
        }
    }

    fun initListener() {
        previousYear.setOnClickListener {
            year = (year.toInt() - 1).toString()
            textYear.text = year
        }
        nextYear.setOnClickListener {
            year = (year.toInt() + 1).toString()
            textYear.text = year
        }
    }

    fun addOnNeedsDateChooserListener(needsDateChooserListener: NeedsDateChooserListener) {
        this.needsDateChooserListener = needsDateChooserListener
    }

    interface NeedsDateChooserListener {
        fun onDateSelected(year: String, month: String)
        fun onBackPressed()
    }
}