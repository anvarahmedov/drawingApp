package com.example.drawingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.example.drawingapp.CanvasForBrushSize.Companion.paintBrushWindowDialog
import com.example.drawingapp.CanvasForBrushSize.Companion.pathWindowDialog

class CustomLayout : AppCompatActivity() {

    private lateinit var seekBar: SeekBar
    private lateinit var text_view_seekbar_progress: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.frame_layout)

        seekBar = findViewById(R.id.seekBar)
        seekBar.progress = 0
        seekBar.max = 100

        text_view_seekbar_progress = findViewById(R.id.seekbar_progress)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                paintBrushWindowDialog.strokeWidth = progress.toFloat()
                text_view_seekbar_progress.text = progress.toString()

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // you can probably leave this empty
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // you can probably leave this empty
            }
        })
    }
}