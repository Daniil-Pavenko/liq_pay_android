package com.dansdev.liqpaykotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dansdev.liqpayhelper.LiqPay
import com.dansdev.liqpayhelper.constant.ErrorCode
import com.dansdev.liqpayhelper.model.LiqPayReceipt
import com.dansdev.liqpayhelper.model.LiqPayStatus
import com.dansdev.liqpayhelper.util.LiqPayCallback
import timber.log.Timber
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        executePayment()
    }

    private fun executePayment() {
        val params = HashMap<String, String>()
        params["version"] = "3"
        params["action"] = "pay"
        params["public_key"] = "<public_liq_pay_key>"
        params["phone"] = "" //phone of customer
        params["amount"] = String.format(Locale.US, "%.2f", 1.3f)
        params["currency"] = "UAH"
        params["description"] = "description"
        params["order_id"] = "<order_id>"
        params["language"] = "<ru|uk|en>"
        params["sandbox"] = "0" //0 - disabled| 1 - enabled
        params["customer"] = "customer_id"
        params["server_url"] = "server_callback_url"
        Timber.i("Pay with params: $params")

        try {
            LiqPay.checkout(
                this,
                params,
                "<private_key>",
                object : LiqPayCallback {
                    override fun onResponseSuccess(receipt: LiqPayReceipt?) {
                        Timber.d(receipt.toString())

//                        callback.onProgressStateUpdate(ProgressState.Hide)
                        when (receipt?.status) {
                            LiqPayStatus.SUCCESS -> {
//                                callback.onSuccess(receipt)
                            }
                            LiqPayStatus.FAILURE -> {
//                                callback.onFail(Exception(receipt.description))
//                                callback.onComplete(AlertMessage.ErrorMessage(R.string.error_card_is_declined))
                            }
                            LiqPayStatus.ERROR -> {
//                                callback.onFail(Exception(receipt.description))
//                                callback.onComplete(AlertMessage.ErrorMessage(R.string.error_card_is_declined))
                            }
                            LiqPayStatus.WAIT_ACCEPT -> {
                                Timber.w(receipt.description)
//                                callback.onSuccess(receipt)
                            }
                            else -> "something went wrong"//callback.onComplete(AlertMessage.ErrorMessage(R.string.error_something_wrong))
                        }
                    }

                    override fun onResponseError(errorCode: ErrorCode?) {
                        Timber.e("$errorCode")
//                        when (errorCode) {
//                            ErrorCode.CHECKOUT_CANCELED -> callback.onComplete(
//                                AlertMessage.ErrorMessage(
//                                    R.string.error_card_is_declined
//                                )
//                            )
//                            ErrorCode.NEED_PERMISSION -> callback.onComplete(
//                                AlertMessage.ErrorMessage(
//                                    R.string.message_denied_phone_permission
//                                )
//                            )
//                            else -> callback.onComplete(AlertMessage.ErrorMessage(R.string.error_something_wrong))
//                        }
//                        callback.onProgressStateUpdate(ProgressState.Hide)
                    }
                }
            )
        } catch (e: Exception) {
            Timber.e(e)
//            callback.onFail(e)
        }
    }
}