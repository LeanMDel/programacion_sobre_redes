package TP_2;

import static TP_2.Utilidades.*;

import java.io.IOException;

/**
 * Menú principal del TP2. Cada ejercicio también se puede ejecutar solo (todas
 * las clases tienen su propio main).
 */
public class main {

	public static void main(String[] args) {
		try {
			String opcion;
			do {
				ps.println();
				ps.println("=== TP2 - ARCHIVOS ===");
				ps.println("1. Guardar el último dato");
				ps.println("2. Guardar todos los valores numéricos");
				ps.println("3. Crear números.txt (pares del 0 al 1000)");
				ps.println("4. Leer números.txt");
				ps.println("5. Borrar múltiplos de 3 de números.txt");
				ps.println("6. Crear primos.dat");
				ps.println("7. caracteres.dat (ñ -> nie-nio)");
				ps.println("8. Borrar el lorem de lorem.html");
				ps.println("9. Menú de clima");
				ps.println("0. Salir");
				opcion = pedir("Opción: ");
				if (opcion == null) {
					opcion = "0";
				}
				switch (opcion) {
				case "1":
					GuardarUltimoDato.ejecutar();
					break;
				case "2":
					GuardarValoresNumeros.ejecutar();
					break;
				case "3":
					GuardarNumerosPares.ejecutar();
					break;
				case "4":
					LeerNumeros.ejecutar();
					break;
				case "5":
					BorrarNumerosMulti3.ejecutar();
					break;
				case "6":
					Numeros_Primos.ejecutar();
					break;
				case "7":
					ArchivoCaracteres.ejecutar();
					break;
				case "8":
					BorrarLorem.ejecutar();
					break;
				case "9":
					MenuClima.ejecutar();
					break;
				case "0":
					break;
				default:
					ps.println("Opción inválida.");
				}
			} while (!opcion.equals("0"));
			ps.println("Chau!");
		} catch (IOException e) {
			ps.println("Error de entrada/salida: " + e.getMessage());
		}
	}
}
