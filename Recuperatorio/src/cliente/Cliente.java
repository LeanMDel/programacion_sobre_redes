package cliente;
import servidor.Servidor;
import cifrado.Cifrado;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Un objeto Cliente = un cliente conectado al SERVIDOR (corre en su propio Thread)
public class Cliente implements Runnable {

    public static final String BIENVENIDA = "BIENVENIDO AL SERVIDOR LOCAL";

    private final Socket socket;
    private final Servidor servidor;
    private final BufferedReader entrada;
    private final PrintWriter salida;
    private final String id;
    private final AtomicBoolean conectado = new AtomicBoolean(true);
    
    // Ping-Pong
    
    private volatile boolean pongRecibido = true;
    private ScheduledExecutorService reloj;

    public Cliente(Socket socket, Servidor servidor) throws IOException {
        this.socket = socket;
        this.servidor = servidor;
        this.id = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        this.entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.salida = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

        // El primer mensaje (bienvenida) SIEMPRE va en texto plano, sin encriptar.
        // Se manda aca (antes de entrar a la lista) para que ningun broadcast llegue primero.
        salida.println(BIENVENIDA);
    }

    @Override
    public void run() {
        System.out.println(Cifrado.VERDE + "\n[CONECTADO]\t" + id + Cifrado.RESET);
        iniciarPing();

        try {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                // Si viene encriptado se desencripta; si viene en plano se usa tal cual
                String mensaje = Cifrado.decriptarOPlano(Cifrado.CLAVE, linea).trim();

                if (mensaje.isEmpty()) {
                    continue;
                }
                
                // Respuesta al ping: se marca y se descarta (sin mostrar ni difundir)
                
                if (mensaje.equals("/PONG_ALIVE")) {
                	pongRecibido = true;
                	continue;
                }
                
                System.out.println(Cifrado.CIAN + "\t[" + id + "] " + Cifrado.RESET + mensaje);

                if (mensaje.startsWith("/")) {
                    if (!procesarComando(mensaje)) {
                        break; // /logout
                    }
                } else {
                    servidor.difundir(mensaje); // chat: le llega a todos
                }
            }
        } catch (IOException e) {
            // Conexion caida: se limpia en el finally
        } finally {
            cerrar();
        }
    }
    
    
    private void iniciarPing() {
    	reloj = Executors.newSingleThreadScheduledExecutor();
    	reloj.scheduleAtFixedRate(() ->{
    		pongRecibido = false;
    		enviar("/PING_CHECK");
    		reloj.schedule(() -> {
    			if (!pongRecibido) {
    				System.out.println(Cifrado.ROJO + "\n[EXPULSADO]\t" + id
    						+ " (sin /PONG_ALIVE en 5 s)" + Cifrado.RESET);
    				cerrar();
    								
    			}
    		} , 5 , TimeUnit.SECONDS);
    	}, 15, 15, TimeUnit.SECONDS);
    }
    

    // Devuelve false si hay que cortar la conexion con este cliente
    private boolean procesarComando(String mensaje) {
        String comando = mensaje.toLowerCase();

        if (comando.equals("/hi")) {
            enviar(BIENVENIDA);
            return true;
        }

        if (comando.equals("/logout")) {
            return false;
        }

        enviar("COMANDO DESCONOCIDO");
        return true;
    }

    // synchronized: varios hilos (otros clientes) pueden mandarle mensajes a la vez
    public synchronized void enviar(String texto) {
        if (!conectado.get()) {
            return;
        }

        String paquete = Cifrado.encriptar(Cifrado.CLAVE, texto);
        if (paquete == null) {
            return;
        }

        salida.println(paquete);
        if (salida.checkError()) { // PrintWriter no lanza excepciones: se consulta
            cerrar();
        }
    }

    public void cerrar() {
        if (!conectado.compareAndSet(true, false)) {
            return;
        }

        try {
            socket.close();
        } catch (IOException e) {
            // ya estaba cerrado
        }

        servidor.quitar(this);
        System.out.println(Cifrado.AMARILLO + "\n[DESCONECTADO]\t" + id + Cifrado.RESET);
    }
}
