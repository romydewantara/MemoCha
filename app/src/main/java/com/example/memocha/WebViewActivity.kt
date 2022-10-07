package com.example.memocha

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.CircularProgressIndicator

class WebViewActivity : AppCompatActivity() {

    private val URL = "https://cookpad.com/id/pengguna/9875548"
    private lateinit var myWebView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        myWebView = findViewById(R.id.webView)
        loadWeb()
    }

    private fun loadWeb() {
        val circularProgress = findViewById<CircularProgressIndicator>(R.id.circularProgress)
        circularProgress.visibility = View.VISIBLE
        myWebView.visibility = View.GONE

        myWebView.loadUrl(URL)
        myWebView.settings.javaScriptEnabled = true
        myWebView.clearHistory()
        myWebView.clearFormData()
        myWebView.clearCache(true)
        android.webkit.CookieManager.getInstance().removeAllCookie()

        myWebView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                circularProgress.visibility = View.GONE
                view?.visibility = View.VISIBLE
            }

            @Deprecated("Deprecated in Java")
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                showError(errorCode, description)
            }

        }
    }
    private fun showError(errorCode: Int, desc: String?) {
        if (errorCode == WebViewClient.ERROR_IO || errorCode == WebViewClient.ERROR_TIMEOUT) return
        Toast.makeText(applicationContext, getString(R.string.webview_error_load, errorCode.toString(), desc), Toast.LENGTH_LONG).show()
        val retry = findViewById<Button>(R.id.buttonRetry)
        retry.visibility = View.VISIBLE
        retry.setOnClickListener {
            loadWeb()
            retry.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        if (myWebView.url != URL) {
            loadWeb()
        } else {
            startActivity(Intent(this@WebViewActivity, DashboardActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }
    }
}