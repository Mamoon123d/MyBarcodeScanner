package com.mybarcodescanner.app.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.base.baselibrary.base.BaseActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mybarcodescanner.app.R
import com.mybarcodescanner.app.databinding.HomePageBinding
import com.mybarcodescanner.app.util.Constant

class HomePage : BaseActivity<HomePageBinding>() {

    override fun initM() {
     setSystemBarColor(R.color.sec_color)
        //set all buttons event
        listener()
        checkCameraPermission()
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

    private fun listener() {
        //qr code button click
        binding.qrBt.setOnClickListener {
            goActivity(ScannerPage(), Bundle().apply {
                putInt(Constant.CodeType.BARCODE_TYPE,Constant.Type.QRCODE.ordinal)
            })
        }
        //Barcode button click
        binding.barBt.setOnClickListener {
            goActivity(ScannerPage(), Bundle().apply {
                putInt(Constant.CodeType.BARCODE_TYPE,Constant.Type.BARCODE.ordinal)
            })
        }
        //AutoBt button click
        binding.autoBt.setOnClickListener {
            goActivity(ScannerPage(), Bundle().apply {
                putInt(Constant.CodeType.BARCODE_TYPE,Constant.Type.BOTH.ordinal)
            })
        }

    }

    override fun setLayoutId(): Int {
        return R.layout.home_page
    }
}