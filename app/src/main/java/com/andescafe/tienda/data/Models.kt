package com.andescafe.tienda.data

/** Perfil de usuario almacenado en la colección "users" (el id del documento es el uid de Firebase Auth). */
data class UserProfile(
    val uid: String = "", val name: String = "", val email: String = "",
    val role: String = "buyer",          // "admin" | "seller" | "buyer"
    val phone: String = "", val address: String = "", val active: Boolean = true
)

/** Producto publicado por un vendedor (colección "products"). La imagen vive en Firebase Storage. */
data class Product(
    val id: String = "", val name: String = "", val description: String = "",
    val price: Long = 0, val stock: Long = 0, val imageUrl: String = "",
    val sellerId: String = "", val sellerName: String = ""
)

/** Ítem del carrito: carts/{uid}/items/{productId}. */
data class CartItem(
    val productId: String = "", val name: String = "", val price: Long = 0,
    val qty: Long = 1, val imageUrl: String = "", val sellerId: String = ""
)

/** Orden de compra (colección "orders"). Guarda la ubicación de entrega y la referencia de pago de ePayco. */
data class Order(
    val id: String = "", val buyerId: String = "", val buyerName: String = "",
    val sellerIds: List<String> = emptyList(),
    val items: List<Map<String, Any>> = emptyList(),
    val total: Long = 0, val address: String = "",
    val lat: Double = 0.0, val lng: Double = 0.0,
    val paymentRef: String = "", val status: String = "", val createdAt: Long = 0
)
