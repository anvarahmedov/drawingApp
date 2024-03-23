package com.example.drawingapp


import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.drawingapp.CanvasForBrushSize.Companion.paintBrushWindowDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var imageViewGalleryPhoto: ImageView
    private lateinit var constraintLayoutForSavingPhoto: ConstraintLayout


    private lateinit var purpleBtn: AppCompatButton
    private lateinit var redBtn: AppCompatButton
    private lateinit var greenBtn: AppCompatButton
    private lateinit var blueBtn: AppCompatButton
    private lateinit var orangeBtn: AppCompatButton

    private lateinit var brushSizeBtn: ImageButton
    private lateinit var saveBtn: ImageButton
    private lateinit var galleryBtn: ImageButton
    private lateinit var undoBtn: ImageButton
    private lateinit var paletteBtn: ImageButton

    private var isSeekBarChanged = false



    private val imageContract = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageViewGalleryPhoto.setImageURI(it)
    }


    companion object {
        private const val READ_EXTERNAL_STORAGE_PERM_CODE = 100
        private const val WRITE_EXTERNAL_STORAGE_PERM_CODE = 100
    }



    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)



        setContentView(R.layout.activity_main)



        drawingView = findViewById<DrawingView>(R.id.main_canvas)
        imageViewGalleryPhoto = findViewById(R.id.image_gallery)
        constraintLayoutForSavingPhoto = findViewById(R.id.drawing_view_constraint_layout)

        purpleBtn = findViewById(R.id.purple_btn)
        redBtn = findViewById(R.id.red_btn)
        greenBtn = findViewById(R.id.green_btn)
        blueBtn = findViewById(R.id.blue_btn)
        orangeBtn = findViewById(R.id.orange_btn)




        brushSizeBtn = findViewById(R.id.brush_btn)
        saveBtn = findViewById(R.id.save_btn)
        galleryBtn = findViewById(R.id.gallery_btn)
        undoBtn = findViewById(R.id.undo_btn)
        paletteBtn = findViewById(R.id.color_picker_btn)




        purpleBtn.setOnClickListener {
            drawingView.changeColor(Color.parseColor("#B200B7"))
        }


        redBtn.setOnClickListener {
            drawingView.changeColor(Color.parseColor("#D40808"))
        }

        greenBtn.setOnClickListener {
            drawingView.changeColor(Color.parseColor("#179500"))
        }


        blueBtn.setOnClickListener {
            drawingView.changeColor(Color.parseColor("#031DC0"))
        }


        orangeBtn.setOnClickListener {

            drawingView.changeColor(Color.parseColor("#BD7A06"))
        }



        brushSizeBtn.setOnClickListener {
            CanvasForBrushSize.currentColorWindowDialog = drawingView.getCurrentColor()
            showDialog()
        }

        undoBtn.setOnClickListener {
            drawingView.deletePath(imageViewGalleryPhoto)
        }

        undoBtn.setOnLongClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Clear the screen. Alert")
                .setMessage("Do you really want to clear the screen").setNegativeButton(R.string.dialog_no) {dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(R.string.dialog_yes) { dialog, _ ->
                    drawingView.clear(imageViewGalleryPhoto)
                    try {
                        if (imageViewGalleryPhoto.drawable != null) {
                            imageViewGalleryPhoto.setImageDrawable(null)
                            Toast.makeText(this, "Cleared!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this, "Some error occurred!", Toast.LENGTH_SHORT)
                            .show()
                    }
                    dialog.dismiss()
                }
            if (imageViewGalleryPhoto.drawable != null || drawingView.getPaths().isNotEmpty()) {
                builder.create().show()
            }
            return@setOnLongClickListener true
        }

        galleryBtn.setOnClickListener {
            checkPermission(READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE_PERM_CODE)
        }

        paletteBtn.setOnClickListener {
            showPaletteDialog()
        }

        saveBtn.setOnClickListener {
            checkPermission(WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE_PERM_CODE)
        }
    }


    private fun showDialog() {


        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.frame_layout)

        val sampleText = dialog.findViewById(R.id.seekbar_progress) as TextView

        val seekBar = dialog.findViewById(R.id.seekBar) as SeekBar

        if (isSeekBarChanged) {
            var prefs = getSharedPreferences("mySharedPrefsFilename", Context.MODE_PRIVATE);
            var value = prefs.getInt("seekBarValue", 0)

            seekBar.progress = value
            sampleText.text = value.toString()
            paintBrushWindowDialog.strokeWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics
            )
        }




        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                CanvasForBrushSize.paintBrushWindowDialog.strokeWidth = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, progress.toFloat(), resources.displayMetrics
                )
                sampleText.text = progress.toString()
                drawingView.changeBrushSize(progress.toFloat())

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val prefs: SharedPreferences =
                    getSharedPreferences("mySharedPrefsFilename", MODE_PRIVATE)
                prefs.edit().putInt("seekBarValue", seekBar.progress).apply()

                isSeekBarChanged = true
            }
        })



        dialog.show()


    }

    private fun showPaletteDialog() {
        val dialog = AmbilWarnaDialog(this, Color.GREEN, object : OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {

            }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                drawingView.changeColor(color)
            }
        })
        dialog.show()
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (permission == READ_EXTERNAL_STORAGE || permission == WRITE_EXTERNAL_STORAGE) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
            ) {
                showRationaleDialog()
            } else {
                if (permission == READ_EXTERNAL_STORAGE) {
                    imageContract.launch("image/*")
                } else if (permission == WRITE_EXTERNAL_STORAGE) {
                    CoroutineScope(IO).launch {
                        saveImage()
                    }
                }
            }
        }


    }

        private fun showRationaleDialog() {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Storage permission")
                .setMessage("We need storage permission in order to access the internal storage")
                .setPositiveButton(R.string.dialog_yes) { dialog, _ ->
                    var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    var uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)

                    dialog.dismiss()
                }
            builder.create().show()


        }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(
            view.width, view.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }


    private suspend fun saveImage () {
        var img = getBitmapFromView(constraintLayoutForSavingPhoto)
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            .toString()
        val imgFile = File(root, "myImg$n.jpg")

        try {
                if (imgFile.exists()){
                    imgFile.delete()
                }

                val out = withContext(IO) {
                    FileOutputStream(imgFile)
                }

                img.compress(Bitmap.CompressFormat.JPEG, 90, out)

                withContext(IO) {
                    out.flush()
                }
                withContext(IO) {
                    out.close()
                }
            } catch (e: Exception) {
                e.stackTrace
        }

        withContext(Main) {
            Toast.makeText(applicationContext, "${imgFile.absolutePath} saved", Toast.LENGTH_LONG).show()
        }
        addImageGallery(imgFile)
    }

    private fun addImageGallery(file: File) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DATA, file.absolutePath)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") // or image/png
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }
}



