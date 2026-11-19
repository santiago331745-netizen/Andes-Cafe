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

/** Crear o editar un usuario. Si llega el argumento "uid" es edición; si no, creación. */
class AdminUserFormFragment : Fragment(R.layout.fragment_admin_user_form) {

    private var editingUid: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editingUid = arguments?.getString("uid")
        val rg = view.findViewById<RadioGroup>(R.id.rgRole)
        rg.check(R.id.rbBuyer)

        if (editingUid != null) {
            // Edición: el correo y la contraseña no se modifican desde aquí
            view.findViewById<View>(R.id.etEmail).isEnabled = false
            view.findViewById<View>(R.id.tilPassword).visibility = View.GONE
            Repo.loadProfile(editingUid!!) { u ->
                if (u == null || !isAdded) return@loadProfile
                view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName).setText(u.name)
                view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEmail).setText(u.email)
                rg.check(when (u.role) { "admin" -> R.id.rbAdmin; "seller" -> R.id.rbSeller; else -> R.id.rbBuyer })
                view.findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.swActive).isChecked = u.active
            }
        }

        view.findViewById<View>(R.id.btnCancel).setOnClickListener { findNavController().popBackStack() }
        view.findViewById<View>(R.id.btnSave).setOnClickListener {
            val name = view.str(R.id.etName); val email = view.str(R.id.etEmail); val pass = view.str(R.id.etPassword)
            val role = when (rg.checkedRadioButtonId) { R.id.rbAdmin -> "admin"; R.id.rbSeller -> "seller"; else -> "buyer" }
            val active = view.findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.swActive).isChecked
            if (name.isEmpty()) { toast("Escribe el nombre"); return@setOnClickListener }

            val uid = editingUid
            if (uid != null) {
                Repo.db.collection("users").document(uid).update(mapOf("name" to name, "role" to role, "active" to active))
                    .addOnSuccessListener { toast("Usuario actualizado"); findNavController().popBackStack() }
                    .addOnFailureListener { toast("Error: ${it.localizedMessage}") }
            } else {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { toast("Correo no válido"); return@setOnClickListener }
                if (pass.length < 6) { toast("La contraseña debe tener mínimo 6 caracteres"); return@setOnClickListener }
                Repo.createUserAsAdmin(requireContext(), name, email, pass, role, active) { error ->
                    if (error == null) { toast("Usuario creado"); findNavController().popBackStack() } else toast(error)
                }
            }
        }
    }
}
