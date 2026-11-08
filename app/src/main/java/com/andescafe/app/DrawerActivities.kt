package com.andescafe.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.andescafe.app.databinding.ActivityDrawerBinding

/** Base para Administrador y Vendedor: Navigation Drawer con menú distinto por rol. */
abstract class DrawerActivity : AppCompatActivity() {
    abstract val menuRes: Int
    abstract fun fragmentFor(id: Int): Fragment
    private lateinit var b: ActivityDrawerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityDrawerBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.toolbar.setNavigationOnClickListener { b.drawer.openDrawer(GravityCompat.START) }
        b.nav.inflateMenu(menuRes)
        b.nav.getHeaderView(0).apply {
            findViewById<TextView>(R.id.tvHeaderName).text = Repo.userName
            findViewById<TextView>(R.id.tvHeaderRole).text = Repo.role
        }
        b.nav.setNavigationItemSelectedListener { select(it.itemId); true }
        select(b.nav.menu.getItem(0).itemId)
    }

    private fun select(id: Int) {
        b.drawer.closeDrawers()
        if (id == R.id.nav_logout) { logout(); return }
        b.nav.setCheckedItem(id)
        b.toolbar.title = b.nav.menu.findItem(id).title
        supportFragmentManager.beginTransaction().replace(R.id.container, fragmentFor(id)).commit()
    }
}

class AdminActivity : DrawerActivity() {
    override val menuRes = R.menu.menu_admin
    override fun fragmentFor(id: Int): Fragment = when (id) {
        R.id.nav_dashboard -> DashboardFragment()
        R.id.nav_users -> UsersFragment()
        R.id.nav_products -> ManageProductsFragment()
        R.id.nav_report -> ReportFragment()
        else -> ProfileFragment()
    }
}

class SellerActivity : DrawerActivity() {
    override val menuRes = R.menu.menu_seller
    override fun fragmentFor(id: Int): Fragment = when (id) {
        R.id.nav_products -> ManageProductsFragment()
        R.id.nav_orders -> OrdersFragment()
        else -> ProfileFragment()
    }
}
