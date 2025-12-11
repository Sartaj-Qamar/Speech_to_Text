package com.codetech.speechtotext.application

import android.app.Application
import com.codetech.speechtotext.koin.InitKoin
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
//            applySavedTheme()
        InitKoin(this).loadKoin()
    }

    /* private fun applySavedTheme() {
         val themeMode = SharedPreferencesTheme.getThemePreference(this)
         AppCompatDelegate.setDefaultNightMode(themeMode)
     }*/
}