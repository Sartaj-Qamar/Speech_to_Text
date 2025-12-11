package com.codetech.speechtotext.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.codetech.speechtotext.repository.OnboardingRepository

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: OnboardingRepository
) : ViewModel() {

    val onboardingItems = repository.getOnboardingItems()
}
