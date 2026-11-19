package com.andescafe.tienda.data

/** Configuración de la pasarela de pagos ePayco. Reemplaza la llave por la de tu cuenta (Panel ePayco > Integraciones). */
object Config {
    /** Pasarela activa: "paypal" (sandbox, sin requisitos de Colombia) o "epayco". */
    const val PAYMENT_PROVIDER = "paypal"

    /** PayPal Sandbox: credenciales de tu app en https://developer.paypal.com (Apps & Credentials > Sandbox). */
    const val PAYPAL_CLIENT_ID = "TU_CLIENT_ID_PAYPAL_SANDBOX"
    const val PAYPAL_SECRET = "TU_SECRET_PAYPAL_SANDBOX"
    const val PAYPAL_API = "https://api-m.sandbox.paypal.com"
    /** PayPal no maneja COP: el total se convierte a USD con esta tasa fija (documentada en el informe). */
    const val COP_PER_USD = 4000.0
    const val PAYPAL_RETURN_URL = "https://andescafe.app/paypal-ok"
    const val PAYPAL_CANCEL_URL = "https://andescafe.app/paypal-cancel"

    const val EPAYCO_PUBLIC_KEY = "TU_LLAVE_PUBLICA_EPAYCO"
    const val EPAYCO_TEST_MODE = true
    /** URL de respuesta: la app intercepta la redirección y nunca la carga realmente. */
    const val EPAYCO_RESPONSE_URL = "https://andescafe.app/respuesta-pago"
}
