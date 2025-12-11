package com.codetech.speechtotext.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.codetech.speechtotext.R
import com.codetech.speechtotext.Utils.safeClickListener
import com.codetech.speechtotext.databinding.FragmentDrawableBinding

class DrawableFragment : Fragment() {
    private var _binding: FragmentDrawableBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrawableBinding.inflate(inflater, container, false)
        setupClickListeners()
        return binding.root
    }

    private fun setupClickListeners() {
        binding.apply {
            languageLayout.safeClickListener {
                Log.d("Language", "Language Selected")
               // findNavController().navigate(R.id.languageFragment)
            }

            favLayout.safeClickListener {
                Log.d("Favorite", "Favorite Selected")
                //findNavController().navigate(R.id.favouriteFragment)
            }

            historyLayout.safeClickListener {
                Log.d("History", "History Selected")
              //  findNavController().navigate(R.id.historyFragment)
            }

            rateUsLayout.safeClickListener {
                Log.d("RateUs", "Rate Us Selected")
                Toast.makeText(requireContext(), "Rate Us Clicked", Toast.LENGTH_SHORT).show()
            }

            shareLayout.safeClickListener {
                Log.d("Share", "Share Selected")
                Toast.makeText(requireContext(), "Share Clicked", Toast.LENGTH_SHORT).show()
            }

            exitLayout.safeClickListener {
                Log.d("Exit", "Exit Selected")
                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}