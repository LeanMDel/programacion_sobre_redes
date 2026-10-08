import java.io.EOFException;
import java.io.File;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

/**
 * Cliente TCP por consola, SIN hilos. Flujo: leer comando -> enviar -> esperar respuesta.
 * Uso: java Cliente [host] [puerto]
 */
public class Cliente {

    static final int MAX_ARCHIVO = 5 * 1024 * 1024;
    static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : "localhost";
        int puerto = (args.length > 1) ? Integer.parseInt(args[1]) : 5000;

        Log log = new Log("CLIENTE");
        Scanner sc = new Scanner(System.in);
        List<String> historial = new ArrayList<>();

        Log.banner("CLIENTE CHATBOT E-COMMERCE  |  TCP + AES-GCM");
        System.out.print("Ingrese su nombre y apellido: ");
        String alumno = sc.hasNextLine() ? sc.nextLine().trim() : "";
        if (alumno.isEmpty()) alumno = "Alumno";

        Socket socket = null;
        Canal canal = null;
        try {
            log.info("Conectando a " + host + ":" + puerto + " ...");
            socket = new Socket(host, puerto);
            canal = new Canal(socket);
            log.conexion("Conexión establecida con " + host + ":" + puerto);
            log.info("Canal cifrado activo (AES-128-GCM)");

            // Saludo inicial: el nombre del alumno queda impreso en ambas consolas
            String saludo = "Hola, mi nombre es " + alumno;
            intercambiar(canal, log, historial, "CHAT|" + saludo, saludo);

            boolean activo = true;
            while (activo) {
                System.out.print(Log.CIAN + Log.NEGRITA + "\n" + alumno + " > " + Log.RESET);
                String linea = sc.hasNextLine() ? sc.nextLine().trim() : "salir";
                if (linea.isEmpty()) continue;
                String l = linea.toLowerCase();

                if (l.equals("autenticar")) {
                    System.out.print("  Usuario: ");
                    String u = sc.nextLine().trim();
                    System.out.print("  Contraseña: ");
                    String p = sc.nextLine();
                    intercambiar(canal, log, historial, "AUTH|" + u + "|" + p, "autenticar (usuario " + u + ")");

                } else if (l.equals("ayuda")) {
                    intercambiar(canal, log, historial, "AYUDA", "ayuda");

                } else if (l.startsWith("ver stock")) {
                    String producto = linea.substring("ver stock".length()).trim();
                    intercambiar(canal, log, historial, "STOCK|" + producto, linea);

                } else if (l.startsWith("subir comprobante")) {
                    String ruta = linea.substring("subir comprobante".length()).trim().replace("\"", "");
                    if (ruta.isEmpty()) {
                        System.out.print("  Ruta del archivo: ");
                        ruta = sc.nextLine().trim().replace("\"", "");
                    }
                    File f = new File(ruta);
                    if (!f.isFile()) {
                        log.error("Archivo no encontrado: " + ruta);
                    } else if (f.length() > MAX_ARCHIVO) {
                        log.error("El archivo supera el máximo de 5 MB");
                    } else {
                        byte[] datos = Files.readAllBytes(f.toPath());
                        log.archivo("Preparando envío de '" + f.getName() + "' (" + datos.length + " bytes)");
                        String b64 = Base64.getEncoder().encodeToString(datos);
                        intercambiar(canal, log, historial,
                                "SUBIR|" + f.getName().replace("|", "_") + "|" + b64,
                                "subir comprobante " + f.getName());
                    }

                } else if (l.equals("ver historial de chat")) {
                    mostrarHistorial(log, historial);

                } else if (l.equals("salir")) {
                    intercambiar(canal, log, historial, "SALIR", "salir");
                    activo = false;

                } else {
                    intercambiar(canal, log, historial, "CHAT|" + linea, linea);
                }
            }

        } catch (ConnectException e) {
            log.error("No se pudo conectar a " + host + ":" + puerto + " (¿servidor apagado, IP o firewall?)");
        } catch (UnknownHostException e) {
            log.error("Host desconocido: " + host);
        } catch (EOFException | SocketException e) {
            log.desconexion("El servidor cerró la conexión");
        } catch (Exception e) {
            log.error("Error inesperado: " + e);
        } finally {
            log.info("Cerrando streams y socket...");
            if (canal != null) {
                canal.close();                    // cierra in, out y socket
            } else if (socket != null) {
                try { socket.close(); } catch (Exception ignorada) { }
            }
            log.desconexion("Conexión cerrada y recursos de red liberados");
            sc.close();
        }
    }

    /** Envía un mensaje cifrado, espera la respuesta y registra todo en el log. */
    static void intercambiar(Canal canal, Log log, List<String> historial,
                             String mensaje, String visible) throws Exception {
        log.enviado(Log.describir(mensaje));
        canal.enviar(mensaje);
        log.cifrado(">> " + Log.resumen(canal.ultimoCifrado));
        historial.add(hora() + "  Yo       : " + visible);

        String respuesta = canal.recibir();
        log.cifrado("<< " + Log.resumen(canal.ultimoCifrado));
        log.recibido(respuesta);
        historial.add(hora() + "  Servidor : " + respuesta.replace("\n", " "));
    }

    static void mostrarHistorial(Log log, List<String> historial) {
        if (historial.isEmpty()) {
            log.info("El historial de chat está vacío");
            return;
        }
        StringBuilder sb = new StringBuilder("Historial de chat (" + historial.size() + " mensajes):");
        for (String h : historial) sb.append("\n  ").append(h);
        log.info(sb.toString());
    }

    static String hora() {
        return LocalTime.now().format(HORA);
    }
}
