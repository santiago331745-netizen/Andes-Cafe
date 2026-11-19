package com.andescafe.tienda.common

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.andescafe.tienda.R
import com.andescafe.tienda.data.Repo

/** Activity base con Navigation Drawer, reutilizada por Administrador y Vendedor. */
abstract class DrawerActivity : AppCompatActivity() {
    abstract val graphRes: Int
    abstract val menuRes: Int
    abstract val roleName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawer = findViewById<DrawerLayout>(R.id.drawerLayout)
        val nav = findViewById<NavigationView>(R.id.navView)
        nav.inflateMenu(menuRes)
        val headerRole = nav.getHeaderView(0).findViewById<TextView>(R.id.tvHeaderRole)
        headerRole.text = roleName
        Repo.loadProfile { p -> if (p != null) headerRole.text = "$roleName · ${p.name}" }

        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close)
        toggle.drawerArrowDrawable.color = getColor(R.color.white)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val host = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        val controller = host.navController
        controller.setGraph(graphRes)
        controller.addOnDestinationChangedListener { _, dest, _ ->
            supportActionBar?.title = dest.label
            nav.setCheckedItem(dest.id)
        }

        nav.setNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.nav_logout) {
                Repo.auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            } else {
                val start = controller.graph.startDestinationId
                controller.popBackStack(start, false)
                if (item.itemId != start) controller.navigate(item.itemId)
            }
            drawer.closeDrawers()
            true
        }
    }
}
