package TP_2;

import static TP_2.Utilidades.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Ejercicio 6: de "números.txt" (ya sin múltiplos de 3) guardar los primos en
 * "primos.dat", en una carpeta distinta a la del proyecto y a la de
 * números.txt.
 * 
 * OJO: como números.txt sólo tiene pares, el único primo posible es el 2.
 */
public class Numeros_Primos {

	public static void ejecutar() {
		File origen = archivoNumeros();
		File destino = new File(CARPETA_PRIMOS, "primos.dat");
		int cantidad = 0;

		try {
			crearCarpeta(CARPETA_PRIMOS);
			try (BufferedReader br = new BufferedReader(new FileReader(origen, CODIFICACION));
					PrintWriter pw = new PrintWriter(destino, CODIFICACION)) {

				String linea;
				while ((linea = br.readLine()) != null) {
					linea = linea.trim();
					if (linea.isEmpty()) {
						continue;
					}
					int numero = Integer.parseInt(linea);
					if (esPrimo(numero)) {
						pw.println(numero);
						cantidad++;
					}
				}
			}
			ps.println("primos.dat creado en " + destino.getAbsolutePath() + " (" + cantidad + " primo/s).");
		} catch (FileNotFoundException e) {
			ps.println("No existe " + origen.getAbsolutePath() + ". Primero ejecutá los ejercicios 3 y 5.");
		} catch (NumberFormatException e) {
			ps.println("Hay un renglón que no es un número entero: " + e.getMessage());
		} catch (IOException e) {
			ps.println("Error al procesar los archivos: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		ejecutar();
	}
}
