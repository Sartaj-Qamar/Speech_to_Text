package com.codetech.speechtotext.Fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.codetech.speechtotext.Activity.MainActivity
import com.codetech.speechtotext.Adapters.MeaningPagerAdapter
import com.codetech.speechtotext.R
import com.codetech.speechtotext.Utils.PermissionUtils
import com.codetech.speechtotext.Utils.gone
import com.codetech.speechtotext.Utils.isKeyboardVisible
import com.codetech.speechtotext.Utils.safeClickListener
import com.codetech.speechtotext.Utils.visible
import com.codetech.speechtotext.VM.HistoryViewModel
import com.codetech.speechtotext.data_source.RetrofitInstance
import com.codetech.speechtotext.databinding.FragmentDictionaryBinding
import com.codetech.speechtotext.models.WordResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DictionaryFragment : Fragment() {

    private lateinit var binding: FragmentDictionaryBinding
    private lateinit var adapter: MeaningPagerAdapter

    // private lateinit var bottomNav: LinearLayout
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var lastKeyboardState = false
    private lateinit var historyViewModel: HistoryViewModel

    private val speechRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val results = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                val recognizedText = results[0].trim()
                val words = recognizedText.split(" ")

                if (words.size > 1) {
                    Toast.makeText(requireActivity(), "Only the first word will be used.", Toast.LENGTH_SHORT).show()
                }

                val firstWord = words[0]
                binding.searchInputField.text = Editable.Factory.getInstance().newEditable(firstWord)
                getMeaning(firstWord)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDictionaryBinding.inflate(inflater, container, false)

        historyViewModel = ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java)

        //  bottomNav = (activity as MainActivity).findViewById(R.id.bottomMenu)
        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val isKeyboardVisibleNow = binding.root.isKeyboardVisible()
            if (isKeyboardVisibleNow != lastKeyboardState) {
                lastKeyboardState = isKeyboardVisibleNow
                if (isKeyboardVisibleNow) {
                    // bottomNav.gone()
                } else {
                    // bottomNav.visible()
                }
            }
        }
        binding.root.viewTreeObserver?.addOnGlobalLayoutListener(globalLayoutListener)



        adapter = MeaningPagerAdapter(this)
        binding.meaningViewPager.adapter = adapter

        createDots(adapter.itemCount)

        binding.meaningViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateDots(position)
            }
        })


        binding.searchInputField.setOnClickListener {
            //   binding.searchInputField.text.clear()

            binding.searchInputField.requestFocus()

            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchInputField, InputMethodManager.SHOW_IMPLICIT)
        }



        binding.searchInputField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val word = binding.searchInputField.text.toString()
                getMeaning(word)
                historyViewModel.addToHistory(word) // Add word to history
                true
            } else {
                false
            }
        }

        binding.searchInputField.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            if (source.contains(" ")) {
                Toast.makeText(requireContext(), "Only one word is allowed", Toast.LENGTH_SHORT).show()
                ""
            } else {
                source
            }
        })

        binding.search.safeClickListener {
            val word = binding.searchInputField.text.toString()
            getMeaning(word)
        }

        binding.mic.setOnClickListener {
            checkMicrophonePermission()
        }

        binding.clear.safeClickListener {
            val handler = Handler(Looper.getMainLooper())
            val delay = 1L
            binding.searchInputField.text = Editable.Factory.getInstance().newEditable("")

            val runnable = object : Runnable {
                override fun run() {
                    val currentText = binding.searchInputField.text.toString()
                    if (currentText.isNotEmpty()) {
                        val updatedText = currentText.dropLast(1)
                        binding.searchInputField.text = Editable.Factory.getInstance().newEditable(updatedText)
                        handler.postDelayed(this, delay)
                    }
                }
            }

            handler.post(runnable)
            binding.meaningViewPager.visibility = View.GONE
            binding.dotContainer.visibility = View.GONE
        }

        return binding.root
    }

    private fun checkMicrophonePermission() {

        val micPermission = arrayOf(Manifest.permission.RECORD_AUDIO)
        PermissionUtils.checkPermission(
            requireContext(), permissionArray = micPermission,
            object : PermissionUtils.OnPermissionListener {
                override fun onPermissionSuccess() {
                    startSpeechRecognition()
                }
            }

        )
    }


    private fun createDots(count: Int) {
        binding.dotContainer.removeAllViews()
        for (i in 0 until count) {
            val dotView = LayoutInflater.from(binding.dotContainer.context)
                .inflate(R.layout.custom_tab_dot, binding.dotContainer, false)
            binding.dotContainer.addView(dotView)
        }
        updateDots(0)
    }

    private fun updateDots(selectedPosition: Int) {
        for (i in 0 until binding.dotContainer.childCount) {
            val dotView = binding.dotContainer.getChildAt(i).findViewById<View>(R.id.dot)
            dotView.setBackgroundResource(
                if (i == selectedPosition)
                    R.drawable.circle_shape
                else
                    R.drawable.circle_shapes
            )
        }
    }

    private fun setUI(response: WordResult) {
        Log.d("SIZE", "Size is ${response.meanings.size}")

        // Reset ViewPager to first position
        binding.meaningViewPager.setCurrentItem(0, false)

        // Update adapter with new data
        adapter.updateNewData(response.meanings)

        // Update dots
        createDots(response.meanings.size)
        updateDots(0)

        // Ensure views are visible
        binding.meaningViewPager.visibility = View.VISIBLE
        binding.dotContainer.visibility = View.VISIBLE
    }

    private fun getMeaning(word: String) {
        if (word.isEmpty()) {
            return Toast.makeText(requireContext(), "Text is Empty", Toast.LENGTH_SHORT).show()
        }

        // Clear previous results first
        adapter.updateNewData(emptyList())
        binding.meaningViewPager.visibility = View.VISIBLE
        binding.dotContainer.visibility = View.VISIBLE

        setInProgress(true)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.dictionaryApi.getMeaning(word)
                Log.d("API Response", response.toString())

                if (response.body() == null || response.body()?.isEmpty() == true) {
                    throw Exception("No meanings found")
                }

                withContext(Dispatchers.Main) {
                    setInProgress(false)
                    response.body()?.first()?.let { result ->
                        // Make sure ViewPager is visible before updating
                        binding.meaningViewPager.visibility = View.VISIBLE
                        binding.dotContainer.visibility = View.VISIBLE
                        setUI(result)
                    } ?: run {
                        Toast.makeText(requireContext(), "No meanings found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setInProgress(false)
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Hide views on error
                    binding.meaningViewPager.visibility = View.GONE
                    binding.dotContainer.visibility = View.GONE
                }
            }
        }
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            speechRecognitionLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireActivity(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setInProgress(inProgress: Boolean) {
        binding.lottie.visibility = if (inProgress) View.VISIBLE else View.INVISIBLE
        binding.meaningViewPager.visibility = if (inProgress) View.INVISIBLE else View.VISIBLE
        binding.mic.visibility = if (inProgress) View.INVISIBLE else View.VISIBLE

    }
}
