package servidor;
import cifrado.Cifrado;
import cliente.Cliente;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Servidor {

    private final int puerto;
    // Lista de clientes conectados (segura para usar desde varios hilos)
    private final List<Cliente> clientes = new CopyOnWriteArrayList<>();

    public Servidor(int puerto) {
        this.puerto = puerto;
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println(Cifrado.VERDE + "[SERVIDOR] Escuchando en el puerto "
                    + puerto + Cifrado.RESET);

            while (true) {
                Socket socket = serverSocket.accept();
                registrar(socket);
            }
        } catch (IOException e) {
            System.out.println(Cifrado.ROJO + "[SERVIDOR] No se pudo iniciar/seguir: "
                    + e.getMessage() + Cifrado.RESET);
        }
    }

    private void registrar(Socket socket) {
        try {
            Cliente cliente = new Cliente(socket, this);
            clientes.add(cliente);
            new Thread(cliente, "CLIENTE-" + socket.getPort()).start();
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException ignorada) {
                // nada que hacer
            }
        }
    }

    // Reenvia el mensaje a TODOS los conectados, incluido quien lo envio
    public void difundir(String mensaje) {
        for (Cliente cliente : clientes) {
            cliente.enviar(mensaje);
        }
    }

    public void quitar(Cliente cliente) {
        clientes.remove(cliente);
    }
}
