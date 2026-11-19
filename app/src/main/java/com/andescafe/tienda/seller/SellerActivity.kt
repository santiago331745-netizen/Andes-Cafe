package com.andescafe.tienda.seller

import com.andescafe.tienda.R
import com.andescafe.tienda.common.DrawerActivity

/** Contenedor del rol Vendedor (Navigation Drawer + grafo nav_seller). */
class SellerActivity : DrawerActivity() {
    override val graphRes = R.navigation.nav_seller
    override val menuRes = R.menu.menu_seller_drawer
    override val roleName = "Vendedor"
}
