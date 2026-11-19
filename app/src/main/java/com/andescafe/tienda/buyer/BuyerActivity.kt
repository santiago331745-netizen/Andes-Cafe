package com.andescafe.tienda.buyer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.andescafe.tienda.R

/** Contenedor del rol Comprador: navegación inferior (Bottom Navigation) con Navigation Component. */
class BuyerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer)
        setSupportActionBar(findViewById<MaterialToolbar>(R.id.toolbar))

        val host = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        val controller = host.navController
        controller.setGraph(R.navigation.nav_buyer)
        findViewById<BottomNavigationView>(R.id.bottomNav).setupWithNavController(controller)
        controller.addOnDestinationChangedListener { _, dest, _ -> supportActionBar?.title = dest.label }
    }
}
