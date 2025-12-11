package com.codetech.speechtotext.Helper

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleUtils {
    fun setLocale(locale: Locale) {
        Locale.setDefault(locale)
    }

    fun updateConfig(context: Context, locale: Locale) {
        setLocale(locale)
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            config.locale = locale
        }
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun saveLocale(context: Context, localeCode: String) {
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("LANGUAGE", localeCode)
        editor.apply()
    }

    fun loadLocale(context: Context) {
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val localeCode = sharedPreferences.getString("LANGUAGE", Locale.getDefault().language)
        val locale = Locale(localeCode ?: Locale.getDefault().language)
        updateConfig(context, locale)
    }
}
