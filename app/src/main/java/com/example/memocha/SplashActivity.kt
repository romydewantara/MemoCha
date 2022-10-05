package com.example.memocha

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.memocha.lib.MemoChaPopupDialog
import com.example.memocha.utility.Constant
import com.example.memocha.utility.MemoChaRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

@SuppressLint("MissingSuperCall")
class SplashActivity : AppCompatActivity() {

    companion object {
        const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101
    }

    private val database by lazy { MemoChaRoomDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED) {
            goToDashboard()
        } else {
            requestPermission()
        }
    }

    private fun goToDashboard() {
        CoroutineScope(Dispatchers.IO).launch {
            val settings = database.settingsDao().getSettings()
            if (settings != null) setApplicationLanguage(settings.applicationLanguage)
        }
        startActivity(Intent(this@SplashActivity, DashboardActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        finish()
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
    }

    private fun showPopUpRationale() {
        val memoChaPopupDialog = MemoChaPopupDialog.newInstance().setContent(getString(R.string.dialog_title_information),
            getString(R.string.dialog_message_permission), getString(R.string.button_no_thanks), getString(R.string.button_ok),
            object : MemoChaPopupDialog.MemoChaPopupDialogListener{
                override fun onNegativeButton() {
                    finish()
                }
                override fun onPositiveButton() {
                    requestPermission()
                }
            })
        memoChaPopupDialog.show(supportFragmentManager, memoChaPopupDialog.tag)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            WRITE_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    goToDashboard()
                } else {
                    showPopUpRationale()
                }
            }
        }
    }

    private fun setApplicationLanguage(language: String) {
        var languageCode = Locale.getDefault().language
        if (language == getString(R.string.setting_language_bahasa)) languageCode = Constant.APP_LANG_INDONESIA
        else if (languageCode == getString(R.string.setting_language_english)) languageCode = Constant.APP_LANG_ENGLISH
        val locale= Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, null)
    }
}