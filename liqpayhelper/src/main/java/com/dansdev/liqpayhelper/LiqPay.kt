package com.dansdev.liqpayhelper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Looper
import androidx.annotation.Keep
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.dansdev.liqpayhelper.api.LiqPayRequest
import com.dansdev.liqpayhelper.constant.ErrorCode
import com.dansdev.liqpayhelper.model.LiqPayReceipt
import com.dansdev.liqpayhelper.util.LiqPayCallback
import com.dansdev.liqpayhelper.util.LiqPayUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import timber.log.Timber
import java.io.IOException
import java.util.*

@Keep
class LiqPay private constructor(
    private val fragmentActivity: FragmentActivity,
    private val checkoutCallback: LiqPayCallback
) {

    private val gson: Gson = GsonBuilder().create()

    companion object {
        const val INTENT_ACTION = "ua.privatbank.paylibliqpay.broadcast"

        const val EXTRAS_KEY = "postData"
        const val DATA_KEY = "data"

        private val gson: Gson = GsonBuilder().create()

        fun api(
            fragmentActivity: FragmentActivity,
            path: String,
            params: Map<String, String>,
            privateKey: String,
            callBack: LiqPayCallback
        ) {
            val base64Data = LiqPayUtil.base64Encode(gson.toJson(params)).orEmpty()
            val signature = LiqPayUtil.createSignature(base64Data, privateKey).orEmpty()
            api(fragmentActivity, path, base64Data, signature, callBack)
        }

        fun api(
            fragmentActivity: FragmentActivity,
            path: String,
            base64Data: String,
            signature: String,
            callBack: LiqPayCallback
        ) {
            if (!LiqPayUtil.checkPermissions(
                    fragmentActivity,
                    arrayOf(
                        "android.permission.INTERNET",
                        "android.permission.ACCESS_NETWORK_STATE"
                    )
                )
            ) {
                callBack.onResponseError(ErrorCode.NEED_PERMISSION)
            } else if (!LiqPayUtil.isOnline(fragmentActivity)) {
                callBack.onResponseError(ErrorCode.NO_CONNECTION)
            } else if (Looper.myLooper() == Looper.getMainLooper()) {
                callBack.onResponseError(ErrorCode.USE_BACKGROUND_THREAD)
            } else {
                val postParams = HashMap<String, String>()
                postParams["data"] = base64Data
                postParams["signature"] = signature
                try {
                    val resp =
                        LiqPayRequest.post(LiqPayUtil.LIQPAY_API_URL_REQUEST + path, postParams)
                    Timber.d("Local LiqPay Receipt: $resp")
                    if (resp != null) {
                        val response = gson.fromJson(resp, LiqPayReceipt::class.java)
                        callBack.onResponseSuccess(response)
                    } else {
                        callBack.onResponseError(ErrorCode.RESPONSE_EMPTY)
                    }
                } catch (error: IOException) {
                    error.printStackTrace()
                    callBack.onResponseError(ErrorCode.IO)
                }
            }
        }

        fun checkout(
            fragmentActivity: FragmentActivity,
            params: HashMap<String, String>,
            privateKey: String,
            callBack: LiqPayCallback
        ) {
            val base64Data = LiqPayUtil.base64Encode(gson.toJson(params))
            val signature = LiqPayUtil.createSignature(base64Data!!, privateKey).orEmpty()
            checkout(fragmentActivity, base64Data, signature, callBack)
        }

        fun checkout(
            fragmentActivity: FragmentActivity,
            base64Data: String,
            signature: String,
            callBack: LiqPayCallback
        ) {
            if (!LiqPayUtil.checkPermissions(
                    fragmentActivity,
                    arrayOf(
                        "android.permission.INTERNET",
                        "android.permission.ACCESS_NETWORK_STATE"
                    )
                )
            ) {
                callBack.onResponseError(ErrorCode.NEED_PERMISSION)
            } else if (!LiqPayUtil.isOnline(fragmentActivity)) {
                callBack.onResponseError(ErrorCode.NO_CONNECTION)
            } else {
                val liqPay = LiqPay(fragmentActivity, callBack)
                fragmentActivity.registerReceiver(liqPay.mReceiver, IntentFilter(INTENT_ACTION))
                liqPay.startCheckoutActivity(base64Data, signature)
            }
        }
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == INTENT_ACTION) {
                val resp = intent.getStringExtra("data")
                if (resp == null) {
                    this@LiqPay.checkoutCallback.onResponseError(ErrorCode.CHECKOUT_CANCELED)
                } else {
                    Timber.d("Local LiqPay Resp: $resp")
                    try {
                        val receipt = gson.fromJson(resp, LiqPayReceipt::class.java)
                        this@LiqPay.checkoutCallback.onResponseSuccess(receipt)
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }
                context.unregisterReceiver(this)
            }
        }
    }

    /**
     * In this function can customize animation for open fragment
     */
    private fun startCheckoutActivity(data: String, signature: String) {
        fragmentActivity.supportFragmentManager.commit {
            add(
                android.R.id.content, LiqPayCheckoutFragment::class.java, bundleOf(
                    EXTRAS_KEY to "$DATA_KEY=$data&signature=$signature&hash_device="
                            + LiqPayUtil.getHashDevice(fragmentActivity)
                            + "&channel=android"
                ), LiqPayCheckoutFragment.TAG
            )
            setReorderingAllowed(true)
            addToBackStack(LiqPayCheckoutFragment.TAG)
        }
    }
}
