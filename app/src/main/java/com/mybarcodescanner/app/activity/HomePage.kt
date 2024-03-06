package com.mybarcodescanner.app.activity

import android.os.Bundle
import com.base.baselibrary.base.BaseActivity
import com.mybarcodescanner.app.R
import com.mybarcodescanner.app.databinding.HomePageBinding
import com.mybarcodescanner.app.util.Constant

class HomePage : BaseActivity<HomePageBinding>() {

    override fun initM() {
     setSystemBarColor(R.color.sec_color)
        //set all buttons event
        listener()
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