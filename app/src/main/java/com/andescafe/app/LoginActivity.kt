package com.andescafe.app

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.andescafe.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var b: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.btnLogin.setOnClickListener { login() }
        b.btnBiometric.setOnClickListener { biometric() }
        b.tvForgot.setOnClickListener { startActivity(Intent(this, RecoverActivity::class.java)) }
    }

    private fun login() {
        val email = b.etEmail.text.toString().trim()
        val pass = b.etPass.text.toString()
        b.tilEmail.error = if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) null else getString(R.string.err_email)
        b.tilPass.error = if (pass.length >= 4) null else getString(R.string.err_pass)
        if (b.tilEmail.error == null && b.tilPass.error == null) {
            Repo.userEmail = email
            enter()
        }
    }

    private fun selectedRole() = when (b.chipGroup.checkedChipId) {
        R.id.chipAdmin -> "Administrador"
        R.id.chipSeller -> "Vendedor"
        else -> "Comprador"
    }

    private fun enter() {
        Repo.role = selectedRole()
        Repo.userName = Repo.userEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        val dest = when (Repo.role) {
            "Administrador" -> AdminActivity::class.java
            "Vendedor" -> SellerActivity::class.java
            else -> BuyerActivity::class.java
        }
        startActivity(Intent(this, dest))
        finish()
    }

    private fun biometric() {
        val auth = BiometricManager.Authenticators.BIOMETRIC_WEAK
        if (BiometricManager.from(this).canAuthenticate(auth) != BiometricManager.BIOMETRIC_SUCCESS) {
            toast(getString(R.string.bio_unavailable))
            return
        }
        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = enter()
            })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.bio_title))
            .setSubtitle(getString(R.string.bio_sub))
            .setNegativeButtonText(getString(R.string.cancel))
            .setAllowedAuthenticators(auth)
            .build()
        prompt.authenticate(info)
    }
}
