package com.andescafe.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.andescafe.app.databinding.ActivityPaymentBinding
import com.andescafe.app.databinding.ActivityProductDetailBinding
import com.andescafe.app.databinding.ActivityProductFormBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProductDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupBar(b.toolbar, getString(R.string.product_detail))
        val p = Repo.products.firstOrNull { it.id == intent.getIntExtra("id", -1) } ?: run { finish(); return }
        var qty = 1
        b.tvName.text = p.name
        b.tvPrice.text = Repo.money(p.price)
        b.tvDesc.text = p.desc
        b.tvCategory.text = p.category
        b.tvStock.text = "Disponibles: ${p.stock}"
        b.btnPlus.setOnClickListener { if (qty < p.stock) { qty++; b.tvQty.text = qty.toString() } }
        b.btnMinus.setOnClickListener { if (qty > 1) { qty--; b.tvQty.text = qty.toString() } }
        b.btnAdd.setOnClickListener { Repo.addToCart(p, qty); toast("Agregado al carrito"); finish() }
    }
}

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupBar(b.toolbar, getString(R.string.payment))
        b.tvTotal.text = Repo.money(Repo.cartTotal())
        b.rgMethod.setOnCheckedChangeListener { _, id ->
            b.cardForm.visibility = if (id == R.id.rbCard) View.VISIBLE else View.GONE
        }
        b.btnConfirm.setOnClickListener {
            if (b.rgMethod.checkedRadioButtonId == R.id.rbCard) {
                val okNum = b.etCard.text.toString().filter { it.isDigit() }.length == 16
                val okName = b.etHolder.text.toString().isNotBlank()
                val okCvv = b.etCvv.text.toString().length == 3
                b.tilCard.error = if (okNum) null else "Debe tener 16 dígitos"
                b.tilHolder.error = if (okName) null else getString(R.string.required)
                b.tilCvv.error = if (okCvv) null else "3 dígitos"
                if (!(okNum && okName && okCvv)) return@setOnClickListener
            }
            Repo.orders.add(0, Order(Repo.nextOrderId(), Repo.userName, Repo.cart.sumOf { it.qty }, Repo.cartTotal(), "Pendiente"))
            Repo.cart.clear()
            MaterialAlertDialogBuilder(this)
                .setTitle("¡Pago exitoso!")
                .setMessage("Tu pedido fue registrado y el vendedor lo recibirá.")
                .setCancelable(false)
                .setPositiveButton("Aceptar") { _, _ -> finish() }
                .show()
        }
    }
}

/** Crear / editar producto (CRUD del vendedor y del administrador). */
class ProductFormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityProductFormBinding.inflate(layoutInflater)
        setContentView(b.root)
        val p = Repo.products.firstOrNull { it.id == intent.getIntExtra("id", -1) }
        setupBar(b.toolbar, if (p == null) "Nuevo producto" else "Editar producto")
        p?.let {
            b.etName.setText(it.name); b.etPrice.setText(it.price.toString()); b.etStock.setText(it.stock.toString())
            b.etCategory.setText(it.category); b.etDesc.setText(it.desc)
        }
        b.btnSave.setOnClickListener {
            val name = b.etName.text.toString().trim()
            val price = b.etPrice.text.toString().toIntOrNull()
            val stock = b.etStock.text.toString().toIntOrNull()
            b.tilName.error = if (name.isEmpty()) getString(R.string.required) else null
            b.tilPrice.error = if (price == null || price <= 0) "Precio inválido" else null
            b.tilStock.error = if (stock == null) "Stock inválido" else null
            if (name.isEmpty() || price == null || price <= 0 || stock == null) return@setOnClickListener
            val cat = b.etCategory.text.toString().ifBlank { "Café" }
            val desc = b.etDesc.text.toString()
            if (p == null) Repo.products.add(Product(Repo.nextProductId(), name, price, desc, cat, stock))
            else { p.name = name; p.price = price; p.stock = stock; p.category = cat; p.desc = desc }
            toast("Producto guardado")
            finish()
        }
    }
}
