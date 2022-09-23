package com.example.kuntan.lib

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.GridLayout
import androidx.core.widget.TextViewCompat
import com.example.kuntan.R
import com.example.kuntan.utility.AppUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_needs_month_chooser.view.*
import kotlinx.android.synthetic.main.layout_needs_month_chooser_bottom_sheet.*
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
class NeedsMonthChooserBottomSheet: BottomSheetDialogFragment() {

    companion object {
        const val TAG = "NeedsMonth"
        const val rows = 6
        const val columns = 2
        val arrayListOfMonth = arrayListOf(
            R.string.month_january, R.string.month_february, R.string.month_march, R.string.month_april,
            R.string.month_may, R.string.month_june, R.string.month_july, R.string.month_august,
            R.string.month_september, R.string.month_october, R.string.month_november, R.string.month_december)
    }

    private var year = SimpleDateFormat("yyyy").format(Calendar.getInstance().time)
    private val arrayListIconUsed = arrayListOf<Int>()

    lateinit var needsDateChooserListener: NeedsDateChooserListener
    lateinit var gridLayout: GridLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_needs_month_chooser_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            textViewHeader, 1, 16,
            1, TypedValue.COMPLEX_UNIT_SP)
        generateLayout()
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
            }
        }
        bottomSheetDialog.setOnKeyListener { _: DialogInterface?, _: Int, keyEvent: KeyEvent ->
            if (keyEvent.keyCode == KeyEvent.KEYCODE_BACK) dismiss()
            false
        }
        return bottomSheetDialog
    }

    private fun generateLayout() {
        gridLayout = GridLayout(context)
        val gridParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        gridLayout.layoutParams = gridParams
        gridLayout.alignmentMode = GridLayout.ALIGN_BOUNDS
        gridLayout.rowCount = rows
        gridLayout.columnCount = columns

        var column = 0
        var row = 0
        for (i in 0 until arrayListOfMonth.size) {
            if (column == columns) {
                column = 0
                row++
            }

            val needsMonthChooser = LayoutInflater.from(requireContext()).inflate(
                R.layout.layout_needs_month_chooser, gridLayout, false)
            needsMonthChooser.measure(needsMonthChooser.width, needsMonthChooser.height)

            val needsMonthChooserParams = GridLayout.LayoutParams()
            needsMonthChooserParams.width = (resources.displayMetrics.widthPixels / 2) - AppUtil.dpToPx(requireContext(), 40f)
            needsMonthChooserParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            needsMonthChooserParams.setGravity(Gravity.CENTER)
            needsMonthChooserParams.columnSpec = GridLayout.spec(column)
            needsMonthChooserParams.rowSpec = GridLayout.spec(row)
            needsMonthChooserParams.setMargins(
                AppUtil.dpToPx(requireContext(), 20f), AppUtil.dpToPx(requireContext(), 10f),
                AppUtil.dpToPx(requireContext(), 20f), AppUtil.dpToPx(requireContext(), 10f)
            )
            needsMonthChooser.layoutParams = needsMonthChooserParams
            needsMonthChooser.imageIcon.setImageResource(AppUtil.randomIcon(requireContext(), arrayListIconUsed))
            needsMonthChooser.textViewNeedsMonthCode.text = AppUtil.convertMonthCodeFromId(i)
            needsMonthChooser.textViewNeedsMonth.text = requireContext().getString(arrayListOfMonth[i])

            needsMonthChooser.setOnClickListener {
                needsDateChooserListener.onDateSelected(year,
                    AppUtil.convertMonthCodeFromName(requireContext(), arrayListOfMonth[i].toString()))
                dismiss()
            }

            gridLayout.addView(needsMonthChooser)
            column++
        }
        containerMonthNeeds.addView(gridLayout)
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
    }
}