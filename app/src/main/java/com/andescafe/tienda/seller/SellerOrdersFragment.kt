package com.andescafe.tienda.seller

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

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Pedidos recibidos por el vendedor (órdenes que incluyen alguno de sus productos).
 * Toque = abrir la dirección de entrega en Google Maps; pulsación larga = cambiar el estado.
 */
class SellerOrdersFragment : Fragment(R.layout.fragment_seller_orders) {

    private var orders = mapOf<String, Order>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.recycler)
        rv.layoutManager = LinearLayoutManager(requireContext())
        val adapter = RowAdapter(
            onClick = { r -> orders[r.id]?.let { openMap(it) } },
            onLongClick = { r ->
                val states = arrayOf("Pagado", "Enviado", "Entregado")
                MaterialAlertDialogBuilder(requireContext()).setTitle("Cambiar estado del pedido")
                    .setItems(states) { _, i ->
                        Repo.db.collection("orders").document(r.id).update("status", states[i])
                            .addOnSuccessListener { toast("Estado actualizado") }
                    }.show()
            })
        rv.adapter = adapter
        listen(Repo.db.collection("orders").whereArrayContains("sellerIds", Repo.uid())) { docs ->
            val list = docs.mapNotNull { d -> d.toObject(Order::class.java)?.copy(id = d.id) }.sortedByDescending { it.createdAt }
            orders = list.associateBy { it.id }
            adapter.submit(list.map { o ->
                Row("Pedido #${o.id.take(6).uppercase()} · ${o.buyerName}", "${o.createdAt.dateTime()} · ${o.address}", o.status, o.id)
            })
        }
    }

    /** Abre la ubicación de entrega en Google Maps (esquema geo:). */
    private fun openMap(o: Order) {
        if (o.lat == 0.0 && o.lng == 0.0) { toast("El comprador no compartió su ubicación"); return }
        val uri = Uri.parse("geo:${o.lat},${o.lng}?q=${o.lat},${o.lng}(Entrega ${o.buyerName})")
        try { startActivity(Intent(Intent.ACTION_VIEW, uri)) } catch (e: ActivityNotFoundException) { toast("No hay una app de mapas instalada") }
    }
}
