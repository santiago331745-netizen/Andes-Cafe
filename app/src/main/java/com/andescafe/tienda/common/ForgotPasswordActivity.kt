package com.andescafe.tienda.common

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.andescafe.tienda.R
import com.andescafe.tienda.data.Repo

/** Recuperación de contraseña: Firebase envía un correo con el enlace de restablecimiento. */
class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnSend).setOnClickListener {
            val email = findViewById<View>(android.R.id.content).str(R.id.etEmail)
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                findViewById<TextInputLayout>(R.id.tilEmail).error = "Ingresa un correo válido"
                return@setOnClickListener
            }
            Repo.auth.sendPasswordResetEmail(email)
                .addOnSuccessListener { Toast.makeText(this, "Te enviamos un enlace a $email", Toast.LENGTH_LONG).show(); finish() }
                .addOnFailureListener { Toast.makeText(this, "No se pudo enviar: ${it.localizedMessage}", Toast.LENGTH_LONG).show() }
        }
    }
}
