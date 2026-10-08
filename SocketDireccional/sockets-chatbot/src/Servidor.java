import java.io.EOFException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servidor TCP de espera activa, SIN hilos: acepta un cliente, lo atiende
 * hasta que se desconecta y recién entonces acepta al siguiente.
 */
public class Servidor {

    static final int PUERTO_DEFECTO = 5000;
    static final int MAX_ARCHIVO = 5 * 1024 * 1024;
    static final Path CARPETA = Paths.get("comprobantes_recibidos");

    // ---- Datos de prueba: arreglo de productos y cantidades ----
    static final String[] PRODUCTOS   = {"Laptop", "Mouse", "Teclado", "Monitor", "Auriculares", "Webcam"};
    static final int[]    CANTIDADES  = {15,       120,     80,        22,        45,            0};

    // ---- Usuarios válidos para el comando "autenticar" ----
    static final Map<String, String> USUARIOS = new HashMap<>();
    static {
        USUARIOS.put("admin", "admin123");
        USUARIOS.put("alumno", "1234");
    }

    // ---- Frases precargadas del chatbot ----
    static final String MENU =
        "Menú de Soporte: 1. Estado de Envío | 2. Devoluciones | 3. Reclamos. "
      + "Por favor, introduzca un comando válido.";
    static final String AYUDA =
        "AYUDA - Preguntas base guiadas:\n"
      + "  1. ¿Cuál es el estado de mi envío?   -> escriba: estado de envío\n"
      + "  2. ¿Cómo hago una devolución?        -> escriba: devolución\n"
      + "  3. Quiero presentar un reclamo       -> escriba: reclamo\n"
      + "  4. ¿Qué medios de pago aceptan?      -> escriba: pagos\n"
      + "Comandos: autenticar | ayuda | ver stock <producto> | subir comprobante <ruta> | ver historial de chat | salir";

