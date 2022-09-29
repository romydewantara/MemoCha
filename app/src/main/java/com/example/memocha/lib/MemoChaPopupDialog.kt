package com.example.memocha.lib

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.memocha.R
import kotlinx.android.synthetic.main.layout_memocha_popup_dialog.*

class MemoChaPopupDialog : DialogFragment() {

    private lateinit var memoChaPopupDialogListener: MemoChaPopupDialogListener
    private lateinit var textPopupTitle: String
    private lateinit var textPopupSubtitle: String
    private lateinit var textNegativeButton: String
    private lateinit var textPositiveButton: String

    companion object {
        fun newInstance(): MemoChaPopupDialog {
            return MemoChaPopupDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (dialog != null && dialog?.window != null) dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_memocha_popup_dialog, container, false)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        if (dialog != null && dialog?.window != null)
            dialog?.window?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.background_popup_dialog, null))

        textViewPopupTitle.text = textPopupTitle
        textViewPopupSubtitle.text = textPopupSubtitle
        negativeButton.text = textNegativeButton
        positiveButton.text = textPositiveButton

        if (textPopupTitle == "") textViewPopupTitle.visibility = View.GONE
        if (textNegativeButton.isEmpty()) negativeButton.visibility = View.GONE
        negativeButton.setOnClickListener {
            dismiss()
            memoChaPopupDialogListener.onNegativeButton()
        }
        positiveButton.setOnClickListener {
            dismiss()
            memoChaPopupDialogListener.onPositiveButton()
        }
    }

    fun setContent(
        title: String,
        subtitle: String,
        negative: String,
        positive: String,
        memoChaPopupDialogListener: MemoChaPopupDialogListener
    ) : MemoChaPopupDialog {
        textPopupTitle = title
        textPopupSubtitle = subtitle
        textNegativeButton = negative
        textPositiveButton = positive
        this.memoChaPopupDialogListener = memoChaPopupDialogListener
        return this
    }

    interface MemoChaPopupDialogListener {
        fun onNegativeButton()
        fun onPositiveButton()
    }
}