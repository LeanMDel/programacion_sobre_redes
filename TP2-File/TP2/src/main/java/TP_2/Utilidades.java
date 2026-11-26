package TP_2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Cosas compartidas por todos los ejercicios.
 * 
 * Reglas de la consigna: - Consola (entrada): SOLO métodos de Reader -> read()
 * - Consola (salida): PrintStream - Escribir archivos: PrintWriter - Leer
 * archivos: BufferedReader
 */
public class Utilidades {

	/** Salida por consola. */
	public static final PrintStream ps = new PrintStream(System.out, true);

	/** Entrada por consola: se lee SOLO con read() de Reader. */
	private static final Reader entrada = new InputStreamReader(System.in);

	/** Codificación de todos los archivos (así la ñ y las tildes no se rompen). */
	public static final Charset CODIFICACION = StandardCharsets.UTF_8;

	/** Palabra para terminar un ejercicio (o Ctrl+Z / Ctrl+D). */
	public static final String PALABRA_SALIDA = "fin";

	// ---------------------------------------------------------------
	// Carpetas FUERA de la carpeta del proyecto (cada una distinta,
	// porque primos.dat NO puede estar en la carpeta de números.txt)
	// ---------------------------------------------------------------
	private static final String HOME = System.getProperty("user.home");
	public static final File CARPETA_NUMEROS = new File(HOME, "TP2_Numeros");
	public static final File CARPETA_PRIMOS = new File(HOME, "TP2_Primos");
	public static final File CARPETA_CARACTERES = new File(HOME, "TP2_Caracteres");

	/** "números.txt" (\u00fa = ú, escrito así para que no dependa de la codificación del .java). */
	public static File archivoNumeros() {
		return new File(CARPETA_NUMEROS, "n\u00fameros.txt");
	}

	/** Lee una línea de la consola usando SOLO Reader.read(). null = fin de entrada. */
	public static String leerLinea() throws IOException {
		StringBuilder sb = new StringBuilder();
		int c;
		while ((c = entrada.read()) != -1 && c != '\n') {
			if (c != '\r') {
				sb.append((char) c);
			}
		}
		if (c == -1 && sb.length() == 0) {
			return null;
		}
		return sb.toString();
	}

	/**
	 * Muestra un mensaje y lee la respuesta. Devuelve null si el usuario escribió
	 * "fin" (o cerró la entrada): así todos los ejercicios terminan igual.
	 */
	public static String pedir(String mensaje) throws IOException {
		ps.print(mensaje);
		String linea = leerLinea();
		if (linea == null || linea.trim().equalsIgnoreCase(PALABRA_SALIDA)) {
			return null;
		}
		return linea.trim();
	}

	/** ¿El texto es un número? (enteros o decimales, con punto o coma). */
	public static boolean esNumero(String texto) {
		return texto != null && texto.trim().matches("[-+]?\\d+([.,]\\d+)?");
	}

	public static boolean esPrimo(int num) {
		if (num <= 1) {
			return false;
		}
		for (int i = 2; i * i <= num; i++) {
			if (num % i == 0) {
				return false;
			}
		}
		return true;
	}

	public static void crearCarpeta(File carpeta) throws IOException {
		if (!carpeta.exists() && !carpeta.mkdirs()) {
			throw new IOException("No se pudo crear la carpeta " + carpeta.getAbsolutePath());
		}
	}

	/** Lee todas las líneas de un archivo (lista vacía si el archivo no existe). */
	public static List<String> leerLineas(File archivo) throws IOException {
		List<String> lineas = new ArrayList<>();
		if (!archivo.exists()) {
			return lineas;
		}
		try (BufferedReader br = new BufferedReader(new FileReader(archivo, CODIFICACION))) {
			String linea;
			while ((linea = br.readLine()) != null) {
				lineas.add(linea);
			}
		}
		return lineas;
	}

	/** Reescribe un archivo completo con las líneas dadas. */
	public static void escribirLineas(File archivo, List<String> lineas) throws IOException {
		try (PrintWriter pw = new PrintWriter(archivo, CODIFICACION)) {
			for (String linea : lineas) {
				pw.println(linea);
			}
		}
	}
}
