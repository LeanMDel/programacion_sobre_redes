package servidor;
import cifrado.Cifrado;
public class MainServidor {

    public static final int PUERTO = 5000;

    public static void main(String[] args) {
        // Código único otorgado por el servidor del profesor: (completar acá)

        System.out.println(Cifrado.AMARILLO + "====================================\n"
                + "         SERVIDOR DE CHAT LOCAL\n"
                + "====================================" + Cifrado.RESET);

        new Servidor(PUERTO).iniciar();
    }
}
