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

/** Historial de compras del comprador (Firestore: órdenes con buyerId = uid). */
class BuyerOrdersFragment : Fragment(R.layout.fragment_buyer_orders) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.recycler)
        rv.layoutManager = LinearLayoutManager(requireContext())
        val adapter = RowAdapter()
        rv.adapter = adapter
        listen(Repo.db.collection("orders").whereEqualTo("buyerId", Repo.uid())) { docs ->
            adapter.submit(docs.mapNotNull { d -> d.toObject(Order::class.java)?.copy(id = d.id) }
                .sortedByDescending { it.createdAt }
                .map { o -> Row("Pedido #${o.id.take(6).uppercase()} · ${o.total.cop()}", "${o.createdAt.dateTime()} · ${o.items.size} productos", o.status, o.id) })
        }
    }
}
