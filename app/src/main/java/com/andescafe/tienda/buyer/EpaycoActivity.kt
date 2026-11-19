package com.andescafe.tienda.buyer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.andescafe.tienda.R
import com.andescafe.tienda.data.Config

/**
 * Pasarela de pagos ePayco (Checkout estándar) dentro de un WebView.
 * 1) Carga el botón de checkout de ePayco con el monto y los datos del pedido.
 * 2) ePayco redirige a la URL de respuesta con ?ref_payco=...; la app la intercepta.
 * 3) Se consulta la transacción en el endpoint de validación de ePayco y se devuelve el resultado.
 */
class EpaycoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AMOUNT = "amount"; const val EXTRA_DESC = "desc"; const val EXTRA_NAME = "name"
        const val EXTRA_EMAIL = "email"; const val EXTRA_INVOICE = "invoice"
        const val EXTRA_CODE = "code"; const val EXTRA_REF = "ref"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_epayco)
        val web = findViewById<WebView>(R.id.webView)
        val bar = findViewById<ProgressBar>(R.id.progress)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.javaScriptCanOpenWindowsAutomatically = true
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val ref = request.url.getQueryParameter("ref_payco")
                if (ref != null) { validate(ref, bar); return true }   // respuesta de ePayco
                return false
            }
            override fun onPageFinished(view: WebView, url: String) { bar.visibility = View.GONE }
        }
        web.loadDataWithBaseURL("https://checkout.epayco.co/", buildHtml(), "text/html", "UTF-8", null)
    }

    /** Página mínima con el script oficial de checkout de ePayco. */
    private fun buildHtml(): String {
        val amount = intent.getLongExtra(EXTRA_AMOUNT, 0L)
        val test = if (Config.EPAYCO_TEST_MODE) "true" else "false"
        return """
            <html><head><meta name="viewport" content="width=device-width, initial-scale=1"/></head>
            <body style="font-family:sans-serif;text-align:center;padding:32px;background:#FFF8F0;color:#3E2723">
              <h2>AndesCafé</h2>
              <p>Total a pagar: <b>COP $amount</b></p>
              <form>
                <script src="https://checkout.epayco.co/checkout.js" class="epayco-button"
                  data-epayco-key="${Config.EPAYCO_PUBLIC_KEY}"
                  data-epayco-amount="$amount"
                  data-epayco-tax="0" data-epayco-tax-base="$amount"
                  data-epayco-name="Pedido AndesCafé"
                  data-epayco-description="${intent.getStringExtra(EXTRA_DESC) ?: ""}"
                  data-epayco-invoice="${intent.getStringExtra(EXTRA_INVOICE) ?: ""}"
                  data-epayco-currency="cop" data-epayco-country="co" data-epayco-lang="es"
                  data-epayco-test="$test" data-epayco-external="false"
                  data-epayco-name-billing="${intent.getStringExtra(EXTRA_NAME) ?: ""}"
                  data-epayco-email-billing="${intent.getStringExtra(EXTRA_EMAIL) ?: ""}"
                  data-epayco-response="${Config.EPAYCO_RESPONSE_URL}"
                  data-epayco-button="https://multimedia.epayco.co/dashboard/btns/btn4.png">
                </script>
              </form>
            </body></html>
        """.trimIndent()
    }

    /** Consulta la transacción en ePayco y devuelve (código, referencia) a quien abrió esta pantalla. */
    private fun validate(refPayco: String, bar: ProgressBar) {
        bar.visibility = View.VISIBLE
        Thread {
            var code = 4
            var ref = refPayco
            try {
                val conn = URL("https://secure.epayco.co/validation/v1/reference/$refPayco").openConnection() as HttpURLConnection
                conn.connectTimeout = 15000; conn.readTimeout = 15000
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val data = JSONObject(body).optJSONObject("data")
                if (data != null) {
                    code = data.optString("x_cod_response").toIntOrNull() ?: 4
                    ref = data.optString("x_transaction_id").ifEmpty { refPayco }
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "No se pudo validar el pago: ${e.localizedMessage}", Toast.LENGTH_LONG).show() }
            }
            runOnUiThread {
                setResult(RESULT_OK, Intent().putExtra(EXTRA_CODE, code).putExtra(EXTRA_REF, ref))
                finish()
            }
        }.start()
    }
}
