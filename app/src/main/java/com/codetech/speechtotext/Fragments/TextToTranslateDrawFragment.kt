package com.codetech.speechtotext.Fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.codetech.speechtotext.Utils.DrawView
import com.codetech.speechtotext.Utils.DrawingListener
import com.codetech.speechtotext.databinding.FragmentTextToTranslateDrawBinding
import com.codetech.speechtotext.models.StrokeManager

class TextToTranslateDrawFragment : Fragment(), DrawingListener {
    private lateinit var binding: FragmentTextToTranslateDrawBinding
    private lateinit var btnTranslate: ImageButton
    private lateinit var btnClear: ImageButton
    private lateinit var btnRedo: ImageButton
    private lateinit var drawView: DrawView
    private lateinit var textView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTextToTranslateDrawBinding.inflate(inflater, container, false)

        // Initialize views
        btnTranslate = binding.next
        btnClear = binding.brushColorButton
        btnRedo = binding.redoButton
        drawView = binding.drawingArea
        textView = binding.editTextView

        // StrokeManager.download()
        //  drawView.setDrawingListener(this)

        btnClear.setOnClickListener {
            // drawView.clear()
            StrokeManager.clear()
            textView.text = ""
        }

        return binding.root
    }

    override fun onDrawingChanged() {
        // Not needed for this implementation
    }

    override fun onDrawingStopped() {
        if (isAdded) {
            requireActivity().runOnUiThread {
                //                  StrokeManager.recognize(textView)
            }
        }
    }
}
