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

import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** Carrito del comprador (Firestore: carts/{uid}/items) con total en tiempo real. */
class BuyerCartFragment : Fragment(R.layout.fragment_buyer_cart) {

    private var items = listOf<CartItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.recycler)
        rv.layoutManager = LinearLayoutManager(requireContext())
        val adapter = RowAdapter(
            onClick = { r -> editItem(r.id) },
            onLongClick = { r -> confirmDelete(r.title) { Repo.cartRef().document(r.id).delete() } })
        rv.adapter = adapter

        listen(Repo.cartRef()) { docs ->
            items = docs.mapNotNull { it.toObject(CartItem::class.java) }
            adapter.submit(items.map { Row(it.name, "Cantidad: ${it.qty} · ${it.price.cop()} c/u", (it.price * it.qty).cop(), it.productId, it.imageUrl) })
            view.findViewById<TextView>(R.id.tvTotal).text = items.sumOf { it.price * it.qty }.cop()
        }
        view.findViewById<View>(R.id.btnPay).setOnClickListener {
            if (items.isEmpty()) toast("Tu carrito está vacío") else findNavController().navigate(R.id.buyerPayment)
        }
    }

    /** Menú para cambiar la cantidad de un ítem o quitarlo del carrito. */
    private fun editItem(productId: String) {
        val item = items.firstOrNull { it.productId == productId } ?: return
        val ref = Repo.cartRef().document(productId)
        MaterialAlertDialogBuilder(requireContext()).setTitle(item.name)
            .setItems(arrayOf("Aumentar cantidad", "Disminuir cantidad", "Eliminar del carrito")) { _, which ->
                when (which) {
                    0 -> ref.update("qty", FieldValue.increment(1))
                    1 -> if (item.qty > 1) ref.update("qty", FieldValue.increment(-1)) else ref.delete()
                    else -> ref.delete()
                }
            }.show()
    }
}
