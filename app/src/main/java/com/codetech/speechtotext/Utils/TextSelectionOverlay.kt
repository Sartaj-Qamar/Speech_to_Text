package com.codetech.speechtotext.Utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class TextSelectionOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint().apply {
        color = Color.BLUE
        alpha = 50
        style = Paint.Style.FILL
    }
    
    private val textBlocks = mutableMapOf<Rect, String>()
    private var selectedRect: Rect? = null
    
    fun setTextBlocks(blocks: Map<Rect, String>) {
        textBlocks.clear()
        textBlocks.putAll(blocks)
        invalidate()
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchX = event.x.toInt()
                val touchY = event.y.toInt()
                
                selectedRect = textBlocks.keys.find { rect ->
                    rect.contains(touchX, touchY)
                }
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        selectedRect?.let {
            canvas.drawRect(it, paint)
        }
    }
    
    fun getSelectedText(): String? {
        return selectedRect?.let { textBlocks[it] }
    }
} 