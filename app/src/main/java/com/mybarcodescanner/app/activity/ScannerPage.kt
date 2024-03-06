package com.mybarcodescanner.app.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.base.baselibrary.base.BaseActivity
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.finreward.android.utils.require.Extension.Companion.gone
import com.finreward.android.utils.require.Extension.Companion.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.mybarcodescanner.app.R
import com.mybarcodescanner.app.databinding.ScannerPageBinding
import com.mybarcodescanner.app.util.BarCodeAnalyzer
import com.mybarcodescanner.app.util.BarcodeBoxView
import com.mybarcodescanner.app.util.Constant
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class ScannerPage : BaseActivity<ScannerPageBinding>() {
    private var camera: Camera? = null
    private var mCameraId: String?=null
    private var type: Int = 3
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeBoxView: BarcodeBoxView
    override fun setLayoutId(): Int {
        return R.layout.scanner_page
    }

    override fun initM() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        //set Image analyzer
        barcodeBoxView = BarcodeBoxView(mActivity).apply {
            //setRect()
        }
        addContentView(
            barcodeBoxView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        checkCameraPermission()
        type = intent.getIntExtra(Constant.CodeType.BARCODE_TYPE, 3)

        //back listener
        binding.backBt.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        //torch listener
        binding.flashBt.setOnClickListener {
            switchFlash()
        }

        //pick image from gallery
        setUserImage()

        binding.pickImgBt.gone()
        // startScanner()
    }

    private fun setUserImage() {
        val cropImageOptions = CropImageOptions()
        cropImageOptions.apply {
            imageSourceIncludeGallery = true
            imageSourceIncludeCamera = false
            //cropImageOptions.borderCornerOffset = 100f
            borderCornerLength = 50f
            cropShape = CropImageView.CropShape.RECTANGLE
            //maxCropResultHeight = 400
            //maxCropResultWidth = 400
            //  minCropResultHeight = 400
            // minCropResultWidth = 400
            allowRotation = true
            centerMoveEnabled = true
            fixAspectRatio = true
            autoZoomEnabled = true
            multiTouchEnabled = false
            initialCropWindowPaddingRatio = 0.2f
            progressBarColor = getColor(R.color.sec_color)
            toolbarColor = getColor(R.color.first_color)
            toolbarTitleColor = getColor(R.color.text_2)
            toolbarTintColor = getColor(R.color.first_color)
            activityBackgroundColor = getColor(R.color.first_color)
            title = getString(R.string.app_name)
            activityTitle = getString(R.string.app_name)
            activityMenuTextColor = getColor(R.color.sec_color)
            activityMenuIconColor = getColor(R.color.sec_color)
            toolbarBackButtonColor = getColor(R.color.sec_color)
        }

        val cropImageContractOptions = CropImageContractOptions(null, cropImageOptions)
        binding.pickImgBt.setOnClickListener {
            cropImage.launch(cropImageContractOptions)
        }
    }

    var cropImage = registerForActivityResult(
        CropImageContract(),
        ActivityResultCallback { result: CropImageView.CropResult ->
            if (result.isSuccessful) {
                val uriContent = result.uriContent
                val uriFilePath = result.getUriFilePath(mActivity)
               // binding.userImg.setImageURI(uriContent)
                // uploadImage(uriFilePath)
                startCamera(uriContent)
            }
        })


    var isFlashOn = false
    fun switchFlash() {
        if (!isFlashOn) {
            camera?.run {
                this.cameraControl.enableTorch(true)
                binding.flashBt.setImageResource(R.drawable.ic_flash2)
            }
        } else {
            camera?.run {
                this.cameraControl.enableTorch(false)
                binding.flashBt.setImageResource(R.drawable.ic_flash_1)
            }
        }
        isFlashOn=!isFlashOn
    }

    /*private fun startScanner() {
        val scanner = BarcodeScanning.getClient()
        *//*val result = scanner.process(image)
            .addOnSuccessListener { barcodes ->
                // Task completed successfully
                //get Information from Bar Codes
                getInfo(barcodes)
            }
            .addOnFailureListener {
                // Task failed with an exception
                // ...
            }*//*
    }*/

    private fun getInfo(barcodes: Any) {
        /* for (barcode in barcodes) {
             val bounds = barcode.boundingBox
             val corners = barcode.cornerPoints

             val rawValue = barcode.rawValue

             val valueType = barcode.valueType
             // See API reference for complete list of supported types
             when (valueType) {
                 Barcode.TYPE_WIFI -> {
                     val ssid = barcode.wifi!!.ssid
                     val password = barcode.wifi!!.password
                     val type = barcode.wifi!!.encryptionType
                 }

                 Barcode.TYPE_URL -> {
                     val title = barcode.url!!.title
                     val url = barcode.url!!.url
                 }
             }
         }*/
    }

    private fun configure() {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_CODABAR,
                Barcode.FORMAT_AZTEC
            ).enableAllPotentialBarcodes()

            .build()

    }

    fun setZoom(ZoomRatio: Float): Boolean {
        //   if (camera.isClosed()) return false
        //  camera.getCameraControl().setZoomRatio(zoomRatio)
        return true
    }

    private class MyImageAnalyzer : ImageAnalysis.Analyzer {

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                //input image
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                // Pass image to an ML Kit Vision API
                //configure bar code scanner
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_CODABAR,
                        Barcode.FORMAT_AZTEC
                    ).enableAllPotentialBarcodes()
                    .build()

                val scanner = BarcodeScanning.getClient(options)

                scanner.process(image).addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        // Handle received barcodes...
                    }
                }.addOnFailureListener { }


                // ...

            }
            imageProxy.close()

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.run {
            shutdown()
        }
    }

    private fun checkCameraPermission() {
        try {
            val requiredPermissions = arrayOf(Manifest.permission.CAMERA)
            ActivityCompat.requestPermissions(this, requiredPermissions, 0)
        } catch (e: IllegalArgumentException) {
            checkIfCameraPermissionIsGranted()
        }
    }

    private fun checkIfCameraPermissionIsGranted() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission granted: start the preview
            try {

                startCamera()
            }catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(mActivity, "something went wrong", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Permission denied
            MaterialAlertDialogBuilder(this)
                .setTitle("Permission required")
                .setMessage("This application needs to access the camera to process barcodes")
                .setPositiveButton("Ok") { _, _ ->
                    // Keep asking for permission until granted
                    checkCameraPermission()
                }
                .setCancelable(false)
                .create()
                .apply {
                    setCanceledOnTouchOutside(false)
                    show()
                }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkIfCameraPermissionIsGranted()
    }

    private fun startCamera(uriContent: Uri?=null) {
        binding.pickImgBt.visible()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(mActivity)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()


            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }


            val barCodeAnalyzer = BarCodeAnalyzer(
                mActivity,
                barcodeBoxView,
                binding.previewView.width.toFloat(),
                binding.previewView.height.toFloat(),
                type,
                uriContent
            )
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, barCodeAnalyzer)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                 camera= cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )


            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }


}

