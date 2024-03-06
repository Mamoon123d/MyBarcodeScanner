package com.mybarcodescanner.app.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.mybarcodescanner.app.activity.ResultPage


class BarCodeAnalyzer(
    private val context: Context,
    private val barcodeBoxView: BarcodeBoxView,
    private val previewViewWidth: Float,
    private val previewViewHeight: Float,
    private val type: Int
) : ImageAnalysis.Analyzer {

    /**
     * This parameters will handle preview box scaling
     */
    private var scaleX = 1f
    private var scaleY = 1f

    private fun translateX(x: Float) = x * scaleX
    private fun translateY(y: Float) = y * scaleY

    private fun adjustBoundingRect(rect: Rect) = RectF(
        translateX(rect.left.toFloat()),
        translateY(rect.top.toFloat()),
        translateX(rect.right.toFloat()),
        translateY(rect.bottom.toFloat())
    )

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val img = image.image
        if (img != null) {
            // Update scale factors
            scaleX = previewViewWidth / img.height.toFloat()
            scaleY = previewViewHeight / img.width.toFloat()

            val inputImage = InputImage.fromMediaImage(img, image.imageInfo.rotationDegrees)

            // Process image searching for barcodes
            val options = BarcodeScannerOptions.Builder().apply {
                //val barcodeFormats=intArrayOf(Barcode.FORMAT_CODABAR,Barcode.FORMAT_AZTEC)
              /*  val FORMAT_UNKNOWN = -1
                val FORMAT_ALL_FORMATS = 0
              //  val FORMAT_CODE_128 = 1
               // val FORMAT_CODE_39 = 2
              //  val FORMAT_CODE_93 = 4
             //   val FORMAT_CODABAR = 8
                val FORMAT_DATA_MATRIX = 16
             //   val FORMAT_EAN_13 = 32
              //  val FORMAT_EAN_8 = 64
               // val FORMAT_ITF = 128
                val FORMAT_QR_CODE = 256
               // val FORMAT_UPC_A = 512
               // val FORMAT_UPC_E = 1024
              //  val FORMAT_PDF417 = 2048
                val FORMAT_AZTEC = 4096*/

                when(type){
                    Constant.Type.BARCODE.ordinal->{
                        setBarcodeFormats(-1,1,2,4,8,32,64,128,512,1024,2024)
                    }
                    Constant.Type.QRCODE.ordinal->{
                        setBarcodeFormats(Barcode.FORMAT_QR_CODE,Barcode.FORMAT_AZTEC,Barcode.FORMAT_DATA_MATRIX)
                    }
                    Constant.Type.BOTH.ordinal->{
                        setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    }

                }

               // enableAllPotentialBarcodes()
            }.build()

            val scanner = BarcodeScanning.getClient(options)

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        for (barcode in barcodes) {
                            // Handle received barcodes...
                            Toast.makeText(
                                context,
                                "Value: " + barcode.rawValue,
                                Toast.LENGTH_SHORT
                            )
                                .show()

                            // Update bounding rect
                            barcode.boundingBox?.let { rect ->
                                barcodeBoxView.setRect(
                                    adjustBoundingRect(
                                        rect
                                    )
                                )
                            }

                            context.startActivity(Intent(context,ResultPage::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                putExtras(
                                Bundle().apply {
                                    putString(Constant.Scanner.RESULT,barcode.rawValue)
                                }
                            )})

                            playVibrate()
                            (context as Activity).finish()
                            scanner.close()
                            break


                        }
                    } else {
                        // Remove bounding rect
                        barcodeBoxView.setRect(RectF())
                    }
                }
                .addOnFailureListener { }
        }

        image.close()
    }

    private fun playVibrate() {
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
// Vibrate for 500 milliseconds
// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v!!.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            v!!.vibrate(500)
        }
    }
}
