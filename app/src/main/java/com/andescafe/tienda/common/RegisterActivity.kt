package com.andescafe.tienda.common

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.andescafe.tienda.R
import com.andescafe.tienda.data.Repo

/** Registro de nuevos usuarios (Comprador o Vendedor). Los administradores los crea otro administrador. */
class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val root = findViewById<View>(android.R.id.content)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnRegister).setOnClickListener {
            val name = root.str(R.id.etName); val email = root.str(R.id.etEmail)
            val pass = root.str(R.id.etPassword); val confirm = root.str(R.id.etConfirm)
            val role = if (findViewById<RadioGroup>(R.id.rgRole).checkedRadioButtonId == R.id.rbSeller) "seller" else "buyer"
            when {
                name.isEmpty() -> Toast.makeText(this, "Escribe tu nombre", Toast.LENGTH_SHORT).show()
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    findViewById<TextInputLayout>(R.id.tilEmail).error = "Correo no válido"
                pass.length < 6 -> findViewById<TextInputLayout>(R.id.tilPassword).error = "Mínimo 6 caracteres"
                pass != confirm -> findViewById<TextInputLayout>(R.id.tilConfirm).error = "Las contraseñas no coinciden"
                else -> Repo.register(name, email, pass, role) { error ->
                    if (error == null) { Toast.makeText(this, "Cuenta creada", Toast.LENGTH_SHORT).show(); goHome(role) }
                    else Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
