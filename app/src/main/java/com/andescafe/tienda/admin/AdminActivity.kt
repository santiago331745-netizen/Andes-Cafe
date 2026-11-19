package com.andescafe.tienda.admin

import com.andescafe.tienda.R
import com.andescafe.tienda.common.DrawerActivity

/** Contenedor del rol Administrador (Navigation Drawer + grafo nav_admin). */
class AdminActivity : DrawerActivity() {
    override val graphRes = R.navigation.nav_admin
    override val menuRes = R.menu.menu_admin_drawer
    override val roleName = "Administrador"
}
