package com.example.kuntan

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kuntan.utility.KuntanRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SplashActivity : AppCompatActivity() {

    private val database by lazy { KuntanRoomDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        CoroutineScope(Dispatchers.IO).launch {
            val settings = database.settingsDao().getSettings()
            setApplicationLanguage(settings.language)
        }
        startActivity(Intent(this@SplashActivity, DashboardActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        finish()
    }

    private fun setApplicationLanguage(language: String) {
        var languageCode = Locale.getDefault().language
        if (language == getString(R.string.setting_language_bahasa)) languageCode = "id"
        else if (languageCode == getString(R.string.setting_language_english)) languageCode = "en"
        val locale= Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, null)
    }
}