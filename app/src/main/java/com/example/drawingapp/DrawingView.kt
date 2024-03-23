package com.example.drawingapp

import android.content.ContentValues
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import com.example.drawingapp.CanvasForBrushSize.Companion.paintBrushWindowDialog
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream

class DrawingView(context: Context, attrs: AttributeSet): View(context, attrs) {
    //drawing Path
    private lateinit var drawPath: FingerPath

    //defines what to draw
    private lateinit var canvasPaint: Paint

    //defines how to draw
    private lateinit var drawPaint: Paint

    private lateinit var canvas: Canvas
    private lateinit var canvasBitmap: Bitmap

    private var brushSize: Float = 0.toFloat()
    private val paths = mutableListOf<FingerPath>()
    private var brushColor = Color.RED


    companion object{
    }

    init {
        setUpDrawing()
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap)
    }

    // this function will be called when the user is going to touch the screen

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            // this option is called when the user puts his finger on the screen
            MotionEvent.ACTION_DOWN -> {



                    drawPath.color = brushColor
                    drawPath.brushThickness = brushSize

                    drawPath.reset() // Reset the path before setting the initial points
                    drawPath.moveTo(touchX!!, touchY!!)

                }


            // this event will be fired when the user starts to move his finger
            // this event will continuously be called until the user picks his finger up

            MotionEvent.ACTION_MOVE -> {
                drawPath.lineTo(touchX!!, touchY!!)
            }

            // this event will be called when the user picks his finger up from the screen

            MotionEvent.ACTION_UP -> {
                    paths.add(drawPath)
                    drawPath = FingerPath(brushColor, brushSize)
                }

            else -> return false


        }

        invalidate() // this function is called to refresh the layout to display all the changes
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitmap, 0f, 0f, drawPaint)

        for (path in paths) {
            drawPaint.strokeWidth = path.brushThickness
            drawPaint.color = path.color
            canvas.drawPath(path, drawPaint)
        }

        if (!drawPath.isEmpty) {
            drawPaint.strokeWidth = drawPath.brushThickness
            drawPaint.color = drawPath.color
            canvas.drawPath(drawPath, drawPaint)  // drawing path on canvas
        }
    }

    private fun setUpDrawing() {
        drawPaint = Paint()
        drawPath = FingerPath(brushColor, brushSize)

        drawPaint.color = brushColor
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND
        drawPaint.strokeCap = Paint.Cap.ROUND

        canvasPaint = Paint(Paint.DITHER_FLAG)
        brushSize = paintBrushWindowDialog.strokeWidth
        drawPaint.strokeWidth = brushSize
    }

    fun changeBrushSize(newBrushSize: Float) {
        brushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, newBrushSize, resources.displayMetrics
        )

        drawPaint.strokeWidth = brushSize
    }

    fun changeColor(color: Int){
        brushColor = color
    }

    fun deletePath(imageView: ImageView){
        if (paths.size > 0) {
            if (paths.size == 1 && imageView.drawable == null) {
                Toast.makeText(context, "Cleared!", Toast.LENGTH_SHORT).show()
            }
            paths[paths.size - 1].reset()
            paths.removeAt(paths.size - 1)
            canvas.drawBitmap(canvasBitmap, 0f, 0f, drawPaint)
            invalidate()
        } else if (imageView.drawable != null) {
            imageView.setImageDrawable(null)
            Toast.makeText(context, "Cleared!", Toast.LENGTH_SHORT).show()
        }
    }

    fun clear(imageView: ImageView) {
        if (imageView.drawable == null && paths.size > 0) {
                paths.clear()
                invalidate()
                Toast.makeText(context, "Cleared!", Toast.LENGTH_SHORT).show()
        } else {
            paths.clear()
            invalidate()
        }

    }

    fun getCurrentColor(): Int {
        return brushColor
    }

    internal fun getPaths (): MutableList<FingerPath> {
        return paths
    }



    internal inner class FingerPath(var color: Int, var brushThickness: Float) : Path()


}