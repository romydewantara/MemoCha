package com.example.kuntan.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import com.example.kuntan.R
import com.example.kuntan.utility.AppUtil
import kotlinx.android.synthetic.main.fragment_surname.*

@SuppressLint("ClickableViewAccessibility")
class SurnameFragment(private val mSurnameState: Boolean, private val mSurname: String) : Fragment(R.layout.fragment_surname) {

    var surnameState = false
    var surname = ""
    lateinit var textWatcher: TextWatcher
    lateinit var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootLayoutSurname.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    globalLayoutListener = this
                    if (!AppUtil.isKeyboardVisible(rootLayoutSurname)) {
                        editTextSurname.isFocusable = false
                    }
                }
            })

        surnameState = mSurnameState
        surname = mSurname
        if (surnameState) {
            surnameOnOff.isChecked = true
            editTextSurname.visibility = View.VISIBLE
            textViewSurnameInfo.visibility = View.VISIBLE
            editTextSurname.setText(surname)
        }

        surnameOnOff.setOnCheckedChangeListener { _, isChecked ->
            surnameState = isChecked
            if (isChecked) {
                editTextSurname.visibility = View.VISIBLE
                textViewSurnameInfo.visibility = View.VISIBLE
            } else {
                editTextSurname.visibility = View.GONE
                textViewSurnameInfo.visibility = View.GONE
            }
        }

        editTextSurname.addTextChangedListener(onTextChangedListener())
        editTextSurname.setOnTouchListener { _, _ ->
            editTextSurname.isFocusableInTouchMode = true
            false
        }
    }

    private fun onTextChangedListener(): TextWatcher {
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.isNotEmpty()) surname = s.toString().trim() else surname = ""
            }
        }
        return textWatcher
    }

    fun removeTextChangedListener() {
        editTextSurname.removeTextChangedListener(textWatcher)
        rootLayoutSurname.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
    }

}