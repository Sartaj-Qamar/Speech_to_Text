package com.codetech.speechtotext.Fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codetech.speechtotext.R
import com.codetech.speechtotext.databinding.FragmentSplashBinding
import com.codetech.speechtotext.Helper.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private lateinit var binding: FragmentSplashBinding
    private var counter = 0

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSplashBinding.inflate(inflater, container, false)

        progressBar()

        Handler(Looper.getMainLooper()).postDelayed({
            updateUIForSplashCompletion()
        }, 5000)

        setupButtonClickListener()

        return binding.root
    }

    private fun progressBar() {
        binding.splashProgress.scaleY = 3f
        val timer = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                counter++
                binding.splashProgress.progress = counter
                if (counter == 100) {
                    timer.cancel()
                    lifecycleScope.launch {
                        updateUIForSplashCompletion()
                    }
                }
            }
        }
        timer.schedule(task, 0, 100)
    }

    private fun updateUIForSplashCompletion() {
        binding.splashProgress.visibility = View.INVISIBLE
        binding.btnStart.visibility = View.VISIBLE
    }

    private fun setupButtonClickListener() {
        binding.btnStart.setOnClickListener {

            if (preferencesManager.isOnboardingComplete()) {
                findNavController().navigate(R.id.speechToTextFragment)
            } else {
                findNavController().navigate(R.id.onboardingFragment)
            }
        }
    }
}
