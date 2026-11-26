package TP3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;

public class Utils {

    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    // Códigos ANSI para estética en consola
    public static final String RESET = "\u001B[0m";
    public static final String SELECCION = "\u001B[36m"; // Cian
    public static final String EXITO = "\u001B[32m";     // Verde
    public static final String ERROR = "\u001B[31m";     // Rojo
    public static final String AVISO = "\u001B[33m";     // Amarillo
    public static final String TITULO = "\u001B[35m";    // Púrpura

    /** Resultado de identificar qué tipo de dato es un texto. */
    public enum TipoDato {
        NO_NUMERICO, ENTERO, DECIMAL
    }

    /** Se lanza cuando la consola ya no tiene más entrada (Ctrl+Z / Ctrl+D). */
    public static class EntradaCerradaException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public EntradaCerradaException() {
            super("Se cerró la entrada de consola.");
        }
    }

    // ------------------------------------------------------------------
    // LECTURA DE CONSOLA (retorna String)
    // ------------------------------------------------------------------

    public static String leerString(String mensaje) {
        System.out.print(SELECCION + mensaje + RESET);
        try {
            String linea = reader.readLine();
            if (linea == null) {
                throw new EntradaCerradaException();
            }
            return linea.trim();
        } catch (IOException e) {
            throw new EntradaCerradaException();
        }
    }

    public static String leerStringObligatorio(String mensaje) {
        while (true) {
            String entrada = leerString(mensaje);
            if (!entrada.isEmpty() && !entrada.contains(";")) {
                return entrada;
            }
            System.out.println(ERROR + "\t[Error] El campo no puede estar vacío ni contener ';'" + RESET);
        }
    }

    /** Pregunta de sí/no. Devuelve true si responde s/si. */
    public static boolean confirmar(String mensaje) {
        while (true) {
            String r = leerString(mensaje + " (s/n): ").toLowerCase();
            if (r.equals("s") || r.equals("si") || r.equals("sí")) {
                return true;
            }
            if (r.equals("n") || r.equals("no")) {
                return false;
            }
            System.out.println(ERROR + "\t[Error] Responda 's' o 'n'." + RESET);
        }
    }

    // ------------------------------------------------------------------
    // VALIDACIÓN DEL TIPO DE DATO
    // ------------------------------------------------------------------

    /** Identifica si el texto NO es numérico, si es un ENTERO o si es un DECIMAL (punto o coma). */
    public static TipoDato identificarTipo(String texto) {
        if (texto == null) {
            return TipoDato.NO_NUMERICO;
        }
        String t = texto.trim();
        if (t.matches("[-+]?\\d+")) {
            return TipoDato.ENTERO;
        }
        if (t.matches("[-+]?\\d+[.,]\\d+")) {
            return TipoDato.DECIMAL;
        }
        return TipoDato.NO_NUMERICO;
    }

    public static boolean esNumerico(String texto) {
        return identificarTipo(texto) != TipoDato.NO_NUMERICO;
    }

    public static boolean esEntero(String texto) {
        return identificarTipo(texto) == TipoDato.ENTERO;
    }

    public static boolean esDecimal(String texto) {
        return identificarTipo(texto) == TipoDato.DECIMAL;
    }

    // ------------------------------------------------------------------
    // CONVERSIÓN SEGURA (nunca lanza excepción)
    // ------------------------------------------------------------------

    /** Convierte a int. Devuelve null si no es un entero válido o si no entra en un int. */
    public static Integer convertirAInt(String texto) {
        if (!esEntero(texto)) {
            return null;
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) { // por ejemplo 99999999999 (se pasa del rango)
            return null;
        }
    }

    /** Igual que convertirAInt, pero devuelve un valor por defecto si no se puede convertir. */
    public static int convertirAInt(String texto, int valorPorDefecto) {
        Integer valor = convertirAInt(texto);
        return (valor == null) ? valorPorDefecto : valor;
    }

    /** Convierte a float (acepta 50.6 y 50,6). Devuelve null si no es un número válido. */
    public static Float convertirAFloat(String texto) {
        if (!esNumerico(texto)) {
            return null;
        }
        try {
            float valor = Float.parseFloat(texto.trim().replace(',', '.'));
            return Float.isInfinite(valor) ? null : valor;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Igual que convertirAFloat, pero devuelve un valor por defecto si no se puede convertir. */
    public static float convertirAFloat(String texto, float valorPorDefecto) {
        Float valor = convertirAFloat(texto);
        return (valor == null) ? valorPorDefecto : valor;
    }

    /** float a texto SIN notación científica y sin ceros de más (50.6 -> "50.6", 100.0 -> "100"). */
    public static String floatATexto(float valor) {
        return new BigDecimal(Float.toString(valor)).stripTrailingZeros().toPlainString();
    }

    // ------------------------------------------------------------------
    // LECTURA CON VALIDACIÓN (repite hasta que el dato sea correcto)
    // ------------------------------------------------------------------

    public static int leerEntero(String mensaje) {
        return pedirEntero(mensaje, false, null);
    }

    public static int leerEnteroNoNegativo(String mensaje) {
        return pedirEntero(mensaje, true, null);
    }

    /** Si el usuario presiona Enter sin escribir nada, devuelve el valor actual. */
    public static int leerEnteroNoNegativo(String mensaje, int valorActual) {
        return pedirEntero(mensaje, true, valorActual);
    }

    public static float leerDecimal(String mensaje) {
        return pedirDecimal(mensaje, false, null);
    }

    public static float leerDecimalNoNegativo(String mensaje) {
        return pedirDecimal(mensaje, true, null);
    }

    /** Si el usuario presiona Enter sin escribir nada, devuelve el valor actual. */
    public static float leerDecimalNoNegativo(String mensaje, float valorActual) {
        return pedirDecimal(mensaje, true, valorActual);
    }

    private static int pedirEntero(String mensaje, boolean soloNoNegativo, Integer valorActual) {
        while (true) {
            String entrada = leerString(mensaje);
            if (entrada.isEmpty() && valorActual != null) {
                return valorActual;
            }
            Integer valor = convertirAInt(entrada);
            if (valor == null) {
                System.out.println(ERROR + "\t[Error] Debe ingresar un número entero válido." + RESET);
            } else if (soloNoNegativo && valor < 0) {
                System.out.println(ERROR + "\t[Error] El valor no puede ser negativo." + RESET);
            } else {
                return valor;
            }
        }
    }

    private static float pedirDecimal(String mensaje, boolean soloNoNegativo, Float valorActual) {
        while (true) {
            String entrada = leerString(mensaje);
            if (entrada.isEmpty() && valorActual != null) {
                return valorActual;
            }
            Float valor = convertirAFloat(entrada);
            if (valor == null) {
                System.out.println(ERROR + "\t[Error] Debe ingresar un número válido (ej: 50.6 o 50,6)." + RESET);
            } else if (soloNoNegativo && valor < 0) {
                System.out.println(ERROR + "\t[Error] El valor no puede ser negativo." + RESET);
            } else {
                return valor;
            }
        }
    }
}
