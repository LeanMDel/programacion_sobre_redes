package TP_2;

import static TP_2.Utilidades.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Ejercicio 1: archivo de texto (en la carpeta del proyecto) que guarda SOLO el
 * último dato que el usuario escribe por consola.
 */
public class GuardarUltimoDato {

	/** Sin ruta = carpeta del proyecto. */
	private static final File ARCHIVO = new File("ultimoDato.txt");

	public static void ejecutar() {
		ps.println("Escribí datos de a uno. Para terminar escribí '" + PALABRA_SALIDA + "'.");
		try {
			String dato;
			String ultimo = null;
			while ((dato = pedir("Dato: ")) != null) {
				if (dato.isEmpty()) {
					continue; // un Enter vacío no es un dato
				}
				// Se vuelve a crear el archivo en cada dato: siempre queda SOLO el último.
				try (PrintWriter pw = new PrintWriter(ARCHIVO, CODIFICACION)) {
					pw.println(dato);
				}
				ultimo = dato;
			}
			if (ultimo == null) {
				ps.println("No se ingresó ningún dato.");
			} else {
				ps.println("Último dato guardado en '" + ARCHIVO.getName() + "': " + ultimo);
			}
		} catch (IOException e) {
			ps.println("Ocurrió un error de entrada/salida: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		ejecutar();
	}
}
