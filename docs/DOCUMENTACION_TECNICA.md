# Documento técnico definitivo — AndesCafé

**Estudiante:** Sergio Augusto González · **Institución:** Universidad Compensar
**Programa:** Desarrollo de aplicaciones móviles nativas · **Actividad:** 5 · Aplicativo funcional
**Repositorio:** _(pega aquí el enlace público de GitHub)_

## 1. Descripción
AndesCafé es una tienda virtual Android nativa (Kotlin + XML) que conecta compradores con vendedores de café colombiano. Un administrador gestiona usuarios y productos y consulta las ventas. Esta versión integra la lógica completa sobre las interfaces de la Actividad 4.

## 2. Correcciones de actividades anteriores
El docente no entregó observaciones específicas sobre las Actividades 1 y 2, por lo que no hay correcciones puntuales que registrar. Respecto a la Actividad 4 (interfaces con datos de ejemplo) se hicieron estos cambios en esta versión:

| # | Cambio | Detalle |
|---|---|---|
| 1 | Datos reales en lugar de datos fijos | Las pantallas ahora leen y escriben en Firestore en tiempo real |
| 2 | Roles validados en el servidor | El rol se lee de Firestore y las reglas de seguridad impiden que un usuario se asigne rol de administrador |
| 3 | Imágenes de producto | El vendedor toma la foto con la cámara o la elige de la galería |
| 4 | Entrega con ubicación | El comprador puede usar su ubicación actual y el vendedor la abre en Google Maps |

## 3. Requerimientos y cobertura
| # | Requerimiento de la actividad | Implementación | Archivos |
|---|---|---|---|
| 1 | Autenticación: registro, login, logout y recuperación para los 3 roles | Firebase Authentication (correo/contraseña); el rol se lee de Firestore | `RegisterActivity`, `LoginActivity`, `ForgotPasswordActivity`, `Repo` |
| 2 | Biometría con huella | `BiometricPrompt`; credenciales cifradas con `EncryptedSharedPreferences` | `LoginActivity`, `SecureStore` |
| 3 | CRUD de usuarios (Admin) | Firestore `users`, en tiempo real; alta con segunda instancia de `FirebaseApp` | `AdminUsersFragment`, `AdminUserFormFragment` |
| 4 | CRUD de productos con imagen cámara/galería | Firestore `products`; la imagen se reduce a 640 px, se comprime (JPEG 70 %) y se guarda como cadena base64 en el campo `imageUrl` (sin Firebase Storage) | `SellerProductFormFragment`, `SellerProductsFragment` |
| 5 | CRUD de carrito y órdenes con historial | `carts/{uid}/items` y `orders`; batch atómico al comprar | `BuyerDetailFragment`, `BuyerCartFragment`, `BuyerPaymentFragment`, `BuyerOrdersFragment` |
| 6 | Geolocalización asociada a la entrega | `FusedLocationProviderClient` + `Geocoder`; lat/lng guardadas en la orden; el vendedor abre Google Maps | `BuyerPaymentFragment`, `SellerOrdersFragment` |
| 7 | Pasarela de pagos con confirmación | Pantalla de pago con confirmación de transacción. **El pago está simulado** (sin cobro real): el código incluye `PaypalActivity` (PayPal Sandbox) y `EpaycoActivity` listos para activarse con credenciales en `Config.kt` | `PaypalActivity`, `EpaycoActivity`, `BuyerPaymentFragment` |

## 4. Arquitectura
- **Capas:** UI (Activities/Fragments + XML) → `Repo` (acceso a Firebase) → Firebase (Auth, Firestore).
- **Navegación:** un `Activity` por rol y un grafo de Navigation Component por rol (`nav_admin`, `nav_seller`, `nav_buyer`). Admin y Vendedor usan Drawer (`DrawerActivity`); Comprador usa Bottom Navigation.
- **Tiempo real:** `Fragment.listen()` (en `Util.kt`) registra un `SnapshotListener` y lo elimina al destruirse la vista.
- **Paquetes:** `common` (login, registro, utilidades), `data` (modelos, repositorio, configuración), `admin`, `seller`, `buyer`.

