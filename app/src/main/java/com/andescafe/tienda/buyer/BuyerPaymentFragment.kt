package com.andescafe.tienda.buyer

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.google.firebase.firestore.*
import com.andescafe.tienda.R
import com.andescafe.tienda.common.*
import com.andescafe.tienda.data.*

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale
import java.util.UUID

/**
 * Checkout: dirección de entrega (con geolocalización), pago con PayPal Sandbox o ePayco y creación de la orden.
 * La orden solo se registra en Firestore cuando ePayco confirma la transacción.
 */
class BuyerPaymentFragment : Fragment(R.layout.fragment_buyer_payment) {

    private var lat = 0.0
    private var lng = 0.0
    private var items = listOf<CartItem>()
    private var total = 0L
    private var profile: UserProfile? = null

    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) captureLocation() else toast("Sin permiso de ubicación no podemos usar el GPS")
    }
    private val epaycoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        setPayEnabled(true)
        if (res.resultCode == Activity.RESULT_OK) {
            val d = res.data
            onPaymentResult(d?.getIntExtra(EpaycoActivity.EXTRA_CODE, 4) ?: 4, d?.getStringExtra(EpaycoActivity.EXTRA_REF) ?: "")
        } else toast("Pago cancelado")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Datos del comprador y carrito (una sola lectura)
        Repo.loadProfile { p ->
            profile = p
            if (p != null && isAdded && p.address.isNotEmpty()) view.findViewById<EditText>(R.id.etAddress).setText(p.address)
        }
        Repo.cartRef().get().addOnSuccessListener { snap ->
            items = snap.documents.mapNotNull { it.toObject(CartItem::class.java) }
            total = items.sumOf { it.price * it.qty }
            if (isAdded) view.findViewById<TextView>(R.id.tvTotalPay).text = "Total a pagar: ${total.cop()}"
        }

        view.findViewById<View>(R.id.btnLocation).setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) captureLocation()
            else locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        view.findViewById<View>(R.id.btnPayEpayco).setOnClickListener { startPayment(view) }
    }

    /** Obtiene la ubicación actual con el Fused Location Provider (Google Play Services). */
    @SuppressLint("MissingPermission")
    private fun captureLocation() {
        val status = requireView().findViewById<TextView>(R.id.tvLocationStatus)
        status.text = "Obteniendo ubicación…"
        LocationServices.getFusedLocationProviderClient(requireContext())
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc == null) { status.text = "No se pudo obtener la ubicación. Activa el GPS."; return@addOnSuccessListener }
                lat = loc.latitude; lng = loc.longitude
                status.text = "Ubicación capturada: %.5f, %.5f".format(lat, lng)
                reverseGeocode()
            }
            .addOnFailureListener { status.text = "Error al obtener la ubicación" }
    }

    /** Convierte las coordenadas en una dirección legible (Geocoder de Google) y la escribe en el formulario. */
    @Suppress("DEPRECATION")
    private fun reverseGeocode() {
        val ctx = requireContext().applicationContext
        Thread {
            val line = try { Geocoder(ctx, Locale("es")).getFromLocation(lat, lng, 1)?.firstOrNull()?.getAddressLine(0) } catch (e: Exception) { null }
            activity?.runOnUiThread { if (line != null && isAdded) requireView().findViewById<EditText>(R.id.etAddress).setText(line) }
        }.start()
    }

    private fun setPayEnabled(on: Boolean) { view?.findViewById<View>(R.id.btnPayEpayco)?.isEnabled = on }

    /** Valida los datos y abre ePayco (o el modo demostración si la llave pública aún no está configurada). */
    private fun startPayment(view: View) {
        val address = view.str(R.id.etAddress)
        if (items.isEmpty()) { toast("Tu carrito está vacío"); return }
        if (address.isEmpty()) { toast("Escribe o captura la dirección de entrega"); return }

        val usePaypal = Config.PAYMENT_PROVIDER == "paypal"
        val unconfigured = if (usePaypal) Config.PAYPAL_CLIENT_ID.startsWith("TU_") else Config.EPAYCO_PUBLIC_KEY.startsWith("TU_")
        if (unconfigured) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Pasarela sin configurar")
                .setMessage("Faltan las credenciales de la pasarela en Config.kt. ¿Simular un pago aprobado solo para pruebas?")
                .setPositiveButton("Simular") { _, _ -> onPaymentResult(1, "DEMO-" + UUID.randomUUID().toString().take(8)) }
                .setNegativeButton("Cancelar", null).show()
            return
        }
        setPayEnabled(false)
        epaycoLauncher.launch(Intent(requireContext(), if (usePaypal) PaypalActivity::class.java else EpaycoActivity::class.java).apply {
            putExtra(EpaycoActivity.EXTRA_AMOUNT, total)
            putExtra(EpaycoActivity.EXTRA_DESC, "Pedido AndesCafé (${items.size} productos)")
            putExtra(EpaycoActivity.EXTRA_NAME, profile?.name ?: "")
            putExtra(EpaycoActivity.EXTRA_EMAIL, Repo.auth.currentUser?.email ?: "")
            putExtra(EpaycoActivity.EXTRA_INVOICE, UUID.randomUUID().toString().take(12))
        })
    }

    /** Códigos ePayco: 1 aceptada, 2 rechazada, 3 pendiente, 4 fallida. */
    private fun onPaymentResult(code: Int, ref: String) {
        when (code) {
            1 -> createOrder("Pagado", ref)
            3 -> createOrder("Pago pendiente", ref)
            else -> toast("El pago fue rechazado o falló. Intenta de nuevo.")
        }
    }

    /** Registra la orden, descuenta el stock y vacía el carrito en una sola operación atómica (batch). */
    private fun createOrder(status: String, ref: String) {
        val address = requireView().str(R.id.etAddress)
        val orderRef = Repo.db.collection("orders").document()
        val order = Order(
            id = orderRef.id, buyerId = Repo.uid(), buyerName = profile?.name ?: "",
            sellerIds = items.map { it.sellerId }.distinct(),
            items = items.map { mapOf("productId" to it.productId, "name" to it.name, "price" to it.price, "qty" to it.qty, "sellerId" to it.sellerId) },
            total = total, address = address, lat = lat, lng = lng,
            paymentRef = ref, status = status, createdAt = System.currentTimeMillis()
        )
        val batch = Repo.db.batch()
        batch.set(orderRef, order)
        items.forEach {
            batch.update(Repo.db.collection("products").document(it.productId), "stock", FieldValue.increment(-it.qty))
            batch.delete(Repo.cartRef().document(it.productId))
        }
        batch.commit()
            .addOnSuccessListener {
                toast("¡Compra registrada! Estado: $status")
                findNavController().navigate(R.id.buyerOrders, null, NavOptions.Builder().setPopUpTo(R.id.buyerHome, false).build())
            }
            .addOnFailureListener { toast("El pago se procesó pero no se pudo guardar el pedido: ${it.localizedMessage}") }
    }
}
