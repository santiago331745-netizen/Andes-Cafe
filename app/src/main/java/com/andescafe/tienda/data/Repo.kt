package com.andescafe.tienda.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

/** Acceso centralizado a Firebase (Auth y Firestore) y operaciones comunes de usuarios. */
object Repo {
    val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    /** uid del usuario autenticado ("" si no hay sesión). */
    fun uid(): String = auth.currentUser?.uid ?: ""

    /** Carrito del usuario: carts/{uid}/items. */
    fun cartRef(uid: String = uid()): CollectionReference =
        db.collection("carts").document(uid).collection("items")

    /** Carga el perfil (rol, datos) de un usuario desde Firestore. Devuelve null si no existe. */
    fun loadProfile(uid: String = uid(), cb: (UserProfile?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { cb(it.toObject(UserProfile::class.java)?.copy(uid = it.id)) }
            .addOnFailureListener { cb(null) }
    }

    /** Crea o reemplaza el documento de perfil. */
    fun saveProfile(p: UserProfile, cb: (Boolean) -> Unit) {
        db.collection("users").document(p.uid).set(p)
            .addOnSuccessListener { cb(true) }
            .addOnFailureListener { cb(false) }
    }

    /** Auto-registro (Comprador o Vendedor): crea la cuenta en Auth y su perfil en Firestore. cb(null) = éxito. */
    fun register(name: String, email: String, pass: String, role: String, cb: (String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { res ->
                val uid = res.user!!.uid
                saveProfile(UserProfile(uid = uid, name = name, email = email, role = role)) { ok ->
                    cb(if (ok) null else "La cuenta se creó pero no se pudo guardar el perfil")
                }
            }
            .addOnFailureListener { cb(it.localizedMessage ?: "No se pudo registrar") }
    }

    /**
     * Crea un usuario desde el panel del Administrador sin cerrar la sesión del admin.
     * Truco: se usa una segunda instancia de FirebaseApp para el alta en Auth.
     */
    fun createUserAsAdmin(ctx: Context, name: String, email: String, pass: String, role: String, active: Boolean, cb: (String?) -> Unit) {
        val secondary = try {
            FirebaseApp.getInstance("secondary")
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(ctx, FirebaseApp.getInstance().options, "secondary")
        }
        val secAuth = FirebaseAuth.getInstance(secondary)
        secAuth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { res ->
                val uid = res.user!!.uid
                secAuth.signOut()
                saveProfile(UserProfile(uid = uid, name = name, email = email, role = role, active = active)) { ok ->
                    cb(if (ok) null else "Se creó la cuenta pero no el perfil")
                }
            }
            .addOnFailureListener { cb(it.localizedMessage ?: "No se pudo crear el usuario") }
    }
}
