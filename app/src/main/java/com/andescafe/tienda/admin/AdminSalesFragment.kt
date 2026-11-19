package com.andescafe.tienda.admin

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

import android.content.Intent

/** Reporte de ventas: lista todas las órdenes, muestra el total acumulado y permite compartir el resumen. */
class AdminSalesFragment : Fragment(R.layout.fragment_admin_sales) {

    private var summary = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.recycler)
        rv.layoutManager = LinearLayoutManager(requireContext())
        val adapter = RowAdapter()
        rv.adapter = adapter

        listen(Repo.db.collection("orders")) { docs ->
            val orders = docs.mapNotNull { d -> d.toObject(Order::class.java)?.copy(id = d.id) }.sortedByDescending { it.createdAt }
            val total = orders.sumOf { it.total }
            view.findViewById<TextView>(R.id.tvSalesTotal).text = "Total acumulado: ${total.cop()} · ${orders.size} pedidos"
            adapter.submit(orders.map { o ->
                Row("Pedido #${o.id.take(6).uppercase()}", "${o.buyerName} · ${o.createdAt.dateTime()}", o.total.cop(), o.id)
            })
            summary = "Reporte de ventas AndesCafé\nPedidos: ${orders.size}\nTotal: ${total.cop()}\n" +
                orders.joinToString("\n") { "#${it.id.take(6).uppercase()} - ${it.buyerName} - ${it.total.cop()} - ${it.status}" }
        }

        view.findViewById<View>(R.id.btnExport).setOnClickListener {
            val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, summary) }
            startActivity(Intent.createChooser(send, "Compartir reporte"))
        }
    }
}
