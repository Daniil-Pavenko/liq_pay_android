package com.dansdev.liqpayhelper

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.dansdev.liqpayhelper.LiqPay.Companion.INTENT_ACTION
import com.dansdev.liqpayhelper.util.LiqPayUtil
import timber.log.Timber

class LiqPayCheckoutFragment : Fragment() {

    companion object {
        const val TAG = "LiqPayCheckoutFragment"
    }

    private var mWebView: WebView? = null
    private var progressBar: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_liq_pay_checkout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        mWebView = view.findViewById(R.id.mainWebView)
        progressBar = view.findViewById(R.id.progressBar)

        progressBar?.isVisible = true

        setupIntent()
        mWebView?.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {

                override fun onPageFinished(view: WebView, url: String) {
                    Timber.d("==== onPageFinished:$url")
                    progressBar?.isVisible = false
                    super.onPageFinished(view, url)
                }

                override fun onLoadResource(view: WebView, url: String) {
                    super.onLoadResource(view, url)
                    if (url.contains("checkout/info") && VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        progressBar?.isVisible = false
                    }
                    Timber.d(url)
                    val startUrl = "/api/mob/webview"
                    if (url.contains(startUrl)) {
                        Timber.d("==== checkout resp:$url")
                        try {
                            val data = LiqPayUtil.parseUrl(url)
                            val intent = Intent(INTENT_ACTION)
                            intent.setPackage(activity?.packageName)
                            intent.putExtra(LiqPay.DATA_KEY, data.toString())
                            activity?.sendBroadcast(intent)
                        } catch (var6: java.lang.Exception) {
                            var6.printStackTrace()
                            activity?.sendBroadcast(Intent(INTENT_ACTION))
                        }
                        finish(withCallback = false)
                    }
                }
            }
        }
        registerBackPressed()
    }

    private fun registerBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            finish()
            remove()
        }
    }

    private fun finish(withCallback: Boolean = true) {
        activity?.supportFragmentManager?.commit {
            remove(this@LiqPayCheckoutFragment)
        } ?: Timber.e("Activity or fragmentManager is NULL")
        if (withCallback) activity?.sendBroadcast(Intent(INTENT_ACTION))
    }

    private fun setupIntent() {
        val postData = arguments?.getString(LiqPay.EXTRAS_KEY)
        Timber.d("PARAMS: $postData")
        mWebView?.postUrl(
            LiqPayUtil.LIQPAY_API_URL_CHECKOUT,
            postData.orEmpty().encodeToByteArray()
        )
    }

    override fun onDestroy() {
        this.mWebView?.destroy()
        activity?.sendBroadcast(Intent(INTENT_ACTION))
        super.onDestroy()
    }
}
