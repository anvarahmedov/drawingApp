package com.example.drawingapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup

class CanvasForBrushSize: View {

    var params: ViewGroup.LayoutParams? = null

    companion object {
        var paintBrushWindowDialog = Paint()
        var pathWindowDialog = Path()
        var currentColorWindowDialog = Color.BLACK
    }

    constructor(context: Context) : this(context, null) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        paintBrushWindowDialog.isAntiAlias = true
        paintBrushWindowDialog.style = Paint.Style.STROKE
        paintBrushWindowDialog.strokeJoin = Paint.Join.ROUND
        paintBrushWindowDialog.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 0f, resources.displayMetrics
        )
        pathWindowDialog.reset()
        pathWindowDialog.moveTo(50f, 30f)
        pathWindowDialog.lineTo(850f, 30f)

        params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDraw(canvas: Canvas) {
            paintBrushWindowDialog.color = currentColorWindowDialog
            canvas.drawPath(pathWindowDialog, paintBrushWindowDialog)
            invalidate()
        }
    }

