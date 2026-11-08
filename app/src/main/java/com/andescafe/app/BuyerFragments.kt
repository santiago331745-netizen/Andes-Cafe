package com.andescafe.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.andescafe.app.databinding.FragmentCartBinding
import com.andescafe.app.databinding.FragmentCatalogBinding
import com.andescafe.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val b = FragmentHomeBinding.inflate(i, c, false)
        b.tvWelcome.text = "Hola, ${Repo.userName} ☕"
        b.rv.layoutManager = GridLayoutManager(requireContext(), 2)
        b.rv.adapter = productAdapter(Repo.products.take(4))
        b.btnCatalog.setOnClickListener { (activity as BuyerActivity).goTo(R.id.nav_catalog) }
        return b.root
    }
}

class CatalogFragment : Fragment() {
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val b = FragmentCatalogBinding.inflate(i, c, false)
        val ad = productAdapter(Repo.products.toList())
        b.rv.layoutManager = GridLayoutManager(requireContext(), 2)
        b.rv.adapter = ad
        b.etSearch.doAfterTextChanged { q ->
            val t = q.toString()
            ad.update(Repo.products.filter { it.name.contains(t, true) || it.category.contains(t, true) })
        }
        return b.root
    }
}

class CartFragment : Fragment() {
    private lateinit var b: FragmentCartBinding
    private lateinit var ad: SimpleAdapter<CartItem>

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        b = FragmentCartBinding.inflate(i, c, false)
        ad = SimpleAdapter(Repo.cart.toList(), R.layout.item_cart) { v, ci ->
            v.findViewById<TextView>(R.id.tvName).text = ci.product.name
            v.findViewById<TextView>(R.id.tvQty).text = "${ci.qty} x ${Repo.money(ci.product.price)}"
            v.findViewById<TextView>(R.id.tvSubtotal).text = Repo.money(ci.product.price * ci.qty)
            v.findViewById<View>(R.id.btnRemove).setOnClickListener { Repo.cart.remove(ci); refresh() }
        }
        b.rv.layoutManager = LinearLayoutManager(requireContext())
        b.rv.adapter = ad
        b.btnPay.setOnClickListener {
            if (Repo.cart.isEmpty()) requireContext().toast(getString(R.string.empty_cart))
            else startActivity(Intent(requireContext(), PaymentActivity::class.java))
        }
        return b.root
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        ad.update(Repo.cart.toList())
        b.tvTotal.text = Repo.money(Repo.cartTotal())
        b.tvEmpty.visibility = if (Repo.cart.isEmpty()) View.VISIBLE else View.GONE
    }
}
