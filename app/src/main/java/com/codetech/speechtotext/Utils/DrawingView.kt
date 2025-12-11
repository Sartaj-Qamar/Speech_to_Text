package com.codetech.speechtotext.Utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.codetech.speechtotext.models.StrokeManager

interface DrawingListener {
    fun onDrawingChanged()
    fun onDrawingStopped()
}

class DrawView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val currentStrokePaint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        strokeWidth = 15.0f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    // List of stroke groups, each group contains multiple paths

    private val strokeHistory: MutableList<MutableList<Path>> = mutableListOf()
    private val currentGroup: MutableList<Path> = mutableListOf()
    private var currentStroke = Path()
    private lateinit var canvasBitmap: Bitmap
    private lateinit var drawCanvas: Canvas
    private val canvasPaint = Paint(Paint.DITHER_FLAG)
    private var drawingListener: DrawingListener? = null
    private var isDrawing = false
    private var isRecognizing = false
    private var editText: EditText? = null

    private val handler = Handler(Looper.getMainLooper())
    private var recognitionRunnable: Runnable? = null
    private var isInteractive = true // Tracks whether interaction is allowed
    private val drawingTimeoutHandler = Handler(Looper.getMainLooper())
    private var drawingTimeoutRunnable: Runnable? = null


    private data class StrokeGroup(
        val paths: MutableList<Path>,
        val textPosition: Int,
        val textLength: Int
    )

    private val strokeGroups = mutableListOf<StrokeGroup>()
    private var currentTextPosition = 0

    private val drawingStopRunnable = Runnable {
        if (isDrawing) {

            isDrawing = false
            drawingListener?.onDrawingStopped()
        }
    }

    fun setDrawingListener(listener: DrawingListener) {
        drawingListener = listener
    }

    init {
        currentStroke = Path()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(canvasBitmap, 0f, 0f, canvasPaint)
        canvas.drawPath(currentStroke, currentStrokePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDrawing = true
                currentStroke = Path()
                currentStroke.moveTo(x, y)
                StrokeManager.addNewTouchEvent(event)
                cancelDrawingTimeout() // Reset timeout since user started drawing
            }

            MotionEvent.ACTION_MOVE -> {
                currentStroke.lineTo(x, y)
                StrokeManager.addNewTouchEvent(event)
            }

            MotionEvent.ACTION_UP -> {
                currentStroke.lineTo(x, y)
                drawCanvas.drawPath(currentStroke, currentStrokePaint)
                currentGroup.add(Path(currentStroke)) // Add a copy of the path to current group
                currentStroke = Path()
                StrokeManager.addNewTouchEvent(event)
                startDrawingTimeout() // Start timeout for detecting inactivity
            }
        }

        invalidate()
        return true
    }

    private fun startDrawingTimeout() {
        cancelDrawingTimeout() // Ensure no duplicate timeouts are running
        drawingTimeoutRunnable = Runnable {
            if (isDrawing) {
                isDrawing = false
                drawingListener?.onDrawingStopped()
            }
            handleDrawingTimeout() // Handle timeout logic
        }
        drawingTimeoutHandler.postDelayed(drawingTimeoutRunnable!!, 500) // 1-second delay
    }

    private fun cancelDrawingTimeout() {
        drawingTimeoutRunnable?.let { drawingTimeoutHandler.removeCallbacks(it) }
    }

    private fun handleDrawingTimeout() {
        isInteractive = false // Disable interaction
        moveDrawingsToLeft() // Shift drawings to the left

        editText?.let {
            val selectedLanguage = "en-US" // Replace with dynamic selection if needed
            StrokeManager.recognize(it, selectedLanguage)
        }

        // Re-enable interaction after processing
        postDelayed({ isInteractive = true }, 500)
    }


    fun clearTimeouts() {
        cancelDrawingTimeout()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearTimeouts() // Ensure no leaks when view is detached
    }

    fun moveDrawingsToLeft() {
        val newCanvasBitmap = Bitmap.createBitmap(canvasBitmap.width, canvasBitmap.height, Bitmap.Config.ARGB_8888)
        val newCanvas = Canvas(newCanvasBitmap)
        newCanvas.drawBitmap(canvasBitmap, -600f, 0f, canvasPaint)

        if (currentGroup.isNotEmpty()) {
            val textPosition = editText?.selectionStart ?: 0
            val textLength = editText?.text?.length ?: 0
            strokeGroups.add(StrokeGroup(ArrayList(currentGroup), textPosition, textLength))
            currentGroup.clear()
        }

        canvasBitmap = newCanvasBitmap
        drawCanvas = newCanvas
        invalidate()
    }

    fun undo() {
        if (currentGroup.isNotEmpty()) {
            currentGroup.clear()
            StrokeManager.clear()
            drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            invalidate()
            return
        }

        if (strokeGroups.isEmpty()) {
            return
        }

        val lastGroup = strokeGroups.lastOrNull() ?: return
        val textPosition = lastGroup.textPosition
        val textLength = lastGroup.textLength

        // Update EditText
        editText?.let { editText ->
            val currentText = editText.text.toString()
            if (currentText.length > textLength) {
                val newText = currentText.substring(0, textLength)
                editText.setText(newText)
                editText.setSelection(textPosition)
            }
        }

        // Remove last group
        strokeGroups.removeAt(strokeGroups.size - 1)

        // Redraw all groups with proper positioning
        redrawGroups()
    }

    private fun redrawGroups() {
        // Clear the canvas
        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        // Create a new bitmap for the final result
        val newBitmap = Bitmap.createBitmap(canvasBitmap.width, canvasBitmap.height, Bitmap.Config.ARGB_8888)
        val newCanvas = Canvas(newBitmap)

        // Draw each group with proper shift
        strokeGroups.forEachIndexed { index, group ->
            // Create temporary bitmap for this group
            val tempBitmap = Bitmap.createBitmap(canvasBitmap.width, canvasBitmap.height, Bitmap.Config.ARGB_8888)
            val tempCanvas = Canvas(tempBitmap)

            // Draw all paths in this group
            group.paths.forEach { path ->
                tempCanvas.drawPath(path, currentStrokePaint)
            }

            // Calculate shift based on position from right
            val shift = -600f * (strokeGroups.size - index - 1)

            // Draw the temp bitmap with proper shift
            newCanvas.drawBitmap(tempBitmap, shift, 0f, canvasPaint)

            // Clean up temporary bitmap
            tempBitmap.recycle()
        }

        // Update main canvas
        canvasBitmap = newBitmap
        drawCanvas = newCanvas

        invalidate()
    }

    fun clear() {
        if (::canvasBitmap.isInitialized) {
            strokeHistory.clear()
            currentGroup.clear()
            strokeGroups.clear()
            StrokeManager.clear()
            currentStroke = Path()
            drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            invalidate()
        }
    }

    fun setEditText(editText: EditText) {
        this.editText = editText
        currentTextPosition = editText.selectionStart
    }
}

//

data class PathAndValue(val value: Int, val path: Path)
