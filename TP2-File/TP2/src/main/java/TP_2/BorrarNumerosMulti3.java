package TP_2;

import static TP_2.Utilidades.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Ejercicio 5: borrar de "números.txt" todos los renglones con múltiplos de 3
 * (el 0 también es múltiplo de 3, así que se borra).
 * 
 * Se escribe en un archivo temporal y SOLO si todo salió bien se reemplaza el
 * original. Si algo falla, números.txt queda intacto.
 */
public class BorrarNumerosMulti3 {

	public static void ejecutar() {
		File original = archivoNumeros();
		File temporal = new File(original.getParentFile(), "n\u00fameros.tmp");
		boolean todoOk = false;
		int borrados = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(original, CODIFICACION));
				PrintWriter pw = new PrintWriter(temporal, CODIFICACION)) {

			String linea;
			while ((linea = br.readLine()) != null) {
				linea = linea.trim();
				if (linea.isEmpty()) {
					continue;
				}
				int numero = Integer.parseInt(linea);
				if (numero % 3 == 0) {
					borrados++;
				} else {
					pw.println(numero);
				}
			}
			todoOk = true;

		} catch (FileNotFoundException e) {
			ps.println("No existe " + original.getAbsolutePath() + ". Primero ejecutá el ejercicio 3.");
		} catch (NumberFormatException e) {
			ps.println("Hay un renglón que no es un número entero: " + e.getMessage());
		} catch (IOException e) {
			ps.println("Ocurrió un error al procesar el archivo: " + e.getMessage());
		}

		// Los archivos ya están cerrados acá.
		if (!todoOk) {
			temporal.delete();
			ps.println("No se modificó números.txt.");
			return;
		}
		try {
			Files.move(temporal.toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);
			ps.println("Se borraron " + borrados + " renglones con múltiplos de 3.");
		} catch (IOException e) {
			ps.println("No se pudo actualizar el archivo original: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		ejecutar();
	}
}