    public static void main(String[] args) {
        int puerto = (args.length > 0) ? Integer.parseInt(args[0]) : PUERTO_DEFECTO;
        Log log = new Log("SERVIDOR");

        Log.banner("SERVIDOR CHATBOT E-COMMERCE  |  TCP + AES-GCM");
        try (ServerSocket ss = new ServerSocket(puerto)) {
            String ip = "desconocida";
            try { ip = InetAddress.getLocalHost().getHostAddress(); } catch (Exception ignorada) { }
            log.info("Servidor iniciado en " + ip + ":" + puerto + "  (modo espera activa, 1 cliente a la vez)");
            log.info("Productos precargados: " + PRODUCTOS.length);

            while (true) {                                   // espera activa
                log.info("Esperando conexión de un cliente...");
                try (Socket socket = ss.accept()) {          // bloqueante
                    atender(socket, log);
                } catch (Exception e) {
                    log.error("Fallo al atender cliente: " + e.getMessage());
                }
                System.out.println();
            }
        } catch (Exception e) {
            log.error("No se pudo iniciar el servidor: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    static void atender(Socket socket, Log log) {
        String ip = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        log.conexion("Cliente conectado desde " + ip);

        boolean autenticado = false;
        String usuario = null;
        String nombre = "Cliente";

        try (Canal canal = new Canal(socket)) {
            boolean seguir = true;
            while (seguir) {
                String msg = canal.recibir();                       // espera mensaje (descifra)
                log.cifrado("<< " + Log.resumen(canal.ultimoCifrado));
                log.recibido(Log.describir(msg));

                int i = msg.indexOf('|');
                String tipo  = (i < 0) ? msg : msg.substring(0, i);
                String resto = (i < 0) ? ""  : msg.substring(i + 1);
                String resp;

                switch (tipo) {
                    case "CHAT": {
                        Matcher m = Pattern.compile("mi nombre es\\s+(.+)", Pattern.CASE_INSENSITIVE).matcher(resto);
                        if (m.find()) {
                            nombre = m.group(1).replaceAll("[.!]+$", "").trim();
                            resp = "Hola " + nombre + ", bienvenido al Soporte de Comercio Electrónico. "
                                 + "Escriba 'ayuda' para ver las preguntas base.";
                        } else {
                            resp = responderBot(resto);
                        }
                        break;
                    }
                    case "AUTH": {
                        int j = resto.indexOf('|');
                        String u = (j < 0) ? resto : resto.substring(0, j);
                        String p = (j < 0) ? ""    : resto.substring(j + 1);
                        if (p.equals(USUARIOS.get(u))) {
                            autenticado = true;
                            usuario = u;
                            log.info("Usuario autenticado: " + u);
                            resp = "Autenticación exitosa. Bienvenido, " + u + ".";
                        } else {
                            log.error("Autenticación fallida para usuario '" + u + "'");
                            resp = "ERROR: credenciales inválidas.";
                        }
                        break;
                    }
                    case "AYUDA":
                        resp = AYUDA;
                        break;
                    case "STOCK":
                        if (!autenticado) {
                            log.error("Acceso denegado a STOCK: cliente no autenticado");
                            resp = "ACCESO DENEGADO: use el comando 'autenticar' primero.";
                        } else {
                            resp = consultarStock(resto);
                        }
                        break;
                    case "SUBIR":
                        if (!autenticado) {
                            log.error("Acceso denegado a SUBIR: cliente no autenticado");
                            resp = "ACCESO DENEGADO: use el comando 'autenticar' primero.";
                        } else {
                            resp = guardarComprobante(resto, log);
                        }
                        break;
                    case "SALIR":
                        resp = "Sesión finalizada. Hasta luego, " + nombre + ".";
                        seguir = false;
                        break;
                    default:
                        resp = "ERROR: comando desconocido.";
                }

                canal.enviar(resp);                                  // cifra y envía
                log.enviado(resp);
                log.cifrado(">> " + Log.resumen(canal.ultimoCifrado));
            }
            log.desconexion("Cliente " + ip + " finalizó la sesión correctamente"
                    + (usuario != null ? " (usuario: " + usuario + ")" : ""));
        } catch (EOFException | SocketException e) {
            log.desconexion("Cliente " + ip + " cerró la conexión inesperadamente");
        } catch (Exception e) {
            log.error("Error en la sesión con " + ip + ": " + e);
        }
        log.info("Recursos liberados (streams y socket cerrados)");
    }

    // ------------------------------------------------------------------
    static String responderBot(String texto) {
        String t = normalizar(texto);
        if (t.equals("1") || t.contains("envio") || t.contains("seguimiento") || t.contains("rastre"))
            return "Estado de Envío: ingrese su número de pedido en la web para ver el seguimiento. "
                 + "Entregas en 3 a 5 días hábiles.";
        if (t.equals("2") || t.contains("devol") || t.contains("reembolso"))
            return "Devoluciones: tiene 30 días desde la recepción. El producto debe estar sin uso y en su caja original.";
        if (t.equals("3") || t.contains("reclamo") || t.contains("queja"))
            return "Reclamos: describa su caso y adjunte su comprobante con 'subir comprobante'. Respondemos en 48 hs.";
        if (t.contains("pedido") || t.contains("problema"))
            return MENU;
        if (t.contains("pago") || t.contains("tarjeta") || t.contains("precio"))
            return "Medios de pago: tarjetas de crédito/débito, transferencia bancaria y billeteras virtuales. Hasta 12 cuotas.";
        if (t.contains("gracias"))
            return "¡Gracias a usted! Estamos para ayudarlo.";
        if (t.contains("hola") || t.contains("buen"))
            return "¡Hola! Soy el asistente virtual de la tienda. Escriba 'ayuda' para ver cómo puedo asistirlo.";
        return "No comprendí su consulta. Por favor, introduzca un comando válido (escriba 'ayuda').";
    }

    static String consultarStock(String consulta) {
        String q = normalizar(consulta);
        if (q.isEmpty()) {
            StringBuilder sb = new StringBuilder("Inventario actual:");
            for (int i = 0; i < PRODUCTOS.length; i++) {
                sb.append("\n  ").append(String.format("%-12s", PRODUCTOS[i]))
                  .append(": ").append(CANTIDADES[i]).append(" unidades");
            }
            return sb.toString();
        }
        for (int i = 0; i < PRODUCTOS.length; i++) {
            String p = normalizar(PRODUCTOS[i]);
            if (p.contains(q) || q.contains(p)) {
                return CANTIDADES[i] > 0
                    ? "Stock de " + PRODUCTOS[i] + ": " + CANTIDADES[i] + " unidades disponibles."
                    : "Stock de " + PRODUCTOS[i] + ": AGOTADO (0 unidades).";
            }
        }
        return "Producto '" + consulta + "' no encontrado. Pruebe con: " + String.join(", ", PRODUCTOS) + ".";
    }

    static String guardarComprobante(String resto, Log log) {
        try {
            int j = resto.lastIndexOf('|');
            if (j < 0) return "ERROR: formato de archivo inválido.";
            String nombre = resto.substring(0, j);
            nombre = nombre.substring(Math.max(nombre.lastIndexOf('/'), nombre.lastIndexOf('\\')) + 1)
                           .replaceAll("[^A-Za-z0-9._-]", "_");
            if (nombre.isEmpty()) nombre = "comprobante.bin";

            byte[] datos = Base64.getDecoder().decode(resto.substring(j + 1));
            if (datos.length > MAX_ARCHIVO) return "ERROR: el archivo supera el máximo de 5 MB.";

            Files.createDirectories(CARPETA);
            Path destino = CARPETA.resolve(System.currentTimeMillis() + "_" + nombre);
            Files.write(destino, datos);

            log.archivo("Comprobante guardado: " + destino + "  (" + datos.length + " bytes)");
            return "Comprobante '" + nombre + "' recibido correctamente (" + datos.length + " bytes).";
        } catch (Exception e) {
            log.error("No se pudo guardar el comprobante: " + e.getMessage());
            return "ERROR: no se pudo guardar el comprobante.";
        }
    }

    static String normalizar(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase().trim();
    }
}
