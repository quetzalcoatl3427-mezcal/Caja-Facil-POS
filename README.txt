CAJA FÁCIL POS — PROTOTIPO ANDROID

Este proyecto genera una APK de prueba mediante GitHub Actions.

Incluye:
- Catálogo de productos con foto seleccionada del teléfono.
- Inventario básico, carrito, cobro en efectivo y cambio.
- Historial/corte local y compartir ticket.
- Respaldo e importación/exportación JSON.
- Interfaz web dentro de una aplicación Android.

IMPORTANTE: esta versión todavía NO es la versión final multiusuario.
Los datos se guardan localmente en el dispositivo. No hay sincronización real entre
teléfonos/computadoras, ni inicio de sesión seguro, ni administración de empleados
protegida por servidor. No la uses como caja de producción ni guardes datos sensibles.

Para la versión final se debe integrar Firebase Authentication y Cloud Firestore
con reglas de seguridad que bloqueen por servidor la creación de usuarios a todos
excepto al propietario autorizado. Ocultar un botón en la pantalla NO es seguridad.
El propietario será el único que cree/desactive cuentas y asigne permisos; no habrá
registro público de empleados. También debe añadirse una cola de ventas offline y
sincronización al recuperar internet.

COMPILAR:
1. Sube el contenido de esta carpeta a un repositorio de GitHub.
2. Abre la pestaña Actions/Acciones.
3. Ejecuta el flujo "Build Caja Fácil POS APK" con Run workflow/Ejecutar flujo.
4. Cuando termine en verde, abre la ejecución y descarga el artefacto
   "Caja-Facil-POS-debug-apk".
5. Descomprime el ZIP descargado e instala app-debug.apk en Android.
