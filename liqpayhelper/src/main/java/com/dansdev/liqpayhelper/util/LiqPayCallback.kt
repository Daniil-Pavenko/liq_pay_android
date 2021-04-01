package com.dansdev.liqpayhelper.util

import com.dansdev.liqpayhelper.constant.ErrorCode
import com.dansdev.liqpayhelper.model.LiqPayReceipt

interface LiqPayCallback {
    fun onResponseSuccess(receipt: LiqPayReceipt?)

    fun onResponseError(error: ErrorCode?)
}
