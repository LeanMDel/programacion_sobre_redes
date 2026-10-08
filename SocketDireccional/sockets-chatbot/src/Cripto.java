import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Encriptación simétrica AES-128 en modo GCM (cifra + autentica).
 * Cliente y servidor comparten la misma clave precompartida.
 * Formato de salida: [IV de 12 bytes][texto cifrado + tag de autenticación]
 */
public class Cripto {

    // Debe ser IDÉNTICA en cliente y servidor.
    private static final String CLAVE_COMPARTIDA = "SistemasDistribuidos-TP-Sockets-2026";

    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final SecretKeySpec CLAVE;
    private static final SecureRandom RANDOM = new SecureRandom();

    static {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(CLAVE_COMPARTIDA.getBytes(StandardCharsets.UTF_8));
            CLAVE = new SecretKeySpec(Arrays.copyOf(hash, 16), "AES");
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static byte[] cifrar(String textoPlano) throws Exception {
        byte[] iv = new byte[IV_BYTES];
        RANDOM.nextBytes(iv);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, CLAVE, new GCMParameterSpec(TAG_BITS, iv));
        byte[] cifrado = c.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));

        byte[] salida = new byte[IV_BYTES + cifrado.length];
        System.arraycopy(iv, 0, salida, 0, IV_BYTES);
        System.arraycopy(cifrado, 0, salida, IV_BYTES, cifrado.length);
        return salida;
    }

    public static String descifrar(byte[] datos) throws Exception {
        byte[] iv = Arrays.copyOfRange(datos, 0, IV_BYTES);
        byte[] cifrado = Arrays.copyOfRange(datos, IV_BYTES, datos.length);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, CLAVE, new GCMParameterSpec(TAG_BITS, iv));
        return new String(c.doFinal(cifrado), StandardCharsets.UTF_8);
    }
}
