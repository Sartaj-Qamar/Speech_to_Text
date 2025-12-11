package com.codetech.speechtotext.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.codetech.speechtotext.Adapters.LanguageAdapter
import com.codetech.speechtotext.Helper.LocaleUtils
import com.codetech.speechtotext.R
import com.codetech.speechtotext.application.BaseFragment
import com.codetech.speechtotext.databinding.FragmentLanguageBinding
import com.codetech.speechtotext.models.LanguageData
import java.util.Locale


class LanguageFragment : BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate), LanguageAdapter.OnLanguageItemCLick {
    private var languageAdapter: LanguageAdapter? = null
    private var isFromBoarding = false
    private var _binding: FragmentLanguageBinding? = null
    override val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        val view = binding.root
        setupLanguageRcv()

        arguments?.let {
            isFromBoarding = it.getBoolean("fromBoarding", false)
        }

        return view
    }

    private fun setupLanguageRcv() {
        languageAdapter = LanguageAdapter(getLanguageData(), this, tinyDB)
        binding.languageRcv.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = languageAdapter
        }
    }

    override fun onLanguageClick(holder: LanguageData, position: Int) {
        tinyDB.putString("currentLanguage", holder.languageName)
        tinyDB.putString("My_Lang", holder.languageCode)
        val locale = Locale(holder.languageCode)
        LocaleUtils.setLocale(locale)
        LocaleUtils.updateConfig(requireContext(), locale)
        LocaleUtils.saveLocale(requireContext(), holder.languageCode)

        binding.doneButton.visibility = View.VISIBLE
        binding.doneButton.setOnClickListener {
            requireActivity().recreate()
            navigate()
        }
    }

    private fun navigate() {
        findNavController().navigate(R.id.speechToTextFragment)
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

    private fun getLanguageData(): java.util.ArrayList<LanguageData> {
        return arrayListOf(
            LanguageData(R.drawable.flag_uk, "English", "en"),
            LanguageData(R.drawable.flag_germany, "Germany", "de"),
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
}
