package com.codetech.speechtotext.Utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColor
import com.codetech.speechtotext.R
import org.koin.core.component.getScopeId

// Move interface outside the class
interface OnBlockSelectedListener {
    fun onBlockSelected(originalText: String, translatedText: String)
}

class TranslationOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.white)
        alpha = 200
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 40f
        isAntiAlias = true
    }

    private val selectedBackgroundPaint = Paint().apply {
        color = Color.BLACK
        alpha = 200
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val translatedBlocks = mutableMapOf<Rect, String>()
    private val originalBlocks = mutableMapOf<Rect, String>()
    private var selectedRect: Rect? = null
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isLongPress = false
    private val longPressTimeout = 500L // milliseconds
    private var blockSelectedListener: OnBlockSelectedListener? = null
    private var selectedBlockText: String? = null
    private var selectedOriginalText: String? = null

    fun setTranslatedBlocks(originalTexts: Map<Rect, String>, translatedTexts: Map<Rect, String>) {
        translatedBlocks.clear()
        originalBlocks.clear()
        translatedBlocks.putAll(translatedTexts)
        originalBlocks.putAll(originalTexts)
        invalidate()
    }

    fun setOnBlockSelectedListener(listener: OnBlockSelectedListener) {
        blockSelectedListener = listener
    }

    fun getSelectedBlockText(): String? = selectedBlockText

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                isLongPress = false

                val touchedRect = translatedBlocks.keys.find { rect ->
                    rect.contains(event.x.toInt(), event.y.toInt())
                }
                selectedRect = touchedRect
                selectedBlockText = touchedRect?.let { translatedBlocks[it] }
                selectedOriginalText = touchedRect?.let { originalBlocks[it] }
                blockSelectedListener?.onBlockSelected(selectedOriginalText ?: "", selectedBlockText ?: "")
                invalidate()

                postDelayed({
                    if (lastTouchX == event.x && lastTouchY == event.y) {
                        isLongPress = true
                    }
                }, longPressTimeout)

                return true
            }

            MotionEvent.ACTION_UP -> {
                if (!isLongPress) {
                    val touchedRect = translatedBlocks.keys.find { rect ->
                        rect.contains(event.x.toInt(), event.y.toInt())
                    }
                    touchedRect?.let { rect ->
                        translatedBlocks[rect]?.let { text ->
                            copyToClipboard(text)
                        }
                    }
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (Math.abs(event.x - lastTouchX) > 10 || Math.abs(event.y - lastTouchY) > 10) {
                    isLongPress = false
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Translated Text", text)
        clipboard.setPrimaryClip(clip)
        //Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        translatedBlocks.forEach { (rect, text) ->
            // Draw background
            canvas.drawRect(rect, backgroundPaint)

            // Draw dotted border for selected rect
            if (rect == selectedRect) {
                canvas.drawRect(rect, selectedBackgroundPaint)
            }

            // Calculate and draw text
            val desiredTextSize = calculateTextSize(text, rect)
            textPaint.textSize = desiredTextSize

            val textBounds = Rect()
            textPaint.getTextBounds(text, 0, text.length, textBounds)

            val x = rect.left + (rect.width() - textBounds.width()) / 2f
            val y = rect.top + (rect.height() + textBounds.height()) / 2f

            canvas.drawText(text, x, y, textPaint)
        }
    }

    private fun calculateTextSize(text: String, rect: Rect): Float {
        var textSize = 40f
        textPaint.textSize = textSize

        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)

        val targetWidth = rect.width() * 0.9f
        val targetHeight = rect.height() * 0.8f

        val widthRatio = targetWidth / textBounds.width()
        val heightRatio = targetHeight / textBounds.height()

        val ratio = minOf(widthRatio, heightRatio)

        return textSize * ratio
    }

}