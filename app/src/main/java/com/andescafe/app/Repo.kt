package com.andescafe.app

import java.util.Locale

data class Product(var id: Int, var name: String, var price: Int, var desc: String, var category: String, var stock: Int)
data class User(var id: Int, var name: String, var email: String, var role: String)
data class Order(val id: Int, val customer: String, val items: Int, val total: Int, val status: String)
data class CartItem(val product: Product, var qty: Int)

/** Datos de ejemplo en memoria (sin backend): esta actividad solo evalúa el frontend. */
object Repo {
    var role = "Comprador"
    var userName = "Usuario"
    var userEmail = "demo@andescafe.com"
    private var pid = 7
    private var uid = 5
    private var oid = 1004

    val products = mutableListOf(
        Product(1, "Café Huila Premium", 32000, "Notas de panela y cacao. Molido medio, 500 g.", "Café", 40),
        Product(2, "Café Nariño Especial", 36000, "Acidez brillante y notas cítricas. 500 g.", "Café", 25),
        Product(3, "Café Sierra Nevada", 34000, "Cuerpo cremoso y final dulce. 500 g.", "Café", 30),
        Product(4, "Cápsulas Andes x10", 18000, "Cápsulas compatibles, tueste oscuro.", "Cápsulas", 60),
        Product(5, "Prensa Francesa 600 ml", 75000, "Vidrio borosilicato y acero inoxidable.", "Accesorios", 12),
        Product(6, "Molinillo Manual", 98000, "Muelas cerámicas ajustables.", "Accesorios", 8)
    )
    val users = mutableListOf(
        User(1, "Laura Gómez", "admin@andescafe.com", "Administrador"),
        User(2, "Carlos Ruiz", "carlos@andescafe.com", "Vendedor"),
        User(3, "Ana Torres", "ana@gmail.com", "Comprador"),
        User(4, "Mateo Díaz", "mateo@gmail.com", "Comprador")
    )
    val orders = mutableListOf(
        Order(1001, "Ana Torres", 2, 68000, "Entregado"),
        Order(1002, "Mateo Díaz", 1, 98000, "Enviado"),
        Order(1003, "Ana Torres", 3, 86000, "Pendiente")
    )
    val cart = mutableListOf<CartItem>()

    fun nextProductId() = pid++
    fun nextUserId() = uid++
    fun nextOrderId() = oid++
    fun cartTotal() = cart.sumOf { it.product.price * it.qty }
    fun addToCart(p: Product, qty: Int) {
        val ex = cart.firstOrNull { it.product.id == p.id }
        if (ex != null) ex.qty += qty else cart.add(CartItem(p, qty))
    }
    fun money(v: Int): String = "$ " + String.format(Locale("es", "CO"), "%,d", v)
}
