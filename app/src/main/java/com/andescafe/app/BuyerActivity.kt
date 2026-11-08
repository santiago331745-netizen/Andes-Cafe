package com.andescafe.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.andescafe.app.databinding.ActivityBuyerBinding

/** Comprador: Bottom Navigation con Inicio, Catálogo, Carrito y Perfil. */
class BuyerActivity : AppCompatActivity() {
    private lateinit var b: ActivityBuyerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityBuyerBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.bottomNav.setOnItemSelectedListener { show(it.itemId); true }
        b.bottomNav.selectedItemId = R.id.nav_home
    }

    private fun show(id: Int) {
        val f: Fragment = when (id) {
            R.id.nav_home -> HomeFragment()
            R.id.nav_catalog -> CatalogFragment()
            R.id.nav_cart -> CartFragment()
            else -> ProfileFragment()
        }
        b.toolbar.title = b.bottomNav.menu.findItem(id).title
        supportFragmentManager.beginTransaction().replace(R.id.container, f).commit()
    }

    fun goTo(id: Int) { b.bottomNav.selectedItemId = id }
}