## 5. Modelo de datos (Firestore)
| Colección | Campos principales |
|---|---|
| `users/{uid}` | name, email, role (`admin`/`seller`/`buyer`), phone, address, active |
| `products/{id}` | name, description, price, stock, imageUrl, sellerId, sellerName |
| `carts/{uid}/items/{productId}` | productId, name, price, qty, imageUrl, sellerId |
| `orders/{id}` | buyerId, buyerName, sellerIds[], items[], total, address, lat, lng, paymentRef, status, createdAt |

Estados de la orden: `Pagado`, `Pago pendiente`, `Enviado`, `Entregado`.

## 6. Flujos principales
**Inicio de sesión:** Splash → (sesión activa → rol) o Login → `signInWithEmailAndPassword` → se carga `users/{uid}` → si existe y está activo se abre la Activity del rol; si no, se cierra la sesión.
**Huella:** el usuario marca "Habilitar huella" al iniciar sesión → credenciales cifradas (AES-256-GCM) → en los siguientes ingresos `BiometricPrompt` valida la huella y se inicia sesión con ellas.
**Compra:** Catálogo → Detalle (valida stock) → Carrito → Pago (dirección + ubicación) → pago (simulado) → si es aprobado o pendiente se crea la orden, se descuenta el stock y se vacía el carrito en un `WriteBatch`; si es rechazado o cancelado no se crea.
**Imágenes:** cámara (`TakePicture` + `FileProvider`) o galería (`GetContent`) → se redimensiona y comprime → data URI base64 guardada en `products/{id}.imageUrl` → Glide la muestra.

## 7. Seguridad
- Reglas de Firestore en `firebase/firestore.rules` (rol obtenido del perfil; el auto-registro no puede crear administradores; cada usuario solo edita su perfil sin cambiar rol; el carrito es privado; los vendedores solo editan sus productos; el comprador solo puede modificar `stock` de un producto).
- Contraseñas gestionadas por Firebase Auth; las credenciales de la huella se guardan cifradas.
- Permisos: `INTERNET`, `USE_BIOMETRIC`, `ACCESS_FINE_LOCATION` (solicitado en tiempo de ejecución).

## 8. Interfaz
Paleta propia (café/dorado), tema Material 3 personalizado, estilos reutilizables, drawables (`shape`, `selector`, vectores) y recursos en `values/` (ver Actividad 4).

## 9. Limitaciones conocidas
- Eliminar un usuario borra su perfil de Firestore (queda sin acceso a la app), pero la cuenta de Authentication solo puede borrarse desde la consola o un backend con Admin SDK.
- La confirmación de pago se valida desde la app contra el proveedor; en producción se recomienda además un webhook de confirmación en servidor.
- Las imágenes se guardan dentro del documento de Firestore (límite de 1 MiB por documento); por eso se reducen a 640 px. En producción se recomienda Firebase Storage (requiere plan Blaze).
- La pasarela de pagos está en modo simulado: no se conecta a un proveedor real porque no se configuraron credenciales. Para activar PayPal Sandbox o ePayco basta con completar las claves en `Config.kt`.

## 10. Pruebas realizadas
Probadas en un celular Android físico con la APK de depuración:

| Prueba | Resultado |
|---|---|
| Registro de comprador (correo y contraseña) | Correcto; abre el perfil del comprador |
| Registro de vendedor | Correcto; abre el menú del vendedor |
| Inicio de sesión con correo y contraseña | Correcto |
| Recuperación de contraseña | Correcto; la app informa el envío del enlace |
| Crear producto (vendedor) | Correcto; se guarda en Firestore y aparece en "Mis productos" |
| Catálogo, detalle y carrito (comprador) | Correcto; el producto creado aparece al comprador y se agrega al carrito |
| Ubicación de entrega | Correcto; solicita el permiso y captura latitud y longitud |
| Compra y confirmación | Correcto (pago simulado); registra el pedido con estado Pagado y aparece en "Mis pedidos" |
| Persistencia en Firestore | Las cuentas creadas quedan en la colección `users` |

_(Pendiente de verificar en dispositivo: huella y CRUD de usuarios desde el administrador.)_

## 11. Versionamiento
Repositorio público en GitHub con commits descriptivos por funcionalidad. Todas las clases y métodos relevantes llevan comentarios KDoc.
