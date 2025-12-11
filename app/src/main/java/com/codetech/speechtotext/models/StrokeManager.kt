package com.codetech.speechtotext.models

import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.*

object StrokeManager {
    private var recognizer: DigitalInkRecognizer? = null
    private var inkBuilder = Ink.builder()
    private var strokeBuilder: Ink.Stroke.Builder? = null
    private var isRecognizing = false

    // Add property to track recognized text positions
    private data class RecognitionState(
        val ink: Ink,
        val textPosition: Int,
        val textLength: Int
    )

    private val recognitionHistory = mutableListOf<RecognitionState>()


    fun downloadLanguage(languageTag: String = "en-US", loader: LottieAnimationView) {
        // Show the loader when download starts
        loader.visibility = View.VISIBLE
        loader.playAnimation()

        val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag)
            ?: throw IllegalStateException("Language not supported")

        val model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
        val remoteModelManager = RemoteModelManager.getInstance()

        // Start downloading the model
        remoteModelManager.download(model, DownloadConditions.Builder().build())
            .addOnSuccessListener {
                // Hide the loader when download is successful
                loader.visibility = View.GONE
                loader.cancelAnimation()

                recognizer = DigitalInkRecognition.getClient(
                    DigitalInkRecognizerOptions.builder(model).build()
                )
            }
            .addOnFailureListener { e ->
                // Hide the loader if download fails
                loader.visibility = View.GONE
                loader.cancelAnimation()
                Log.e(TAG, "Error downloading model: $e")
            }
    }

    fun initLanguageModel(languageTag: String = "en-US") {
        val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag)
            ?: throw IllegalStateException("Language not supported")

        val model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
        val remoteModelManager = RemoteModelManager.getInstance()

        remoteModelManager.download(model, DownloadConditions.Builder().build())
            .addOnSuccessListener {
                recognizer = DigitalInkRecognition.getClient(
                    DigitalInkRecognizerOptions.builder(model).build()
                )
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error downloading model: $e")
            }
    }

    fun addNewTouchEvent(event: MotionEvent) {
        val x = event.x
        val y = event.y
        val t = System.currentTimeMillis()

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                strokeBuilder = Ink.Stroke.builder()
                strokeBuilder?.addPoint(Ink.Point.create(x, y, t))
            }

            MotionEvent.ACTION_MOVE -> {
                strokeBuilder?.addPoint(Ink.Point.create(x, y, t))
            }

            MotionEvent.ACTION_UP -> {
                strokeBuilder?.addPoint(Ink.Point.create(x, y, t))
                strokeBuilder?.build()?.let { inkBuilder.addStroke(it) }
                strokeBuilder = null
            }
        }
    }

    fun recognize(editText: EditText, language: String) {
        if (recognizer == null || isRecognizing) return

        isRecognizing = true
        val ink = inkBuilder.build()
        val currentPosition = editText.selectionStart
        val currentText = editText.text.toString()

        recognizer?.recognize(ink)
            ?.addOnSuccessListener { result ->
                if (result.candidates.isNotEmpty()) {
                    val recognizedText = result.candidates[0].text

                    val (updatedText, newCursorPosition) = when (language) {
                        "en-US" -> handleEnglishCompletion(currentText, currentPosition, recognizedText)
                        "ar" -> handleArabicCompletion(currentText, currentPosition, recognizedText)
                        else -> handleGenericCompletion(currentText, currentPosition, recognizedText)
                    }

                    editText.post {
                        editText.setText(updatedText)
                        editText.setSelection(newCursorPosition) // Move cursor to the correct position
                    }
                }
            }
            ?.addOnFailureListener { e ->
                Log.e(TAG, "Error during recognition: $e")
            }
            ?.addOnCompleteListener {
                clear()
                isRecognizing = false
            }
    }


    // Handle word completion for English (left-to-right, space after word)
    private fun handleEnglishCompletion(
        currentText: String,
        cursorPosition: Int,
        recognizedText: String
    ): Pair<String, Int> {
        val beforeCursor = currentText.substring(0, cursorPosition)
        val afterCursor = currentText.substring(cursorPosition)
        val space = if (beforeCursor.isNotEmpty() && beforeCursor.last() != ' ') " " else ""

        val updatedText = "$beforeCursor$space$recognizedText$afterCursor"
        val newCursorPosition = beforeCursor.length + space.length + recognizedText.length

        return Pair(updatedText, newCursorPosition)
    }


    // Handle word completion for Arabic (right-to-left)
    private fun handleArabicCompletion(
        currentText: String,
        cursorPosition: Int,
        recognizedText: String
    ): Pair<String, Int> {
        val beforeCursor = currentText.substring(0, cursorPosition)
        val afterCursor = currentText.substring(cursorPosition)

        val updatedText = "$beforeCursor$recognizedText$afterCursor"
        val newCursorPosition = beforeCursor.length + recognizedText.length

        return Pair(updatedText, newCursorPosition)
    }


    // Generic fallback for unsupported languages
    private fun handleGenericCompletion(
        currentText: String,
        cursorPosition: Int,
        recognizedText: String
    ): Pair<String, Int> {
        val beforeCursor = currentText.substring(0, cursorPosition)
        val afterCursor = currentText.substring(cursorPosition)

        val updatedText = "$beforeCursor$recognizedText$afterCursor"
        val newCursorPosition = beforeCursor.length + recognizedText.length

        return Pair(updatedText, newCursorPosition)
    }


    fun clear() {
        inkBuilder = Ink.builder()
        recognitionHistory.clear()
    }

    private const val TAG = "StrokeManager"
}
