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

import android.text.Editable
import android.text.TextWatcher

/** Catálogo en cuadrícula con búsqueda por nombre (filtro en el cliente). */
class BuyerCatalogFragment : Fragment(R.layout.fragment_buyer_catalog) {

    private var all = listOf<Row>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.recycler)
        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        val adapter = RowAdapter(R.layout.item_product, onClick = { r -> findNavController().navigate(R.id.buyerDetail, bundleOf("id" to r.id)) })
        rv.adapter = adapter

        listen(Repo.db.collection("products")) { docs ->
            all = docs.mapNotNull { d -> d.toObject(Product::class.java)?.takeIf { it.stock > 0 }?.let { p ->
                Row(p.name, p.sellerName, p.price.cop(), d.id, p.imageUrl)
            } }.sortedBy { it.title.lowercase() }
            adapter.submit(filter(view.str(R.id.etSearch)))
        }
        view.findViewById<EditText>(R.id.etSearch).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { adapter.submit(filter(s.toString())) }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        })
    }

    private fun filter(q: String) = if (q.isBlank()) all else all.filter { it.title.contains(q.trim(), ignoreCase = true) }
}
