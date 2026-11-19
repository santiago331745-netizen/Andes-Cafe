# AndesCafé · Tienda virtual Android (Actividad 5: aplicativo funcional)

App Android nativa (Kotlin + XML) con roles **Administrador**, **Vendedor** y **Comprador**, respaldada por **Firebase** (Auth, Firestore, Storage), geolocalización de Google y pago simulado (PayPal Sandbox o ePayco listos para configurar).

**Autor:** Sergio Augusto González · Universidad Compensar

## Puesta en marcha
Sigue `docs/GUIA_CONFIGURACION.md` (Firebase, ePayco, primer administrador).

## Funcionalidades
- Registro, login, logout y recuperación de contraseña · login con huella
- CRUD de usuarios (admin) y de productos con foto de cámara/galería (vendedor)
- Carrito, órdenes e historial · geolocalización de la entrega · pago con ePayco
- Reglas de seguridad en `firebase/`

## Documentos
`docs/DOCUMENTACION_TECNICA.md` · `docs/MANUAL_USUARIO.md` · `docs/VIDEO_LANZAMIENTO.md`

## Requisitos
Android Studio Koala+ · JDK 17 o 21 · minSdk 24 · compileSdk 34
