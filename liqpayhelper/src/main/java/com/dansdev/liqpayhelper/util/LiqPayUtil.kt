package com.dansdev.liqpayhelper.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.provider.Settings.Secure
import android.util.Base64
import android.util.Base64.DEFAULT
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.dansdev.liqpayhelper.LiqPay
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.net.URI
import java.net.URISyntaxException
import java.net.URLDecoder
import java.security.MessageDigest
import java.util.*
import kotlin.collections.HashMap

object LiqPayUtil {

    private const val PREFS_NAME = "paylibliqpay"
    private const val PREFS_DEVICE_KEY = "ua.privatbank.paylibliqpay.hash_device"

    const val LIQPAY_API_URL_CHECKOUT = "https://www.liqpay.ua/api/3/checkout"
    const val LIQPAY_API_URL_REQUEST = "https://www.liqpay.ua/api/request/"

    fun sha1(param: String): ByteArray? {
        return try {
            val shaCrypt = MessageDigest.getInstance("SHA-1")
            shaCrypt.reset()
            shaCrypt.update(param.toByteArray(charset("UTF-8")))
            shaCrypt.digest()
        } catch (var2: Exception) {
            throw RuntimeException("Can't calc SHA-1 hash", var2)
        }
    }

    fun base64Encode(bytes: ByteArray?): String? = Base64.encodeToString(bytes, 2)

    fun base64Encode(data: String): String? = base64Encode(data.toByteArray())

    fun generateData(params: Map<String, String>, privateKey: String): Map<String, String> {
        val apiData = HashMap<String, String>()
        val data = base64Encode(JSONObject(params).toString()).orEmpty()
        apiData["data"] = data
        apiData["signature"] = LiqPayUtil.createSignature(data, privateKey).orEmpty()
        return apiData
    }

    fun strToSign(str: String?): String? = str?.let { base64Encode(sha1(it)) }

    fun createSignature(base64EncodedData: String, privateKey: String): String? =
        strToSign(privateKey + base64EncodedData + privateKey)

    fun isOnline(context: Context): Boolean {
        return try {
            val conMgr = ContextCompat.getSystemService(context, ConnectivityManager::class.java)
                ?: return false
            val i = conMgr.activeNetworkInfo
            i?.isConnected ?: false
        } catch (var3: java.lang.Exception) {
            var3.printStackTrace()
            false
        }
    }

    @SuppressLint("HardwareIds")
    fun getHashDevice(context: Context): String {
        val preferences = context.getSharedPreferences(PREFS_NAME, 0)
        var hashDevice = preferences.getString(PREFS_DEVICE_KEY, null)
        if (hashDevice == null) {
            var androidId = Secure.getString(context.contentResolver, "android_id")
            if (androidId == null) androidId = ""
            val deviceUuid = UUID(
                androidId.hashCode().toLong(), System.currentTimeMillis().hashCode().toLong() shl 32
            )
            hashDevice = deviceUuid.toString()
            preferences.edit { putString(PREFS_DEVICE_KEY, hashDevice) }
        }
        return hashDevice
    }

    fun checkPermissions(contexts: Context, permissions: Array<String>): Boolean {
        val permissionsCount = permissions.size
        for (index in 0 until permissionsCount) {
            val permission = permissions[index]
            val res = contexts.checkCallingOrSelfPermission(permission)
            if (res != 0) return false
        }
        return true
    }

    fun addAll(object1: JSONObject, object2: JSONObject): JSONObject? {
        val iter: Iterator<*> = object2.keys()
        while (iter.hasNext()) {
            val key = iter.next() as String
            try {
                object1.put(key, object2[key])
            } catch (var5: JSONException) {
                var5.printStackTrace()
            }
        }
        return object1
    }

    fun splitQuery(url: URI): Map<String, MutableList<String?>>? {
        val queryPairs: MutableMap<String, MutableList<String?>> = LinkedHashMap()
        val pairs: List<String> = url.query.split("&")
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            val key = if (idx > 0) URLDecoder.decode(pair.substring(0, idx), "UTF-8") else pair
            if (!queryPairs.containsKey(key)) {
                queryPairs[key] = LinkedList()
            }
            val value: String? = if (idx > 0 && pair.length > idx + 1) URLDecoder.decode(
                pair.substring(idx + 1),
                "UTF-8"
            ) else null
            queryPairs[key]!!.add(value)
        }
        return queryPairs
    }


    @Throws(URISyntaxException::class, JSONException::class)
    fun parseUrl(url: String?): JSONObject {
        Timber.i("Data for parse: $url")
        val params = Uri.parse(url).getQueryParameter(LiqPay.DATA_KEY)
        val data = JSONObject()
        LiqPayUtil.addAll(data, JSONObject(
            Base64.decode(params.orEmpty().toByteArray(), DEFAULT).decodeToString()
        ))
        return data
    }
}
