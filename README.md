# AndesCafé – Tienda virtual Android (Frontend)

Proyecto Android Studio (Kotlin + XML, ViewBinding, Material Components). Interfaces para los roles **Administrador**, **Vendedor** y **Comprador** + pantallas transversales. Los datos son de ejemplo en memoria (`Repo.kt`).

## Cómo ejecutar
1. Abrir la carpeta en Android Studio (Hedgehog o superior, JDK 17) y esperar el *Gradle Sync*.
2. Run ▶ en un emulador/dispositivo (minSdk 24).
3. **APK:** *Build → Build Bundle(s) / APK(s) → Build APK(s)*. Queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Acceso de prueba
Cualquier correo válido y contraseña de 4+ caracteres; elegir el rol con los chips. La huella requiere un dispositivo/emulador con biometría configurada.

## Pantallas
| Rol | Pantallas |
|---|---|
| Transversal | Splash, Login (+biometría), Recuperar contraseña, Drawer / Bottom Navigation |
| Administrador | Dashboard, Usuarios (listar/crear/editar/eliminar), Productos, Reporte de ventas |
| Vendedor | Productos (CRUD), Pedidos recibidos, Perfil |
| Comprador | Inicio, Catálogo (búsqueda), Detalle, Carrito, Pasarela de pagos, Perfil |

## Recursos personalizados (`res/`)
`values/` colors (paleta café/caramelo), themes, styles, dimens, strings · `drawable/` shapes, gradientes y vectores · `color/` selector · `menu/` un menú por rol · `layout/` 1 XML por pantalla.
