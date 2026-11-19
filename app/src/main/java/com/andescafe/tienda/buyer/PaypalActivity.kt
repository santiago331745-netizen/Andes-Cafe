package com.andescafe.tienda.buyer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import com.andescafe.tienda.R
import com.andescafe.tienda.data.Config

/**
 * Pasarela de pagos PayPal (modo Sandbox) con la API REST v2 de Orders, sin servidor propio:
 * 1) Obtiene un token OAuth con el Client ID y Secret de la app.
 * 2) Crea una orden (monto convertido de COP a USD) y abre la URL de aprobación en un WebView.
 * 3) Al volver a la URL de retorno, captura el pago y devuelve el resultado (código 1 = aprobado).
 * Nota: el Secret dentro de la app es aceptable solo en Sandbox; en producción se debe usar un backend.
 */
class PaypalActivity : AppCompatActivity() {

    private lateinit var bar: ProgressBar
    private var token = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_epayco)      // mismo layout: WebView + ProgressBar
        val web = findViewById<WebView>(R.id.webView)
        bar = findViewById(R.id.progress)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                return when {
                    url.startsWith(Config.PAYPAL_RETURN_URL) -> {
                        capture(request.url.getQueryParameter("token") ?: ""); true
                    }
                    url.startsWith(Config.PAYPAL_CANCEL_URL) -> { finishWith(2, ""); true }
                    else -> false
                }
            }
            override fun onPageFinished(view: WebView, url: String) { bar.visibility = View.GONE }
        }

        // Pasos 1 y 2 en un hilo secundario (red)
        Thread {
            try {
                token = fetchToken()
                val link = createOrder()
                runOnUiThread { web.loadUrl(link) }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "No se pudo iniciar PayPal: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    finishWith(4, "")
                }
            }
        }.start()
    }

    /** Llamada HTTP simple. Devuelve el cuerpo (también en errores 4xx para poder leer el motivo). */
    private fun http(method: String, path: String, headers: Map<String, String>, body: String?): Pair<Int, String> {
        val conn = URL(Config.PAYPAL_API + path).openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.connectTimeout = 20000; conn.readTimeout = 20000
        headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
        if (body != null) { conn.doOutput = true; conn.outputStream.use { it.write(body.toByteArray()) } }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        return code to (stream?.bufferedReader()?.use { it.readText() } ?: "")
    }

    /** Paso 1: token OAuth2 (client_credentials). */
    private fun fetchToken(): String {
        val basic = Base64.encodeToString("${Config.PAYPAL_CLIENT_ID}:${Config.PAYPAL_SECRET}".toByteArray(), Base64.NO_WRAP)
        val (code, body) = http("POST", "/v1/oauth2/token",
            mapOf("Authorization" to "Basic $basic", "Content-Type" to "application/x-www-form-urlencoded"),
            "grant_type=client_credentials")
        if (code != 200) throw Exception("credenciales inválidas ($code)")
        return JSONObject(body).getString("access_token")
    }

    /** Paso 2: crea la orden en USD y devuelve el enlace donde el comprador aprueba el pago. */
    private fun createOrder(): String {
        val cop = intent.getLongExtra(EpaycoActivity.EXTRA_AMOUNT, 0L)
        val usd = String.format(Locale.US, "%.2f", maxOf(cop / Config.COP_PER_USD, 0.01))
        val json = JSONObject()
            .put("intent", "CAPTURE")
            .put("purchase_units", JSONArray().put(JSONObject()
                .put("description", intent.getStringExtra(EpaycoActivity.EXTRA_DESC) ?: "Pedido AndesCafé")
                .put("amount", JSONObject().put("currency_code", "USD").put("value", usd))))
            .put("payment_source", JSONObject().put("paypal", JSONObject().put("experience_context", JSONObject()
                .put("return_url", Config.PAYPAL_RETURN_URL)
                .put("cancel_url", Config.PAYPAL_CANCEL_URL)
                .put("user_action", "PAY_NOW"))))
        val (code, body) = http("POST", "/v2/checkout/orders",
            mapOf("Authorization" to "Bearer $token", "Content-Type" to "application/json"), json.toString())
        if (code !in 200..299) throw Exception("no se pudo crear la orden ($code)")
        val links = JSONObject(body).getJSONArray("links")
        for (i in 0 until links.length()) {
            val l = links.getJSONObject(i)
            if (l.getString("rel") == "payer-action" || l.getString("rel") == "approve") return l.getString("href")
        }
        throw Exception("PayPal no devolvió el enlace de aprobación")
    }

    /** Paso 3: captura el pago aprobado y devuelve el resultado a la pantalla de pago. */
    private fun capture(orderId: String) {
        bar.visibility = View.VISIBLE
        Thread {
            var code = 4
            try {
                val (http, body) = http("POST", "/v2/checkout/orders/$orderId/capture",
                    mapOf("Authorization" to "Bearer $token", "Content-Type" to "application/json"), "{}")
                code = when {
                    http in 200..299 && JSONObject(body).optString("status") == "COMPLETED" -> 1
                    body.contains("INSTRUMENT_DECLINED") -> 2
                    else -> 4
                }
            } catch (e: Exception) { /* queda en 4 (fallida) */ }
            runOnUiThread { finishWith(code, orderId) }
        }.start()
    }

    private fun finishWith(code: Int, ref: String) {
        setResult(RESULT_OK, Intent().putExtra(EpaycoActivity.EXTRA_CODE, code).putExtra(EpaycoActivity.EXTRA_REF, ref))
        finish()
    }
}
