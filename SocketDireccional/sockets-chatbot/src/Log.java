import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger de consola con colores ANSI y columnas alineadas por tabulaciones.
 * Formato:  [hora]  TIPO  ORIGEN  mensaje
 */
public class Log {

    public static final String RESET    = "\u001B[0m";
    public static final String NEGRITA  = "\u001B[1m";
    public static final String ROJO     = "\u001B[31m";
    public static final String VERDE    = "\u001B[32m";
    public static final String AMARILLO = "\u001B[33m";
    public static final String AZUL     = "\u001B[34m";
    public static final String MAGENTA  = "\u001B[35m";
    public static final String CIAN     = "\u001B[36m";
    public static final String GRIS     = "\u001B[90m";

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private final String origen;

    public Log(String origen) {
        this.origen = origen;
    }

    // ---------- Eventos ----------
    public void conexion(String m)    { linea(VERDE,    "CONEXION",    m); }
    public void enviado(String m)     { linea(CIAN,     "ENVIADO",     m); }
    public void recibido(String m)    { linea(AMARILLO, "RECIBIDO",    m); }
    public void cifrado(String m)     { linea(GRIS,     "CIFRADO",     m); }
    public void info(String m)        { linea(AZUL,     "INFO",        m); }
    public void archivo(String m)     { linea(MAGENTA,  "ARCHIVO",     m); }
    public void error(String m)       { linea(ROJO,     "ERROR",       m); }
    public void desconexion(String m) { linea(ROJO,     "DESCONEXION", m); }

    private void linea(String color, String tipo, String msg) {
        String[] lineas = msg.split("\n");
        for (int i = 0; i < lineas.length; i++) {
            String ts = (i == 0) ? "[" + LocalTime.now().format(FMT) + "]" : "";
            String t  = (i == 0) ? tipo : "";
            String o  = (i == 0) ? origen : "";
            System.out.println(
                GRIS + String.format("%-14s", ts) + RESET + "\t"
              + color + NEGRITA + String.format("%-12s", t) + RESET + "\t"
              + GRIS + String.format("%-9s", o) + RESET + "\t"
              + color + lineas[i] + RESET);
        }
    }

    // ---------- Utilidades compartidas ----------
    public static void banner(String titulo) {
        String barra = "══════════════════════════════════════════════════════════════";
        System.out.println(NEGRITA + VERDE + "╔" + barra + "╗");
        System.out.println("║  " + String.format("%-" + (barra.length() - 2) + "s", titulo) + "║");
        System.out.println("╚" + barra + "╝" + RESET);
    }

    /** Recorta el texto cifrado (Base64) para que el log siga siendo legible. */
    public static String resumen(String base64) {
        if (base64.length() <= 48) return base64;
        return base64.substring(0, 48) + "... (" + base64.length() + " chars)";
    }

    /** Describe un mensaje del protocolo sin volcar contraseñas ni archivos completos. */
    public static String describir(String msg) {
        int i = msg.indexOf('|');
        String tipo  = (i < 0) ? msg : msg.substring(0, i);
        String resto = (i < 0) ? ""  : msg.substring(i + 1);
        switch (tipo) {
            case "CHAT":
                return "CHAT   \"" + resto + "\"";
            case "AUTH": {
                int j = resto.indexOf('|');
                String usuario = (j < 0) ? resto : resto.substring(0, j);
                return "AUTH   usuario=" + usuario + "  clave=****";
            }
            case "STOCK":
                return "STOCK  producto=\"" + resto + "\"";
            case "SUBIR": {
                int j = resto.lastIndexOf('|');
                String nombre = (j < 0) ? resto : resto.substring(0, j);
                int bytes = (j < 0) ? 0 : (resto.length() - j - 1) * 3 / 4;
                return "SUBIR  archivo=\"" + nombre + "\"  (~" + bytes + " bytes)";
            }
            case "AYUDA":
            case "SALIR":
                return tipo;
            default:
                return msg;   // respuestas del servidor
        }
    }
}
