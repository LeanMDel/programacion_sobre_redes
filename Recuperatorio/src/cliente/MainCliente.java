package cliente;
import cifrado.Cifrado;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainCliente {

    private static final String PROMPT = Cifrado.CIAN + "vos> " + Cifrado.RESET;

    public static void main(String[] args) {

        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
        AtomicBoolean cerrando = new AtomicBoolean(false);

        System.out.println(Cifrado.AMARILLO + "====================================\n"
                + "           CLIENTE DE CHAT\n"
                + "====================================" + Cifrado.RESET);

        try {
            System.out.print(Cifrado.AMARILLO + "Ingrese IP: " + Cifrado.RESET);
            String ip = teclado.readLine().trim();

            System.out.print(Cifrado.AMARILLO + "Ingrese puerto: " + Cifrado.RESET);
            int puerto = Integer.parseInt(teclado.readLine().trim());

            try (Socket socket = new Socket(ip, puerto)) {
                BufferedReader entrada = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter salida = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

                mostrarSistema("Conectado a " + ip + ":" + puerto);
                mostrarSistema("Comandos: /hi  |  /logout  |  texto libre = chat");

                // HILO 1: escucha al servidor en segundo plano
                Thread receptor = new Thread(() -> {
                    try {
                        String linea;
                        while ((linea = entrada.readLine()) != null) {
                            // La bienvenida llega en plano: si no se puede desencriptar, se muestra igual
                            String texto = Cifrado.decriptarOPlano(Cifrado.CLAVE, linea);
                            
                            // Ping-pong silencioso: responde solo, sin mostrar nada al usuario
                            
                            if (texto.trim().equals("/PING_CHECK")){
                            	salida.println(Cifrado.encriptar(Cifrado.CLAVE, "/PONG_ALIVE"));
                            	continue;
                            }
                            
                            System.out.println("\n" + Cifrado.MORADO + "[SERVIDOR]\t"
                                    + Cifrado.RESET + texto);
                            System.out.print(PROMPT);
                        }
                    } catch (IOException e) {
                        // Si el socket se cerro, se corta el bucle
                    }

                    if (!cerrando.get()) {
                        System.out.println("\n" + Cifrado.ROJO
                                + "[SISTEMA] El servidor cerro la conexion." + Cifrado.RESET);
                    }
                }, "RECEPCION");
                receptor.start();

                // HILO 2 (main): espera lo que escribe el usuario
                System.out.print(PROMPT);
                String mensaje;
                while ((mensaje = teclado.readLine()) != null) {
                    mensaje = mensaje.trim();

                    if (mensaje.isEmpty()) {
                        System.out.print(PROMPT);
                        continue;
                    }

                    // Todo lo que se envia va encriptado: IV_Base64:Mensaje_Base64
                    salida.println(Cifrado.encriptar(Cifrado.CLAVE, mensaje));

                    if (mensaje.equalsIgnoreCase("/logout")) {
                        break;
                    }
                    System.out.print(PROMPT);
                }

                cerrando.set(true);
                socket.close();     // libera al hilo bloqueado en readLine()
                receptor.join();
                mostrarSistema("Desconectado del chat.");
            }
        } catch (NumberFormatException e) {
            mostrarSistema("El puerto tiene que ser un numero.");
        } catch (IOException e) {
            System.out.println(Cifrado.ROJO + "[SISTEMA] Error de conexion: "
                    + e.getMessage() + Cifrado.RESET);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void mostrarSistema(String texto) {
        System.out.println(Cifrado.AMARILLO + "[SISTEMA]\t" + texto + Cifrado.RESET);
    }
}
