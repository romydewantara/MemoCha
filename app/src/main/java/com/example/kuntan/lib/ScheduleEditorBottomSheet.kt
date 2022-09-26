package com.example.kuntan.lib

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.widget.TextViewCompat
import com.example.kuntan.R
import com.example.kuntan.utility.AppUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_schedule_editor.*

class ScheduleEditorBottomSheet(
    private val time: String,
    private val timeId: Int,
    private val sHour: String,
    private val sMinute: String,
    private val eHour: String,
    private val eMinute: String,
    private val actions: String,
    private val isEdit: Boolean,
    editorListener: ScheduleEditorListener
    ) : BottomSheetDialogFragment() {

    private var startHour = sHour.toInt()
    private var endHour = eHour.toInt()
    private var startMinute = sMinute.toInt()
    private var endMinute = eMinute.toInt()
    private var action = actions
    private var mListener = editorListener

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
        view.viewTreeObserver.addOnGlobalLayoutListener {
            if (!AppUtil.isKeyboardVisible(view)) editTextActions.isFocusable = false
        }
        textViewTime.text = time
        editTextStartTimeHour.setText(sHour)
        editTextStartTimeMinute.setText(sMinute)
        editTextEndTimeHour.setText(eHour)
        editTextEndTimeMinute.setText(eMinute)
        editTextActions.setText(action)

        if (isEdit) {
            textViewAdd.text = requireContext().getString(R.string.button_save)
            imageDelete.visibility = View.VISIBLE
        }

        initListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        textViewCancel.setOnClickListener {
            dismiss()
        }
        textViewAdd.setOnClickListener {
            val startTime = "${editTextStartTimeHour.text}:${editTextStartTimeMinute.text}"
            val endTime = "${editTextEndTimeHour.text}:${editTextEndTimeMinute.text}"
            val actions = if (editTextActions.text.toString() != "") editTextActions.text.toString() else "Empty"
            if (isEdit) mListener.onEditSchedule(timeId, startTime, endTime, actions)
            else mListener.onAddNewSchedule(timeId, startTime, endTime, actions)

            dismiss()
        }
        imageDelete.setOnClickListener {
            mListener.onDeleteSchedule(timeId)
            dismiss()
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
        editTextActions.setOnTouchListener { _, _ ->
            editTextActions.isFocusableInTouchMode = true
            false
        }
        editTextActions.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.isNotEmpty()) {
                    textViewAdd.isClickable = true
                    textViewAdd.isFocusable = true
                    textViewAdd.isEnabled = true
                    TextViewCompat.setTextAppearance(textViewAdd, R.style.TextBoldTeal16)
                } else {
                    textViewAdd.isClickable = false
                    textViewAdd.isFocusable = false
                    textViewAdd.isEnabled = false
                    TextViewCompat.setTextAppearance(textViewAdd, R.style.TextBoldGray16)
                }
            }
        })
    }

    private fun updateNumber(type: String, numberType: String, editText: EditText, max: Int, min: Int) {
        var number = 0
        when(type) {
            TYPE_INCREASE -> {
                when(numberType) {
                    TYPE_START_HOUR -> {
                        startHour++
                        if (startHour > max) startHour = min
                        number = startHour
                    }
                    TYPE_END_HOUR -> {
                        endHour++
                        if (endHour > max) endHour = min
                        number = endHour
                    }
                    TYPE_START_MINUTE -> {
                        startMinute++
                        if (startMinute > max) startMinute = min
                        number = startMinute
                    }
                    TYPE_END_MINUTE -> {
                        endMinute++
                        if (endMinute > max) endMinute = min
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
                        if (startHour < min) startHour = max
                        number = startHour
                    }
                    TYPE_END_HOUR -> {
                        endHour--
                        if (endHour < min) endHour = max
                        number = endHour
                    }
                    TYPE_START_MINUTE -> {
                        startMinute--
                        if (startMinute < min) startMinute = max
                        number = startMinute
                    }
                    TYPE_END_MINUTE -> {
                        endMinute--
                        if (endMinute < min) endMinute = max
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
        bottomSheetDialog.setOnKeyListener { _: DialogInterface?, _: Int, keyEvent: KeyEvent ->
            if (keyEvent.keyCode == KeyEvent.KEYCODE_BACK) dismiss()
            false
        }
        return bottomSheetDialog
    }

    interface ScheduleEditorListener {
        fun onEditSchedule(id: Int, startTime: String, endTime: String, actions: String)
        fun onAddNewSchedule(id: Int, startTime: String, endTime: String, actions: String)
        fun onDeleteSchedule(id: Int)
    }
}