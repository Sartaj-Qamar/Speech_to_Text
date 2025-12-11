package com.codetech.speechtotext.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.codetech.speechtotext.R
import com.codetech.speechtotext.databinding.FragmentPremiumBinding
import com.codetech.speechtotext.databinding.FragmentSpeechToTextBinding


class PremiumFragment : Fragment() {
    lateinit var binding: FragmentPremiumBinding
    private var selectedPlan: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPremiumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupClickListener()

    }

    private fun setupClickListener() {
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.plan1Layout.setOnClickListener {
            selectPlan(1, it)
        }

        binding.plan2Layout.setOnClickListener {
            selectPlan(2, it)
        }

        binding.tvTerms.setOnClickListener {
            Toast.makeText(requireContext(), "Privacy", Toast.LENGTH_SHORT).show()
        }

        binding.tvPrivacy.setOnClickListener {
            Toast.makeText(requireContext(), "Terms", Toast.LENGTH_SHORT).show()
        }

        binding.tvRestore.setOnClickListener {
            Toast.makeText(requireContext(), "Restore", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectPlan(plan: Int, selectedView: View) {
        // Reset previously selected plan
        selectedPlan?.isSelected = false
        resetPlanColors()

        when (plan) {
            1 -> {
                binding.apply {
                    plan1Title.setTextColor(requireContext().getColor(R.color.bg_primary))
                    plan1Duration.setTextColor(requireContext().getColor(R.color.bg_primary))
                    plan1Price.setTextColor(requireContext().getColor(R.color.bg_primary))
                    plan1Month.setTextColor(requireContext().getColor(R.color.bg_primary))
                }
            }

            2 -> {
                binding.apply {
                    plan2Title.setTextColor(requireContext().getColor(R.color.bg_primary))
                    plan2Duration.setTextColor(requireContext().getColor(R.color.bg_primary))
                    plan2Price.setTextColor(requireContext().getColor(R.color.bg_primary))
                    plan2Month.setTextColor(requireContext().getColor(R.color.bg_primary))
                }
            }
        }

        // Highlight the selected plan
        selectedView.isSelected = true

        // Set the selected view as the current selected plan
        selectedPlan = selectedView

    }

    private fun resetPlanColors() {

        //Reset Plan1 Colors
        binding.plan1Title.setTextColor(requireContext().getColor(R.color.text_dark_black))
        binding.plan1Duration.setTextColor(requireContext().getColor(R.color.text_dark_black))
        binding.plan1Price.setTextColor(requireContext().getColor(R.color.text_dark_black))
        binding.plan1Month.setTextColor(requireContext().getColor(R.color.text_dark_black))

        //Reset Plan2 Colors
        binding.plan2Title.setTextColor(requireContext().getColor(R.color.text_dark_black))
        binding.plan2Duration.setTextColor(requireContext().getColor(R.color.text_dark_black))
        binding.plan2Price.setTextColor(requireContext().getColor(R.color.text_dark_black))
        binding.plan2Month.setTextColor(requireContext().getColor(R.color.text_dark_black))

    }
}