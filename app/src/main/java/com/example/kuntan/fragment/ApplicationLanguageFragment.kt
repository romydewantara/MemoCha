package com.example.kuntan.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.kuntan.R
import com.example.kuntan.entity.Settings
import kotlinx.android.synthetic.main.fragment_application_language.*

class ApplicationLanguageFragment(private val mApplicationLanguage: String) : Fragment(R.layout.fragment_application_language) {

    var language = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectLanguage(mApplicationLanguage)

        languageEnglish.setOnClickListener {
            selectLanguage(requireContext().getString(R.string.setting_language_english))
        }
        languageBahasa.setOnClickListener {
            selectLanguage(requireContext().getString(R.string.setting_language_bahasa))
        }
    }

    private fun selectLanguage(languageSelected: String) {
        when (languageSelected) {
            getString(R.string.setting_language_english) -> {
                checkedEnglish.visibility = View.VISIBLE
                checkedBahasa.visibility = View.GONE
                language = languageSelected
            }
            getString(R.string.setting_language_bahasa) -> {
                checkedEnglish.visibility = View.GONE
                checkedBahasa.visibility = View.VISIBLE
                language = languageSelected
            }
        }
    }
}