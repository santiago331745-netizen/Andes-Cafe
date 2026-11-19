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

/** Dashboard: totales de usuarios, productos, pedidos y ventas leídos de Firestore. */
class AdminDashboardFragment : Fragment(R.layout.fragment_admin_dashboard) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        count("users", view.findViewById(R.id.tvStatUsers))
        count("products", view.findViewById(R.id.tvStatProducts))
        count("orders", view.findViewById(R.id.tvStatOrders))
        // Ventas = suma del total de todas las órdenes
        Repo.db.collection("orders").get().addOnSuccessListener { snap ->
            view.findViewById<TextView>(R.id.tvStatSales).text = snap.documents.sumOf { it.getLong("total") ?: 0L }.cop()
        }
        view.findViewById<View>(R.id.btnGoUsers).setOnClickListener { findNavController().navigate(R.id.adminUsers) }
        view.findViewById<View>(R.id.btnGoSales).setOnClickListener { findNavController().navigate(R.id.adminSales) }
    }

    /** Cuenta los documentos de una colección sin descargarlos (consulta de agregación). */
    private fun count(collection: String, target: TextView) {
        Repo.db.collection(collection).count().get(AggregateSource.SERVER)
            .addOnSuccessListener { target.text = it.count.toString() }
    }
}
