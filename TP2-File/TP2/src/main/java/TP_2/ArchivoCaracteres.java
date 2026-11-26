package TP_2;

import static TP_2.Utilidades.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ejercicio 7: "caracteres.dat" FUERA del proyecto. Se cargan 10 palabras con
 * la letra ñ por consola, se muestran ("Fichero original: "), se edita el
 * fichero cambiando cada ñ por "nie-nio" y se vuelven a mostrar ("Fichero
 * arreglado:").
 */
public class ArchivoCaracteres {

	private static final char ENIE_MIN = '\u00f1'; // ñ
	private static final char ENIE_MAY = '\u00d1'; // Ñ
	private static final String FONETICO = "nie-nio";
	private static final int CANTIDAD = 10;

	public static void ejecutar() {
		File archivo = new File(CARPETA_CARACTERES, "caracteres.dat");
		try {
			// 1) Cargar las palabras por consola
			List<String> palabras = new ArrayList<>();
			ps.println("Cargá " + CANTIDAD + " palabras que tengan la letra ñ ('" + PALABRA_SALIDA + "' cancela).");
			while (palabras.size() < CANTIDAD) {
				String palabra = pedir("Palabra " + (palabras.size() + 1) + ": ");
				if (palabra == null) {
					ps.println("Ejercicio cancelado.");
					return;
				}
				if (palabra.indexOf(ENIE_MIN) < 0 && palabra.indexOf(ENIE_MAY) < 0) {
					ps.println("   Esa palabra no tiene ñ, probá con otra.");
				} else if (palabra.contains(" ")) {
					ps.println("   Escribí una sola palabra.");
				} else {
					palabras.add(palabra);
				}
			}

			// 2) Crear el archivo (fuera del proyecto) con una palabra por renglón
			crearCarpeta(CARPETA_CARACTERES);
			escribirLineas(archivo, palabras);

			// 3) Mostrar el original
			mostrar("Fichero original: ", archivo);

			// 4) Editar el fichero: leer todo, cambiar, reescribir
			List<String> arregladas = new ArrayList<>();
			for (String linea : leerLineas(archivo)) {
				arregladas.add(linea.replace(String.valueOf(ENIE_MIN), FONETICO)
						.replace(String.valueOf(ENIE_MAY), FONETICO));
			}
			escribirLineas(archivo, arregladas);

			// 5) Mostrar el arreglado
			mostrar("Fichero arreglado:", archivo);

		} catch (IOException e) {
			ps.println("Error de entrada/salida: " + e.getMessage());
		}
	}

	private static void mostrar(String leyenda, File archivo) throws IOException {
		ps.println(leyenda);
		for (String linea : leerLineas(archivo)) {
			ps.println("   " + linea);
		}
	}

	public static void main(String[] args) {
		ejecutar();
	}
}
