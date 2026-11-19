package com.andescafe.tienda.common

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputLayout
import com.andescafe.tienda.R
import com.andescafe.tienda.data.Repo
import com.andescafe.tienda.data.SecureStore

/** Inicio de sesión con correo/contraseña (Firebase Auth) o con huella dactilar. El rol se lee de Firestore. */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<View>(R.id.btnLogin).setOnClickListener {
            val email = findViewById<View>(android.R.id.content).str(R.id.etEmail)
            val pass = findViewById<View>(android.R.id.content).str(R.id.etPassword)
            if (email.isEmpty() || pass.isEmpty()) {
                findViewById<TextInputLayout>(R.id.tilEmail).error = "Ingresa correo y contraseña"
            } else signIn(email, pass, findViewById<MaterialCheckBox>(R.id.cbRemember).isChecked)
        }
        findViewById<View>(R.id.btnBiometric).setOnClickListener { biometricLogin() }
        findViewById<View>(R.id.tvForgot).setOnClickListener { startActivity(Intent(this, ForgotPasswordActivity::class.java)) }
        findViewById<View>(R.id.tvRegister).setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
    }

    /** Autentica en Firebase, valida que el perfil exista y esté activo y abre la pantalla del rol. */
    private fun signIn(email: String, pass: String, remember: Boolean) {
        Repo.auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { res ->
                Repo.loadProfile(res.user!!.uid) { p ->
                    if (p == null || !p.active) {
                        Repo.auth.signOut()
                        Toast.makeText(this, "Tu cuenta no está habilitada. Contacta al administrador.", Toast.LENGTH_LONG).show()
                    } else {
                        if (remember) SecureStore.save(this, email, pass)
                        goHome(p.role)
                    }
                }
            }
            .addOnFailureListener { Toast.makeText(this, "No se pudo iniciar sesión: ${it.localizedMessage}", Toast.LENGTH_LONG).show() }
    }

    /** Pide la huella y, si es válida, inicia sesión con las credenciales guardadas cifradas. */
    private fun biometricLogin() {
        val saved = SecureStore.load(this)
        if (saved == null) {
            Toast.makeText(this, "Inicia sesión una vez con la casilla \"Habilitar huella\" marcada", Toast.LENGTH_LONG).show()
            return
        }
        val can = BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        if (can != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Este dispositivo no tiene huella disponible o registrada", Toast.LENGTH_LONG).show()
            return
        }
        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    signIn(saved.first, saved.second, false)
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Toast.makeText(this@LoginActivity, errString, Toast.LENGTH_SHORT).show()
                }
            })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Ingreso con huella")
            .setSubtitle("Confirma tu identidad para entrar a AndesCafé")
            .setNegativeButtonText("Cancelar")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
        prompt.authenticate(info)
    }
}
