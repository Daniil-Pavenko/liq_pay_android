package com.dansdev.liqpayhelper.model

import com.google.gson.annotations.SerializedName

data class Dcc(
    @SerializedName("amount") val amount: Float,
    @SerializedName("rate") val rate: Float,
    @SerializedName("commission") val commission: Int,
    @SerializedName("currency") val currency: String
)
