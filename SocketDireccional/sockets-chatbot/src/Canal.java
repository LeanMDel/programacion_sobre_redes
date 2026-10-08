import java.io.*;
import java.net.Socket;
import java.util.Base64;

/**
 * Envuelve un Socket TCP. Todo mensaje se cifra antes de enviarse y se
 * descifra al recibirse. Trama: [int longitud][bytes cifrados].
 */
public class Canal implements Closeable {

    private static final int MAX_BYTES = 10 * 1024 * 1024;

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    /** Último mensaje cifrado (Base64) enviado o recibido; útil para el log. */
    public String ultimoCifrado = "";

    public Canal(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    public void enviar(String texto) throws Exception {
        byte[] cifrado = Cripto.cifrar(texto);
        ultimoCifrado = Base64.getEncoder().encodeToString(cifrado);
        out.writeInt(cifrado.length);
        out.write(cifrado);
        out.flush();
    }

    /** Bloquea hasta recibir un mensaje. Lanza EOFException si el otro extremo se desconecta. */
    public String recibir() throws Exception {
        int n = in.readInt();
        if (n <= 0 || n > MAX_BYTES) {
            throw new IOException("Tamaño de trama inválido: " + n);
        }
        byte[] cifrado = new byte[n];
        in.readFully(cifrado);
        ultimoCifrado = Base64.getEncoder().encodeToString(cifrado);
        return Cripto.descifrar(cifrado);
    }

    /** Cierra streams y socket (cada uno por separado para no dejar recursos abiertos). */
    @Override
    public void close() {
        try { in.close(); }     catch (IOException ignorada) { }
        try { out.close(); }    catch (IOException ignorada) { }
        try { socket.close(); } catch (IOException ignorada) { }
    }
}
