# Guía de configuración (hazla antes de compilar)

El código ya está completo. Faltan **tus** credenciales de Firebase y ePayco. Sin ellas la app compila, pero no podrá iniciar sesión ni cobrar.

## 1. Firebase (≈10 min)
1. Entra a https://console.firebase.google.com → **Agregar proyecto** (ej. `andescafe`).
2. **Agregar app Android** con el nombre de paquete `com.andescafe`. Descarga `google-services.json` y **reemplaza** el archivo `app/google-services.json` (el actual es un marcador de posición).
3. **Authentication → Sign-in method →** activa **Correo electrónico/contraseña**.
4. **Firestore Database → Crear base de datos** (modo producción). En la pestaña **Reglas** pega el contenido de `firebase/firestore.rules` y publica.
5. **Storage:** no se usa (las fotos van en Firestore, así no necesitas el plan Blaze).
6. **Crear el primer administrador** (los admin no pueden auto-registrarse):
   1. Abre la app, toca **Regístrate** y crea una cuenta de comprador.
   2. En Firestore → colección `users` → documento de ese usuario → cambia el campo `role` a `admin`.
   3. Cierra sesión e ingresa de nuevo: verás el panel de administración.
   4. Desde allí crea los demás usuarios (vendedores/compradores/admins).
7. Para el **inicio de sesión con huella en dispositivo físico**: registra una huella en el celular (Ajustes → Seguridad), inicia sesión una vez con la casilla "Habilitar huella" marcada y luego usa **Ingresar con huella**.

## 2. Pagos con PayPal Sandbox (recomendado: no requiere número colombiano)
1. Entra a https://developer.paypal.com e inicia sesión con una cuenta PayPal normal (o crea una).
2. **Apps & Credentials → Sandbox → Create App** (nombre `AndesCafe`).
3. Copia el **Client ID** y el **Secret** en `data/Config.kt` (`PAYPAL_CLIENT_ID`, `PAYPAL_SECRET`). `PAYMENT_PROVIDER` ya está en `"paypal"`.
4. **Testing Tools → Sandbox Accounts**: usa la cuenta *Personal* de pruebas (correo y contraseña ahí) para aprobar pagos en la app.
5. El total se convierte de COP a USD con una tasa fija (`COP_PER_USD = 4000`), indícalo en el informe.
> El Secret dentro de la app solo es aceptable en Sandbox; en producción se usaría un backend.

## 2b. ePayco (opcional)
1. Crea una cuenta en https://dashboard.epayco.co y activa el modo **pruebas**.
2. Copia tu **llave pública** (Integraciones → Llaves API).
3. Pégala en `app/src/main/java/com/andescafe/tienda/data/Config.kt` en `EPAYCO_PUBLIC_KEY`. Mantén `EPAYCO_TEST_MODE = true` para las pruebas.
4. Usa las tarjetas de prueba que publica ePayco en su documentación (https://docs.epayco.co) para simular pagos aprobados, rechazados y pendientes.
5. Mientras la llave siga siendo `TU_LLAVE_PUBLICA_EPAYCO`, la app muestra un aviso y permite **simular** un pago solo para desarrollo. Para la entrega y el video debes tener tu llave configurada.

> Verifica en la documentación vigente de ePayco que los atributos del checkout (`data-epayco-*`) y el endpoint de validación `secure.epayco.co/validation/v1/reference/{ref_payco}` no hayan cambiado; si cambian, solo se ajusta `EpaycoActivity.kt`.

## 3. Compilar y probar
1. Abre la carpeta `AndesCafe` en Android Studio (Koala o superior, JDK 17) → *Sync*.
2. Conecta tu celular (depuración USB) y ejecuta. **El video de lanzamiento debe grabarse en el dispositivo físico**, no en emulador.
3. APK: *Build > Build APK(s)* → `app/build/outputs/apk/debug/app-debug.apk`.

## 4. GitHub
Tu repositorio ya existe. Desde la carpeta del proyecto:
```bash
git add .
git commit -m "feat: integración Firebase, CRUDs, geolocalización y pagos ePayco"
git push
```
Sugerencia de commits: `feat: autenticación y roles`, `feat: CRUD de productos con imágenes en Storage`, `feat: carrito y órdenes`, `feat: geolocalización`, `feat: pasarela ePayco`.

## 5. Seguridad
`google-services.json` contiene una API key de cliente (es normal subirla a un repo de estudio). En Google Cloud Console puedes restringirla al paquete `com.andescafe.tienda`. **No subas** claves privadas de ePayco (solo se usa la pública).
