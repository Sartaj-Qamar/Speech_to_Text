package com.codetech.speechtotext.Fragments

import com.codetech.speechtotext.Adapters.OnboardingAdapter
import com.codetech.speechtotext.viewmodels.OnboardingViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.codetech.speechtotext.Dialog.showExitAppDialog
import com.codetech.speechtotext.R
import com.codetech.speechtotext.databinding.FragmentOnboardingBinding
import com.codetech.speechtotext.Helper.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup the ViewPager2 adapter
        val adapter = OnboardingAdapter(viewModel.onboardingItems)
        binding.onboardingViewPager.adapter = adapter

        // Link DotsIndicator with ViewPager2
        binding.indicator.setViewPager2(binding.onboardingViewPager)

        binding.btn.setOnClickListener {
            val currentItem = binding.onboardingViewPager.currentItem
            if (currentItem < adapter.itemCount - 1) {
                //Scroll to next item
                binding.onboardingViewPager.currentItem = currentItem + 1
            } else {
                preferencesManager.setOnboardingComplete(true)
                findNavController().navigate(R.id.speechToTextFragment)
            }
        }

        binding.onboardingViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == adapter.itemCount - 1) {
                    binding.btn.setText(R.string.get_started)
                } else {
                    binding.btn.setText(R.string.continue_text)
                }
            }
        }
        )


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                showExitAppDialog(requireActivity())
            }
        })

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
