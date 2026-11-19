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

import com.bumptech.glide.Glide

/** Detalle de un producto: datos, imagen, selector de cantidad y botón para agregar al carrito. */
class BuyerDetailFragment : Fragment(R.layout.fragment_buyer_detail) {

    private var product: Product? = null
    private var qty = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = arguments?.getString("id") ?: run { findNavController().popBackStack(); return }

        Repo.db.collection("products").document(id).get().addOnSuccessListener { d ->
            val p = d.toObject(Product::class.java)?.copy(id = d.id) ?: return@addOnSuccessListener
            if (!isAdded) return@addOnSuccessListener
            product = p
            view.findViewById<TextView>(R.id.tvName).text = p.name
            view.findViewById<TextView>(R.id.tvPrice).text = p.price.cop()
            view.findViewById<TextView>(R.id.tvStock).text = "Disponibles: ${p.stock} · Vendedor: ${p.sellerName}"
            view.findViewById<TextView>(R.id.tvDesc).text = p.description.ifEmpty { "Sin descripción" }
            if (p.imageUrl.isNotEmpty()) {
                val iv = view.findViewById<ImageView>(R.id.ivProduct)
                iv.setPadding(0, 0, 0, 0); iv.imageTintList = null; iv.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(this).load(p.imageUrl).into(iv)
            }
        }

        val tvQty = view.findViewById<TextView>(R.id.tvQty)
        view.findViewById<View>(R.id.btnMinus).setOnClickListener { if (qty > 1) qty--; tvQty.text = qty.toString() }
        view.findViewById<View>(R.id.btnPlus).setOnClickListener { qty++; tvQty.text = qty.toString() }

        view.findViewById<View>(R.id.btnAddCart).setOnClickListener {
            val p = product ?: return@setOnClickListener
            val ref = Repo.cartRef().document(p.id)
            // Si ya está en el carrito se suma la cantidad, sin superar las existencias
            ref.get().addOnSuccessListener { d ->
                val newQty = (d.getLong("qty") ?: 0L) + qty
                if (newQty > p.stock) toast("Solo hay ${p.stock} unidades disponibles")
                else ref.set(CartItem(p.id, p.name, p.price, newQty, p.imageUrl, p.sellerId))
                    .addOnSuccessListener { toast("Agregado al carrito"); findNavController().popBackStack() }
            }
        }
    }
}
