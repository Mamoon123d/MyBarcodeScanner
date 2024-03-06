package com.mybarcodescanner.app.activity

import com.base.baselibrary.base.BaseActivity
import com.mybarcodescanner.app.R
import com.mybarcodescanner.app.databinding.ResultPageBinding
import com.mybarcodescanner.app.util.Constant

class ResultPage : BaseActivity<ResultPageBinding>() {
    override fun setLayoutId(): Int {
        return R.layout.result_page
    }

    override fun initM() {
        binding.backBt.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        //set value from scanner
        val scannedText=intent.getStringExtra(Constant.Scanner.RESULT)
        binding.rOutput.text= buildString {
            append("Decoded Text : ")
            append("\n")
            append(scannedText)
        }
    }
}