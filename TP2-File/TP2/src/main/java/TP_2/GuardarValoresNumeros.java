package TP_2;

import static TP_2.Utilidades.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Ejercicio 2: archivo de texto (en la carpeta del proyecto) con TODOS los
 * valores NUMÉRICOS que ingresa el usuario, uno por renglón. Lo que no sea
 * número se ignora.
 */
public class GuardarValoresNumeros {

	private static final File ARCHIVO = new File("GuardarValoresNumeros.txt");

	public static void ejecutar() {
		ps.println("Escribí valores (números y otras cosas). Para terminar escribí '" + PALABRA_SALIDA + "'.");
		try (PrintWriter pw = new PrintWriter(ARCHIVO, CODIFICACION)) {
			String dato;
			int guardados = 0;
			while ((dato = pedir("Dato: ")) != null) {
				if (esNumero(dato)) {
					pw.println(dato);
					pw.flush(); // queda en el archivo aunque se corte el programa
					guardados++;
				} else {
					ps.println("   (no es un número, no se guarda)");
				}
			}
			ps.println("Se guardaron " + guardados + " números en '" + ARCHIVO.getName() + "'.");
		} catch (IOException e) {
			ps.println("Ocurrió un error de entrada/salida: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		ejecutar();
	}
}
