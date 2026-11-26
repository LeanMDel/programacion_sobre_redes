package TP_2;

import static TP_2.Utilidades.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Ejercicio 3: "números.txt" FUERA de la carpeta del proyecto, con los números
 * pares del 0 al 1000, uno por renglón.
 */
public class GuardarNumerosPares {

	public static void ejecutar() {
		File archivo = archivoNumeros();
		try {
			crearCarpeta(CARPETA_NUMEROS);
			try (PrintWriter pw = new PrintWriter(archivo, CODIFICACION)) {
				for (int i = 0; i <= 1000; i += 2) {
					pw.println(i);
				}
			}
			ps.println("Pares del 0 al 1000 guardados en: " + archivo.getAbsolutePath());
		} catch (IOException e) {
			ps.println("Ocurrió un error de entrada/salida: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		ejecutar();
	}
}
