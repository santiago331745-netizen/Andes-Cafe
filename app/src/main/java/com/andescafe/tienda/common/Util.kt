package com.andescafe.tienda.common

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.andescafe.tienda.admin.AdminActivity
import com.andescafe.tienda.buyer.BuyerActivity
import com.andescafe.tienda.data.Repo
import com.andescafe.tienda.seller.SellerActivity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Muestra un mensaje corto. */
fun Fragment.toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

/** Abre la Activity principal según el rol y limpia la pila de navegación. */
fun Context.goHome(role: String) {
    val target = when (role) {
        "admin" -> AdminActivity::class.java
        "seller" -> SellerActivity::class.java
        else -> BuyerActivity::class.java
    }
    startActivity(Intent(this, target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
}

/** Cierra la sesión de Firebase y vuelve al login. */
fun Fragment.logout() {
    Repo.auth.signOut()
    startActivity(Intent(requireContext(), LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
}

/** Diálogo de confirmación antes de eliminar. */
fun Fragment.confirmDelete(name: String, onConfirm: () -> Unit) {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle("Eliminar")
        .setMessage("¿Seguro que deseas eliminar $name?")
        .setPositiveButton("Eliminar") { _, _ -> onConfirm() }
        .setNegativeButton("Cancelar", null)
        .show()
}

/**
 * Escucha en tiempo real una consulta de Firestore y entrega los documentos.
 * El listener se elimina solo cuando la vista del fragment se destruye.
 */
fun Fragment.listen(query: Query, onData: (List<DocumentSnapshot>) -> Unit) {
    val reg = query.addSnapshotListener { snap, err ->
        if (err != null) {
            Log.e("Firestore", err.message ?: "error", err)
            context?.let { Toast.makeText(it, "Error de datos: ${err.localizedMessage}", Toast.LENGTH_LONG).show() }
            return@addSnapshotListener
        }
        if (snap != null) onData(snap.documents)
    }
    viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) = reg.remove()
    })
}

/** Texto de un TextInputEditText del layout, sin espacios sobrantes. */
fun View.str(id: Int): String = findViewById<TextInputEditText>(id).text.toString().trim()

/** Formatea un valor como pesos colombianos: 32000 -> $32.000 */
fun Long.cop(): String = "$" + NumberFormat.getInstance(Locale("es", "CO")).format(this)

/** Formatea milisegundos como fecha y hora. */
fun Long.dateTime(): String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(this))

/** Nombre legible del rol. */
fun roleLabel(role: String) = when (role) {
    "admin" -> "Administrador"
    "seller" -> "Vendedor"
    else -> "Comprador"
}
