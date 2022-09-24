package com.example.kuntan.lib

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.kuntan.R
import kotlinx.android.synthetic.main.layout_kuntan_popup_dialog.*

class KuntanPopupDialog : DialogFragment() {

    private lateinit var kuntanPopupDialogListener: KuntanPopupDialogListener
    private lateinit var textPopupTitle: String
    private lateinit var textPopupSubtitle: String
    private lateinit var textNegativeButton: String
    private lateinit var textPositiveButton: String

    companion object {
        fun newInstance(): KuntanPopupDialog {
            return KuntanPopupDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (dialog != null && dialog?.window != null) dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_kuntan_popup_dialog, container, false)
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

        if (textNegativeButton.isEmpty()) negativeButton.visibility = View.GONE
        negativeButton.setOnClickListener {
            dismiss()
            kuntanPopupDialogListener.onNegativeButton()
        }
        positiveButton.setOnClickListener {
            dismiss()
            kuntanPopupDialogListener.onPositiveButton()
        }
    }

    fun setContent(
        title: String,
        subtitle: String,
        negative: String,
        positive: String,
        kuntanPopupDialogListener: KuntanPopupDialogListener
    ) : KuntanPopupDialog {
        textPopupTitle = title
        textPopupSubtitle = subtitle
        textNegativeButton = negative
        textPositiveButton = positive
        this.kuntanPopupDialogListener = kuntanPopupDialogListener
        return this
    }

    interface KuntanPopupDialogListener {
        fun onNegativeButton()
        fun onPositiveButton()
    }
}