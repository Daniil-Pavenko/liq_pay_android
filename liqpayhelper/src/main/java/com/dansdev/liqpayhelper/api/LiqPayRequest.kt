package com.dansdev.liqpayhelper.api

import timber.log.Timber
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object LiqPayRequest {

    @Throws(IOException::class)
    fun post(url: String, list: Map<String, String>): String? {
        var postData = ""
        var entry: Map.Entry<*, *>
        val interator: Iterator<*> = list.entries.iterator()
        while (interator.hasNext()) {
            entry = interator.next() as Map.Entry<*, *>
            postData = postData + entry.key as String + "=" + URLEncoder.encode(
                entry.value.toString(),
                "UTF-8"
            ) + "&"
        }
        val obj = URL(url)
        var `in`: BufferedReader? = null
        var conn: HttpURLConnection? = null
        return try {
            Timber.d("==== req:$url   data: $postData")
            conn = obj.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            val wr = DataOutputStream(conn.outputStream)
            wr.writeBytes(postData)
            wr.flush()
            wr.close()
            `in` = BufferedReader(InputStreamReader(conn.inputStream))
            val response = StringBuilder()
            var inputLine: String?
            while (`in`.readLine().also { inputLine = it } != null) {
                response.append(inputLine)
            }
            Timber.d("==== resp:$response")
            response.toString()
        } finally {
            `in`?.close()
            conn?.disconnect()
        }
    }
}
