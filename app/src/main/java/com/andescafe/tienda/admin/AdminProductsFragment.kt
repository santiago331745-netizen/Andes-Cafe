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

/** El administrador ve todos los productos y puede eliminar los que incumplan las normas (pulsación larga). */
class AdminProductsFragment : Fragment(R.layout.fragment_admin_products) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.recycler)
        rv.layoutManager = LinearLayoutManager(requireContext())
        val adapter = RowAdapter(
            onClick = { r -> toast("Vendedor: ${r.subtitle.substringAfter("· ")}") },
            onLongClick = { r -> confirmDelete(r.title) {
                Repo.db.collection("products").document(r.id).delete().addOnSuccessListener { toast("Producto eliminado") }
            } })
        rv.adapter = adapter
        listen(Repo.db.collection("products")) { docs ->
            adapter.submit(docs.mapNotNull { d ->
                d.toObject(Product::class.java)?.let { p ->
                    Row(p.name, "Stock ${p.stock} · ${p.sellerName}", p.price.cop(), d.id, p.imageUrl)
                }
            }.sortedBy { it.title.lowercase() })
        }
    }
}
