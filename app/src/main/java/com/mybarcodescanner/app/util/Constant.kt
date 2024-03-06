package com.mybarcodescanner.app.util

class Constant {
    enum class Type(value :Int) {
        BARCODE(1),
        QRCODE(2),
        BOTH(3)
    }


    object CodeType {
        private const val Sub = "codeType_"
        const val BARCODE_TYPE = Sub + "barcodeType"
    }

    object Scanner {
        private const val Sub = "scanner_"
        const val RESULT= Sub + "result"
    }

}