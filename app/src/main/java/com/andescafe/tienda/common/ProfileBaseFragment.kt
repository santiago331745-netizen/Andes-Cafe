package com.andescafe.tienda.common

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.andescafe.tienda.R
import com.andescafe.tienda.data.Repo

/** Pantalla de perfil compartida por Vendedor y Comprador: ver y editar datos, y cerrar sesión. */
open class ProfileBaseFragment(layoutId: Int) : Fragment(layoutId) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.tvEmail).text = Repo.auth.currentUser?.email ?: ""

        // Cargar los datos guardados en Firestore
        Repo.loadProfile { p ->
            if (p == null || !isAdded) return@loadProfile
            view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName).setText(p.name)
            view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPhone).setText(p.phone)
            view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etAddress).setText(p.address)
        }

        // Guardar cambios
        view.findViewById<View>(R.id.btnSaveProfile).setOnClickListener {
            val name = view.str(R.id.etName)
            if (name.isEmpty()) { toast("El nombre no puede estar vacío"); return@setOnClickListener }
            Repo.db.collection("users").document(Repo.uid())
                .update(mapOf("name" to name, "phone" to view.str(R.id.etPhone), "address" to view.str(R.id.etAddress)))
                .addOnSuccessListener { toast("Perfil actualizado") }
                .addOnFailureListener { toast("No se pudo guardar: ${it.localizedMessage}") }
        }
        view.findViewById<View>(R.id.btnLogout).setOnClickListener { logout() }
    }
}
