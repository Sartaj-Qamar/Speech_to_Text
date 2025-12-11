package com.codetech.speechtotext.repository

import android.content.Context
import androidx.core.content.ContextCompat
import com.codetech.speechtotext.Adapters.OnboardingItem
import com.codetech.speechtotext.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject


class OnboardingRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getOnboardingItems(): List<OnboardingItem> {
        return listOf(
            OnboardingItem(
                imageResId = R.drawable.onboarding_img1,
                title = context.getString(R.string.onboarding_title_1),
                description = context.getString(R.string.onboarding_desc_1)
            ),
            OnboardingItem(
                imageResId = R.drawable.onboarding_img2,
                title = context.getString(R.string.onboarding_title_2),
                description = context.getString(R.string.onboarding_desc_2)
            ),
            OnboardingItem(
                imageResId = R.drawable.onboarding_img3,
                title = context.getString(R.string.onboarding_title_3),
                description = context.getString(R.string.onboarding_desc_3)
            )
        )
    }
}
