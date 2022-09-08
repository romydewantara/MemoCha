package com.example.kuntan.lib

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import com.example.kuntan.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_schedule_editor.*

class ScheduleEditorBottomSheet(
    private val time: String,
    private val sHour: String,
    private val sMinute: String,
    private val eHour: String,
    private val eMinute: String,
    ) : BottomSheetDialogFragment() {

    private var startHour = sHour.toInt()
    private var endHour = eHour.toInt()
    private var startMinute = sMinute.toInt()
    private var endMinute = eMinute.toInt()

    companion object {
        const val TYPE_INCREASE = "increased"
        const val TYPE_DECREASE = "decreased"
        const val TYPE_START_HOUR = "start_hour"
        const val TYPE_START_MINUTE = "start_minute"
        const val TYPE_END_HOUR = "end_hour"
        const val TYPE_END_MINUTE = "end_minute"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_schedule_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewTime.text = time
        editTextStartTimeHour.setText(sHour)
        editTextStartTimeMinute.setText(sMinute)
        editTextEndTimeHour.setText(eHour)
        editTextEndTimeMinute.setText(eMinute)

        textViewCancel.setOnClickListener {
            dismiss()
        }
        textViewSave.setOnClickListener {
            //call update schedule
        }
        imageStartHourAdd.setOnClickListener {
            updateNumber(TYPE_INCREASE, TYPE_START_HOUR, editTextStartTimeHour, 23, 0)
        }
        imageStartHourSub.setOnClickListener {
            updateNumber(TYPE_DECREASE, TYPE_START_HOUR, editTextStartTimeHour, 23, 0)
        }
        imageStartMinuteAdd.setOnClickListener {
            updateNumber(TYPE_INCREASE, TYPE_START_MINUTE, editTextStartTimeMinute, 59, 0)
        }
        imageStartMinuteSub.setOnClickListener {
            updateNumber(TYPE_DECREASE, TYPE_START_MINUTE, editTextStartTimeMinute, 59, 0)
        }
        imageEndHourAdd.setOnClickListener {
            updateNumber(TYPE_INCREASE, TYPE_END_HOUR, editTextEndTimeHour, 23, 0)
        }
        imageEndHourSub.setOnClickListener {
            updateNumber(TYPE_DECREASE, TYPE_END_HOUR, editTextEndTimeHour, 23, 0)
        }
        imageEndMinuteAdd.setOnClickListener {
            updateNumber(TYPE_INCREASE, TYPE_END_MINUTE, editTextEndTimeMinute, 59, 0)
        }
        imageEndMinuteSub.setOnClickListener {
            updateNumber(TYPE_DECREASE, TYPE_END_MINUTE, editTextEndTimeMinute, 59, 0)
        }
    }

    private fun updateNumber(type: String, numberType: String, editText: EditText, max: Int, min: Int) {
        var number = 0;
        when(type) {
            TYPE_INCREASE -> {
                when(numberType) {
                    TYPE_START_HOUR -> {
                        startHour++
                        if (startHour > max) startHour = max
                        number = startHour
                    }
                    TYPE_END_HOUR -> {
                        endHour++
                        if (endHour > max) endHour = max
                        number = endHour
                    }
                    TYPE_START_MINUTE -> {
                        startMinute++
                        if (startMinute > max) startMinute = max
                        number = startMinute
                    }
                    TYPE_END_MINUTE -> {
                        endMinute++
                        if (endMinute > max) endMinute = max
                        number = endMinute
                    }
                }
                if (number < 10) editText.setText(String.format("%02d", number))
                else editText.setText(number.toString())
            }
            TYPE_DECREASE -> {
                when(numberType) {
                    TYPE_START_HOUR -> {
                        startHour--
                        if (startHour < min) startHour = min
                        number = startHour
                    }
                    TYPE_END_HOUR -> {
                        endHour--
                        if (endHour < min) endHour = min
                        number = endHour
                    }
                    TYPE_START_MINUTE -> {
                        startMinute--
                        if (startMinute < min) startMinute = min
                        number = startMinute
                    }
                    TYPE_END_MINUTE -> {
                        endMinute--
                        if (endMinute < min) endMinute = min
                        number = endMinute
                    }
                }
                if (number < 10) editText.setText(String.format("%02d", number))
                else editText.setText(number.toString())
            }
        }
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
        return bottomSheetDialog
    }

}