package com.andescafe.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.appbar.MaterialToolbar

/** Adaptador genérico: recibe la lista, el layout del ítem y una lambda que enlaza los datos. */
class SimpleAdapter<T>(
    private var items: List<T>,
    private val layout: Int,
    private val bind: (View, T) -> Unit
) : RecyclerView.Adapter<SimpleAdapter.VH>() {
    class VH(v: View) : RecyclerView.ViewHolder(v)
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(layout, p, false))
    override fun onBindViewHolder(h: VH, i: Int) = bind(h.itemView, items[i])
    override fun getItemCount() = items.size
    fun update(n: List<T>) { items = n; notifyDataSetChanged() }
}

fun Context.toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()

fun Activity.logout() {
    startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
}

fun AppCompatActivity.setupBar(t: MaterialToolbar, title: String) {
    setSupportActionBar(t)
    supportActionBar?.title = title
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    t.setNavigationOnClickListener { finish() }
}

fun Fragment.productAdapter(list: List<Product>) = SimpleAdapter(list, R.layout.item_product) { v, p ->
    v.findViewById<TextView>(R.id.tvName).text = p.name
    v.findViewById<TextView>(R.id.tvPrice).text = Repo.money(p.price)
    v.findViewById<TextView>(R.id.tvCategory).text = p.category
    v.setOnClickListener {
        startActivity(Intent(requireContext(), ProductDetailActivity::class.java).putExtra("id", p.id))
    }
}

fun orderAdapter(list: List<Order>) = SimpleAdapter(list, R.layout.item_order) { v, o ->
    v.findViewById<TextView>(R.id.tvOrder).text = "Pedido #${o.id}"
    v.findViewById<TextView>(R.id.tvCustomer).text = "${o.customer} · ${o.items} ítems"
    v.findViewById<TextView>(R.id.tvTotal).text = Repo.money(o.total)
    val st = v.findViewById<TextView>(R.id.tvStatus)
    st.text = o.status
    st.setTextColor(
        when (o.status) {
            "Entregado" -> ContextCompat.getColor(v.context, R.color.leaf)
            "Enviado" -> ContextCompat.getColor(v.context, R.color.coffee)
            else -> ContextCompat.getColor(v.context, R.color.caramel)
        }
    )
}
