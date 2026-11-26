package TP_2;

import static TP_2.Utilidades.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Ejercicio 8: "lorem.html" (hecho a mano, en la carpeta del proyecto) tiene un
 * texto lorem ipsum en algún lugar. El algoritmo abre el archivo y borra toda la
 * leyenda de lorem, dejando el resto del HTML igual.
 * 
 * Algoritmo: se lee todo el HTML en un String; mientras aparezca "Lorem ipsum"
 * se borra desde ahí hasta la siguiente etiqueta ('<'); se reescribe el archivo.
 */
public class BorrarLorem {

	private static final File ARCHIVO = new File("lorem.html");
	private static final String LOREM = "lorem ipsum";

	public static void ejecutar() {
		try {
			// 1) Leer el HTML completo
			StringBuilder sb = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO, CODIFICACION))) {
				String linea;
				while ((linea = br.readLine()) != null) {
					sb.append(linea).append('\n');
				}
			}
			String html = sb.toString();

			// 2) Borrar cada leyenda de lorem (hasta la próxima etiqueta)
			int borradas = 0;
			int inicio;
			while ((inicio = buscarLorem(html)) >= 0) {
				int fin = html.indexOf('<', inicio);
				if (fin < 0) {
					fin = html.length();
				}
				html = html.substring(0, inicio) + html.substring(fin);
				borradas++;
			}

			if (borradas == 0) {
				ps.println("No se encontró ninguna leyenda de lorem en " + ARCHIVO.getName() + ".");
				return;
			}

			// 3) Reescribir el archivo
			try (PrintWriter pw = new PrintWriter(ARCHIVO, CODIFICACION)) {
				pw.print(html);
			}
			ps.println("Se borraron " + borradas + " leyenda/s de lorem en " + ARCHIVO.getName() + ".");

		} catch (FileNotFoundException e) {
			ps.println("No existe " + ARCHIVO.getAbsolutePath() + " (tiene que estar en la carpeta del proyecto).");
		} catch (IOException e) {
			ps.println("Error de entrada/salida: " + e.getMessage());
		}
	}

	/** Posición de "lorem ipsum" (sin importar mayúsculas), o -1 si no está. */
	private static int buscarLorem(String texto) {
		for (int i = 0; i + LOREM.length() <= texto.length(); i++) {
			if (texto.regionMatches(true, i, LOREM, 0, LOREM.length())) {
				return i;
			}
		}
		return -1;
	}

	public static void main(String[] args) {
		ejecutar();
	}
}
