# Chatbot e-commerce con Sockets TCP (Java)

Sistema cliente-servidor **sin hilos**, con comunicación **cifrada (AES-128-GCM)**, logs ANSI y chatbot de soporte.

## Archivos

| Archivo | Rol |
|---|---|
| `Servidor.java` | `ServerSocket` en espera activa; atiende **un cliente a la vez**. Chatbot, stock, autenticación, recepción de comprobantes |
| `Cliente.java` | `Socket` + consola con el catálogo de comandos |
| `Canal.java` | Envuelve el socket: cifra al enviar, descifra al recibir, cierra streams y socket |
| `Cripto.java` | AES-GCM con clave precompartida (debe ser igual en ambos extremos) |
| `Log.java` | Log con colores ANSI y columnas alineadas por tabulaciones |

## Compilar

```bash
javac -encoding UTF-8 *.java
```

## Ejecutar (dos computadoras en la misma red)

**PC 1 (servidor):**
```bash
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 Servidor 5000
```
El servidor imprime su IP (ej. `192.168.1.20`). Si el cliente no conecta, abrí el puerto 5000 en el firewall de esa PC.

**PC 2 (cliente):**
```bash
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 Cliente 192.168.1.20 5000
```
Al iniciar pide **nombre y apellido** y lo envía como primer mensaje (`Hola, mi nombre es ...`), así queda impreso en **ambas** consolas.

> **Windows:** usá **Windows Terminal** o PowerShell (el `cmd` clásico puede no mostrar los colores). Si ves caracteres raros, ejecutá `chcp 65001` antes de lanzar el programa.

## Comandos del cliente

| Comando | Qué hace |
|---|---|
| `autenticar` | Pide usuario y contraseña. Válidos: `alumno / 1234` y `admin / admin123` |
| `ayuda` | Muestra el menú de preguntas base guiadas |
| `ver stock <producto>` | Consulta existencias (requiere autenticarse). Sin producto lista todo el inventario |
| `subir comprobante <ruta>` | Envía un archivo al servidor; se guarda en `comprobantes_recibidos/` (requiere autenticarse) |
| `ver historial de chat` | Muestra los mensajes de la sesión |
| `salir` | Cierra la sesión y libera streams y sockets |
| *(texto libre)* | Lo responde el chatbot con frases precargadas |

Ejemplo: `Hola, tengo un problema con mi pedido` → `Menú de Soporte: 1. Estado de Envío | 2. Devoluciones | 3. Reclamos. Por favor, introduzca un comando válido.`

Productos de prueba: Laptop (15), Mouse (120), Teclado (80), Monitor (22), Auriculares (45), Webcam (0).

## Colores del log

| Color | Evento |
|---|---|
| Verde | Conexión exitosa |
| Cian | Mensaje enviado |
| Amarillo | Mensaje/comando recibido |
| Gris | Trama cifrada (Base64) en tránsito |
| Magenta | Transferencia de archivos |
| Azul | Información |
| Rojo | Errores y desconexiones |

## Capturas para el Classroom (NO subir al GitHub)

1. Una captura de la consola del servidor y otra de la del cliente, de **dos computadoras distintas**.
2. En ambas debe verse la línea `Hola, mi nombre es <Nombre Apellido>`.
3. Mostrá una sesión completa: saludo, `ayuda`, `autenticar`, `ver stock`, `subir comprobante`, `ver historial de chat` y `salir`.
4. Opcional: un intento sin autenticar (aparece en rojo) y cerrar el cliente con Ctrl+C para ver el log de desconexión del servidor.
