package com.dansdev.liqpayhelper.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
enum class LiqPayStatus {
    @SerializedName("success") SUCCESS,
    @SerializedName("error") ERROR,
    @SerializedName("failure") FAILURE,
    @SerializedName("reversed") REVERSED,
    @SerializedName("activated") ACTIVATED,
    @SerializedName("wait_accept") WAIT_ACCEPT,
    @SerializedName("wait_secure") WAIT_SECURE,
    @SerializedName("sender_verify") SENDER_VERIFY,
    @SerializedName("receiver_verify") RECEIVER_VERIFY,
    @SerializedName("otp_verify") OTP_VERIFY,
    @SerializedName("cvv_verify") CVV_VERIFY,
    @SerializedName("3ds_verify") T_3DS_VERIFY
}
