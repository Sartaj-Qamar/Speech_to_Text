package com.codetech.speechtotext.application

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.codetech.speechtotext.Helper.LocaleUtils
import com.codetech.speechtotext.R
import com.codetech.speechtotext.models.LanguageData
import java.util.Locale

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Load saved locale
        LocaleUtils.loadLocale(this)
    }

    override fun attachBaseContext(base: Context) {
        // Get saved language code
        val languageCode = base.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            .getString("LANGUAGE", Locale.getDefault().language)

        // Create configuration with saved locale
        val config = Configuration(base.resources.configuration)
        val locale = Locale(languageCode ?: Locale.getDefault().language)
        Locale.setDefault(locale)
        config.setLocale(locale)

        super.attachBaseContext(base.createConfigurationContext(config))
    }

    private fun getLanguageData(): ArrayList<LanguageData> {
        return arrayListOf(
            LanguageData(R.drawable.flag_uk, "English", "en"),
            LanguageData(R.drawable.flag_germany, "German", "de"),
            LanguageData(R.drawable.flag_portuguese, "Portuguese", "pt"),
            LanguageData(R.drawable.flag_spanish, "Spanish", "es"),
            LanguageData(R.drawable.flag_french, "French", "fr"),
            LanguageData(R.drawable.flag_india, "Hindi", "hi"),
            LanguageData(R.drawable.flag_bangladesh, "Bengali", "bn"),
            LanguageData(R.drawable.flag_arabic, "Arabic", "ar"),
            LanguageData(R.drawable.flag_indonesia, "Indonesian", "id"),
            LanguageData(R.drawable.flag_japan, "Japanese", "ja"),
        )
    }
} 