package com.andescafe.tienda.admin

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

/** Lista de usuarios (tiempo real). Toque = editar, pulsación larga = eliminar, FAB = crear. */
class AdminUsersFragment : Fragment(R.layout.fragment_admin_users) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.recycler)
        rv.layoutManager = LinearLayoutManager(requireContext())
        val adapter = RowAdapter(
            onClick = { r -> findNavController().navigate(R.id.adminUserForm, bundleOf("uid" to r.id)) },
            onLongClick = { r ->
                if (r.id == Repo.uid()) toast("No puedes eliminar tu propia cuenta")
                else confirmDelete(r.title) {
                    // Se elimina el perfil: sin perfil el usuario ya no puede entrar a la app.
                    Repo.db.collection("users").document(r.id).delete().addOnSuccessListener { toast("Usuario eliminado") }
                }
            })
        rv.adapter = adapter
        listen(Repo.db.collection("users")) { docs ->
            adapter.submit(docs.mapNotNull { d ->
                d.toObject(UserProfile::class.java)?.let { u ->
                    Row(u.name, "${u.email} · ${roleLabel(u.role)}", if (u.active) "Activo" else "Inactivo", d.id)
                }
            }.sortedBy { it.title.lowercase() })
        }
        view.findViewById<View>(R.id.fab).setOnClickListener { findNavController().navigate(R.id.adminUserForm) }
    }
}
