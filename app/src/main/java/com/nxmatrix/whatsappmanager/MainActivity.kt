package com.nxmatrix.whatsappmanager

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("wmm_android", Context.MODE_PRIVATE) }
    private var webView: WebView? = null
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var backendUrl: String = ""

    private val ink = Color.rgb(22, 38, 32)
    private val muted = Color.rgb(104, 119, 112)
    private val green = Color.rgb(21, 128, 82)
    private val page = Color.rgb(246, 249, 247)
    private val border = Color.rgb(224, 233, 227)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backendUrl = prefs.getString("backend_url", "") ?: ""
        if (backendUrl.isNotBlank()) showDashboard(backendUrl) else showSetup()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radius: Int = 16, stroke: Int? = null): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != null) setStroke(dp(1), stroke)
        }

    private fun text(value: String, size: Float, color: Int, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        }

    private fun button(label: String, primary: Boolean = true, action: () -> Unit): TextView =
        TextView(this).apply {
            text = label
            textSize = 15f
            gravity = Gravity.CENTER
            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(if (primary) Color.WHITE else ink)
            background = rounded(if (primary) green else Color.WHITE, 14, if (primary) null else border)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

    private fun showSetup() {
        webView?.destroy()
        webView = null

        val scroll = ScrollView(this).apply { setBackgroundColor(page) }
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(34), dp(22), dp(24))
        }
        scroll.addView(column)

        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val mark = TextView(this).apply {
            text = "W"
            textSize = 22f
            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = rounded(green, 15)
        }
        brand.addView(mark, LinearLayout.LayoutParams(dp(48), dp(48)))
        val brandText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
            addView(text("NX MATRIX", 11f, green, true))
            addView(text("Message Manager", 17f, ink, true))
        }
        brand.addView(brandText)
        column.addView(brand)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.WHITE, 22, border)
            setPadding(dp(20), dp(22), dp(20), dp(20))
        }
        val tag = text("ANDROID COMPANION", 11f, green, true)
        hero.addView(tag)
        val title = text("Your messaging workspace, wherever you work.", 27f, ink, true)
        title.setPadding(0, dp(12), 0, dp(10))
        hero.addView(title)
        hero.addView(text(
            "Connect this app to your secure, hosted Message Manager. Your contacts, groups, campaigns and settings stay synchronized with that server.",
            14f, muted
        ))
        val featureLine = text("CONTACTS     GROUPS     CAMPAIGNS     REPORTS", 10f, green, true)
        featureLine.setPadding(0, dp(20), 0, 0)
        hero.addView(featureLine)
        val heroParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        heroParams.topMargin = dp(28)
        column.addView(hero, heroParams)

        val sectionTitle = text("Connect your workspace", 19f, ink, true)
        sectionTitle.setPadding(2, dp(26), 0, dp(8))
        column.addView(sectionTitle)
        column.addView(text("Enter the HTTPS address where your Node.js backend is hosted.", 13f, muted))

        val input = EditText(this).apply {
            hint = "https://messages.example.com"
            singleLine = true
            textSize = 15f
            setTextColor(ink)
            setHintTextColor(Color.rgb(150, 163, 155))
            setPadding(dp(15), dp(14), dp(15), dp(14))
            background = rounded(Color.WHITE, 14, border)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_URI
            setText(backendUrl)
        }
        val inputParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        inputParams.topMargin = dp(12)
        column.addView(input, inputParams)

        val connect = button("Connect to workspace") {
            val value = input.text.toString().trim().trimEnd('/')
            if (!isValidBackend(value)) {
                Toast.makeText(this, "Enter a valid HTTPS backend URL.", Toast.LENGTH_LONG).show()
            } else {
                backendUrl = value
                prefs.edit().putString("backend_url", value).apply()
                hideKeyboard(input)
                showDashboard(value)
            }
        }
        val connectParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        connectParams.topMargin = dp(14)
        column.addView(connect, connectParams)

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.rgb(235, 244, 238), 14)
            setPadding(dp(15), dp(14), dp(15), dp(14))
        }
        info.addView(text("How the Android app works", 14f, ink, true))
        val infoBody = text(
            "The Android app is the mobile dashboard. The WhatsApp QR session and message processing run on your persistent Node.js server. Keep the server's data/session folder on persistent storage.",
            12f, muted
        )
        infoBody.setPadding(0, dp(7), 0, 0)
        info.addView(infoBody)
        val infoParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        infoParams.topMargin = dp(18)
        column.addView(info, infoParams)

        column.addView(text("Use a second device to scan the WhatsApp Linked Devices QR if the QR is displayed on this phone.", 12f, muted).apply {
            setPadding(dp(2), dp(16), dp(2), 0)
        })
        setContentView(scroll)
    }

    private fun isValidBackend(value: String): Boolean {
        return try {
            val uri = Uri.parse(value)
            uri.scheme == "https" && !uri.host.isNullOrBlank() &&
                uri.userInfo == null && uri.port in -1..65535
        } catch (_: Exception) { false }
    }

    private fun showDashboard(url: String) {
        backendUrl = url
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(8), dp(12), dp(8))
            setBackgroundColor(Color.WHITE)
        }
        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        brand.addView(text("NX MATRIX", 10f, green, true))
        brand.addView(text("Message Manager", 16f, ink, true))
        top.addView(brand, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val settings = button("Settings", false) { showSetup() }
        top.addView(settings)
        root.addView(top)
        val line = View(this).apply { setBackgroundColor(border) }
        root.addView(line, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)))

        val wv = WebView(this)
        webView = wv
        wv.setBackgroundColor(Color.WHITE)
        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT
            allowFileAccess = false
            allowContentAccess = true
            javaScriptCanOpenWindowsAutomatically = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(wv, false)

        wv.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                this@MainActivity.filePathCallback?.onReceiveValue(null)
                this@MainActivity.filePathCallback = filePathCallback
                return try {
                    val intent = fileChooserParams.createIntent()
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST)
                    true
                } catch (_: ActivityNotFoundException) {
                    this@MainActivity.filePathCallback = null
                    Toast.makeText(this@MainActivity, "No file picker is available.", Toast.LENGTH_LONG).show()
                    false
                }
            }
        }
        wv.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val target = request.url
                val base = Uri.parse(backendUrl)
                val sameOrigin = target.scheme == "https" &&
                    target.host.equals(base.host, ignoreCase = true) &&
                    target.port == base.port
                if (sameOrigin) return false

                // External links open in the Android browser; do not navigate the app WebView away.
                return try {
                    startActivity(Intent(Intent.ACTION_VIEW, target))
                    true
                } catch (_: Exception) { true }
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: android.webkit.WebResourceError
            ) {
                if (request.isForMainFrame) {
                    Toast.makeText(this@MainActivity, "Cannot reach your workspace. Check the server and connection.", Toast.LENGTH_LONG).show()
                }
            }
        }

        root.addView(wv, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(root)
        wv.loadUrl(url)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_REQUEST) {
            val result = if (resultCode == RESULT_OK && data?.data != null) {
                arrayOf(data.data!!)
            } else null
            filePathCallback?.onReceiveValue(result)
            filePathCallback = null
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @Deprecated("Deprecated in Android API 33; retained for compatibility with older devices.")
    override fun onBackPressed() {
        val current = webView
        if (current != null && current.canGoBack()) current.goBack()
        else if (current != null) showSetup()
        else super.onBackPressed()
    }

    override fun onDestroy() {
        filePathCallback?.onReceiveValue(null)
        webView?.apply {
            stopLoading()
            webChromeClient = null
            webViewClient = WebViewClient()
            destroy()
        }
        webView = null
        super.onDestroy()
    }

    companion object {
        private const val FILE_CHOOSER_REQUEST = 4102
    }
}
