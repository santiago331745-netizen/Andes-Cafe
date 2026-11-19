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

/** Panel de productos del vendedor: lista sus productos; toque = editar, larga = eliminar, FAB = crear. */
class SellerProductsFragment : Fragment(R.layout.fragment_seller_products) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.recycler)
        rv.layoutManager = LinearLayoutManager(requireContext())
        val adapter = RowAdapter(
            onClick = { r -> findNavController().navigate(R.id.sellerProductForm, bundleOf("id" to r.id)) },
            onLongClick = { r -> confirmDelete(r.title) {
                Repo.db.collection("products").document(r.id).delete().addOnSuccessListener { toast("Producto eliminado") }
            } })
        rv.adapter = adapter
        // Solo los productos de este vendedor
        listen(Repo.db.collection("products").whereEqualTo("sellerId", Repo.uid())) { docs ->
            adapter.submit(docs.mapNotNull { d ->
                d.toObject(Product::class.java)?.let { p -> Row(p.name, "Stock ${p.stock}", p.price.cop(), d.id, p.imageUrl) }
            }.sortedBy { it.title.lowercase() })
        }
        view.findViewById<View>(R.id.fab).setOnClickListener { findNavController().navigate(R.id.sellerProductForm) }
    }
}
