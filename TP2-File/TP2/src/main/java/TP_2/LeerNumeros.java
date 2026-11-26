package TP_2;

import static TP_2.Utilidades.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Ejercicio 4: leer "números.txt" y mostrar los valores por consola.
 */
public class LeerNumeros {

	public static void ejecutar() {
		File archivo = archivoNumeros();
		try (BufferedReader br = new BufferedReader(new FileReader(archivo, CODIFICACION))) {
			String linea;
			while ((linea = br.readLine()) != null) {
				ps.println(linea);
			}
		} catch (FileNotFoundException e) {
			ps.println("No existe " + archivo.getAbsolutePath() + ". Primero ejecutá el ejercicio 3.");
		} catch (IOException e) {
			ps.println("Error al leer el archivo: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		ejecutar();
	}
}
