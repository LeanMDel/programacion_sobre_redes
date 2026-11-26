package TP_2;

import static TP_2.Utilidades.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

/**
 * Ejercicio 9: menú para cargar datos del clima ubicados por fecha, mostrar
 * todos los datos y borrar un registro del archivo.
 * 
 * Formato de cada renglón de clima.txt: fecha;temperatura;humedad;condicion
 * Los registros se mantienen ordenados por fecha y hay uno solo por fecha.
 */
public class MenuClima {

	private static final File ARCHIVO = new File("clima.txt");
	private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/uuuu")
			.withResolverStyle(ResolverStyle.STRICT);

	public static void ejecutar() {
		try {
			String opcion;
			do {
				ps.println();
				ps.println("=== CLIMA ===");
				ps.println("1. Cargar registro");
				ps.println("2. Mostrar todos los registros");
				ps.println("3. Borrar un registro");
				ps.println("0. Volver");
				opcion = pedir("Opción: ");
				if (opcion == null) {
					opcion = "0";
				}
				switch (opcion) {
				case "1":
					cargar();
					break;
				case "2":
					mostrar();
					break;
				case "3":
					borrar();
					break;
				case "0":
					break;
				default:
					ps.println("Opción inválida.");
				}
			} while (!opcion.equals("0"));
		} catch (IOException e) {
			ps.println("Error de entrada/salida: " + e.getMessage());
		}
	}

	private static void cargar() throws IOException {
		String fecha = pedirFecha();
		if (fecha == null) {
			return;
		}
		List<String> registros = leerLineas(ARCHIVO);
		if (buscar(registros, fecha) >= 0) {
			ps.println("Ya hay un registro para el " + fecha + ". Borralo primero si querés cargarlo de nuevo.");
			return;
		}

		String temperatura = pedirNumero("Temperatura (°C): ", -90, 60);
		if (temperatura == null) {
			return;
		}
		String humedad = pedirNumero("Humedad (%): ", 0, 100);
		if (humedad == null) {
			return;
		}
		String condicion = pedir("Condición (soleado, lluvia, nublado...): ");
		if (condicion == null) {
			return;
		}
		condicion = condicion.replace(';', ',');

		registros.add(fecha + ";" + temperatura + ";" + humedad + ";" + condicion);
		// "ubicados por la fecha": se guardan ordenados por fecha
		registros.sort((a, b) -> fechaDe(a).compareTo(fechaDe(b)));
		escribirLineas(ARCHIVO, registros);
		ps.println("Registro guardado.");
	}

	private static void mostrar() throws IOException {
		List<String> registros = leerLineas(ARCHIVO);
		if (registros.isEmpty()) {
			ps.println("No hay registros cargados.");
			return;
		}
		ps.printf("%-12s %10s %12s  %s%n", "Fecha", "Temp (°C)", "Humedad (%)", "Condición");
		for (String registro : registros) {
			String[] p = registro.split(";", 4);
			ps.printf("%-12s %10s %12s  %s%n", p[0], p[1], p[2], p[3]);
		}
	}

	private static void borrar() throws IOException {
		List<String> registros = leerLineas(ARCHIVO);
		if (registros.isEmpty()) {
			ps.println("No hay registros para borrar.");
			return;
		}
		mostrar();
		String fecha = pedirFecha();
		if (fecha == null) {
			return;
		}
		int indice = buscar(registros, fecha);
		if (indice < 0) {
			ps.println("No hay ningún registro con la fecha " + fecha + ".");
			return;
		}
		registros.remove(indice);
		escribirLineas(ARCHIVO, registros);
		ps.println("Registro del " + fecha + " borrado.");
	}

	// ------------------------- auxiliares -------------------------

	/** Pide una fecha dd/MM/aaaa hasta que sea válida. null = cancelar. */
	private static String pedirFecha() throws IOException {
		while (true) {
			String texto = pedir("Fecha (dd/mm/aaaa): ");
			if (texto == null) {
				return null;
			}
			try {
				return LocalDate.parse(texto, FORMATO).format(FORMATO);
			} catch (DateTimeParseException e) {
				ps.println("   Fecha inválida. Ejemplo: 05/06/2026");
			}
		}
	}

	/** Pide un número entre min y max. Devuelve el texto con punto decimal. null = cancelar. */
	private static String pedirNumero(String mensaje, double min, double max) throws IOException {
		while (true) {
			String texto = pedir(mensaje);
			if (texto == null) {
				return null;
			}
			if (esNumero(texto)) {
				String normal = texto.replace(',', '.');
				double valor = Double.parseDouble(normal);
				if (valor >= min && valor <= max) {
					return normal;
				}
			}
			ps.println("   Ingresá un número entre " + (int) min + " y " + (int) max + ".");
		}
	}

	private static LocalDate fechaDe(String registro) {
		return LocalDate.parse(registro.substring(0, registro.indexOf(';')), FORMATO);
	}

	/** Índice del registro con esa fecha, o -1. */
	private static int buscar(List<String> registros, String fecha) {
		for (int i = 0; i < registros.size(); i++) {
			if (registros.get(i).startsWith(fecha + ";")) {
				return i;
			}
		}
		return -1;
	}

	public static void main(String[] args) {
		ejecutar();
	}
}
