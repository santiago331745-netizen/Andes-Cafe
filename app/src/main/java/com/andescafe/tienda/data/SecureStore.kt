package com.andescafe.tienda.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Guarda cifradas (AES-256) las credenciales del usuario para el inicio de sesión con huella. */
object SecureStore {
    private fun prefs(c: Context) = EncryptedSharedPreferences.create(
        c, "secure_login",
        MasterKey.Builder(c).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(c: Context, email: String, pass: String) =
        prefs(c).edit().putString("email", email).putString("pass", pass).apply()

    /** Devuelve (correo, contraseña) o null si el usuario aún no habilitó la huella. */
    fun load(c: Context): Pair<String, String>? {
        val p = prefs(c)
        val e = p.getString("email", null) ?: return null
        val s = p.getString("pass", null) ?: return null
        return e to s
    }
}
