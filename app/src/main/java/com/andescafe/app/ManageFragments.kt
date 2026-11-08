package com.andescafe.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.andescafe.app.databinding.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** CRUD de productos: lo usan Vendedor y Administrador. */
class ManageProductsFragment : Fragment() {
    private lateinit var ad: SimpleAdapter<Product>

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val b = FragmentListFabBinding.inflate(i, c, false)
        ad = SimpleAdapter(Repo.products.toList(), R.layout.item_manage_product) { v, p ->
            v.findViewById<TextView>(R.id.tvName).text = p.name
            v.findViewById<TextView>(R.id.tvPrice).text = Repo.money(p.price)
            v.findViewById<TextView>(R.id.tvStock).text = "Stock: ${p.stock} · ${p.category}"
            v.findViewById<View>(R.id.btnEdit).setOnClickListener { open(p.id) }
            v.findViewById<View>(R.id.btnDelete).setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete).setMessage("¿Eliminar ${p.name}?")
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete) { _, _ -> Repo.products.remove(p); ad.update(Repo.products.toList()) }
                    .show()
            }
        }
        b.rv.layoutManager = LinearLayoutManager(requireContext())
        b.rv.adapter = ad
        b.fab.setOnClickListener { open(-1) }
        return b.root
    }

    private fun open(id: Int) = startActivity(Intent(requireContext(), ProductFormActivity::class.java).putExtra("id", id))
    override fun onResume() { super.onResume(); ad.update(Repo.products.toList()) }
}

class UsersFragment : Fragment() {
    private lateinit var ad: SimpleAdapter<User>

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val b = FragmentListFabBinding.inflate(i, c, false)
        ad = SimpleAdapter(Repo.users.toList(), R.layout.item_user) { v, u ->
            v.findViewById<TextView>(R.id.tvName).text = u.name
            v.findViewById<TextView>(R.id.tvEmail).text = u.email
            v.findViewById<TextView>(R.id.tvRole).text = u.role
            v.findViewById<View>(R.id.btnEdit).setOnClickListener { open(u.id) }
            v.findViewById<View>(R.id.btnDelete).setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete).setMessage("¿Eliminar a ${u.name}?")
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete) { _, _ -> Repo.users.remove(u); ad.update(Repo.users.toList()) }
                    .show()
            }
        }
        b.rv.layoutManager = LinearLayoutManager(requireContext())
        b.rv.adapter = ad
        b.fab.setOnClickListener { open(-1) }
        return b.root
    }

    private fun open(id: Int) = startActivity(Intent(requireContext(), UserFormActivity::class.java).putExtra("id", id))
    override fun onResume() { super.onResume(); ad.update(Repo.users.toList()) }
}

class UserFormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityUserFormBinding.inflate(layoutInflater)
        setContentView(b.root)
        val u = Repo.users.firstOrNull { it.id == intent.getIntExtra("id", -1) }
        setupBar(b.toolbar, if (u == null) "Nuevo usuario" else "Editar usuario")
        u?.let {
            b.etName.setText(it.name); b.etEmail.setText(it.email)
            b.chipGroup.check(when (it.role) { "Administrador" -> R.id.chipAdmin; "Vendedor" -> R.id.chipSeller; else -> R.id.chipBuyer })
        }
        b.btnSave.setOnClickListener {
            val name = b.etName.text.toString().trim()
            val email = b.etEmail.text.toString().trim()
            val okEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
            b.tilName.error = if (name.isEmpty()) getString(R.string.required) else null
            b.tilEmail.error = if (okEmail) null else getString(R.string.err_email)
            if (name.isEmpty() || !okEmail) return@setOnClickListener
            val role = when (b.chipGroup.checkedChipId) { R.id.chipAdmin -> "Administrador"; R.id.chipSeller -> "Vendedor"; else -> "Comprador" }
            if (u == null) Repo.users.add(User(Repo.nextUserId(), name, email, role))
            else { u.name = name; u.email = email; u.role = role }
            toast("Usuario guardado")
            finish()
        }
    }
}

class OrdersFragment : Fragment() {
    private lateinit var ad: SimpleAdapter<Order>
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val b = FragmentListBinding.inflate(i, c, false)
        ad = orderAdapter(Repo.orders.toList())
        b.rv.layoutManager = LinearLayoutManager(requireContext())
        b.rv.adapter = ad
        return b.root
    }
    override fun onResume() { super.onResume(); ad.update(Repo.orders.toList()) }
}

class DashboardFragment : Fragment() {
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val b = FragmentDashboardBinding.inflate(i, c, false)
        b.tvStatProducts.text = Repo.products.size.toString()
        b.tvStatUsers.text = Repo.users.size.toString()
        b.tvStatOrders.text = Repo.orders.size.toString()
        b.tvStatSales.text = Repo.money(Repo.orders.sumOf { it.total })
        b.rv.layoutManager = LinearLayoutManager(requireContext())
        b.rv.adapter = orderAdapter(Repo.orders.take(3))
        return b.root
    }
}

class ReportFragment : Fragment() {
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val b = FragmentReportBinding.inflate(i, c, false)
        val total = Repo.orders.sumOf { it.total }
        b.tvTotalSales.text = Repo.money(total)
        b.tvAvg.text = Repo.money(if (Repo.orders.isEmpty()) 0 else total / Repo.orders.size)
        b.barCafe.progress = 70; b.barCaps.progress = 25; b.barAcc.progress = 45
        b.rv.layoutManager = LinearLayoutManager(requireContext())
        b.rv.adapter = orderAdapter(Repo.orders.toList())
        return b.root
    }
}
