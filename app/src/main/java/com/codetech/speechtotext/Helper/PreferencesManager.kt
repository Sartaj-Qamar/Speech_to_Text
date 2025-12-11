package com.codetech.speechtotext.Helper

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    fun isOnboardingComplete(): Boolean {
        return sharedPreferences.getBoolean("isOnboardingComplete", false)
    }

    fun setOnboardingComplete(isComplete: Boolean) {
        sharedPreferences.edit().putBoolean("isOnboardingComplete", isComplete).apply()
    }
}
