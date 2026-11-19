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

/** Inicio: saludo personalizado y productos disponibles. */
class BuyerHomeFragment : Fragment(R.layout.fragment_buyer_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Repo.loadProfile { p ->
            if (p != null && isAdded) view.findViewById<TextView>(R.id.tvGreeting).text = "¡Hola, ${p.name.substringBefore(' ')}!"
        }
        val rv = view.findViewById<RecyclerView>(R.id.recycler)
        rv.layoutManager = LinearLayoutManager(requireContext())
        val adapter = RowAdapter(onClick = { r -> findNavController().navigate(R.id.buyerDetail, bundleOf("id" to r.id)) })
        rv.adapter = adapter
        listen(Repo.db.collection("products")) { docs ->
            adapter.submit(docs.mapNotNull { d -> d.toObject(Product::class.java)?.takeIf { it.stock > 0 }?.let { p ->
                Row(p.name, p.description.take(40), p.price.cop(), d.id, p.imageUrl)
            } }.take(20))
        }
    }
}
