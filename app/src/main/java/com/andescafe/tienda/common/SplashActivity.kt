package com.andescafe.tienda.common

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.andescafe.tienda.R
import com.andescafe.tienda.data.Repo

/** Pantalla de bienvenida. Si ya hay sesión abierta entra directo al rol; si no, va al login. */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler(Looper.getMainLooper()).postDelayed({ route() }, 1500)
    }

    private fun toLogin() {
        startActivity(Intent(this, LoginActivity::class.java)); finish()
    }

    private fun route() {
        val user = Repo.auth.currentUser ?: return toLogin()
        Repo.loadProfile(user.uid) { p ->
            if (p != null && p.active) goHome(p.role) else { Repo.auth.signOut(); toLogin() }
        }
    }
}
