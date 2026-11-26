package TP3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Consultas {

    private static final File ARCHIVO = new File("Inventario.dat");
    private static final File TEMPORAL = new File("Inventario.dat.tmp");
    private static final Charset CODIFICACION = StandardCharsets.UTF_8;
    private static final Locale AR = Locale.forLanguageTag("es-AR");

    /**
     * Líneas del archivo que no se pudieron interpretar. No se pierden: al guardar
     * se vuelven a escribir tal cual, para que editar o eliminar un producto nunca
     * borre otras líneas por accidente.
     */
    private final List<String> lineasInvalidas = new ArrayList<>();

    public Consultas() {
        crearArchivoSiNoExiste();
    }

    // ------------------------------------------------------------------
    // ARCHIVO: creación, lectura y escritura
    // ------------------------------------------------------------------

    private void crearArchivoSiNoExiste() {
        try {
            if (!ARCHIVO.exists()) {
                ARCHIVO.createNewFile();
            }
        } catch (IOException e) {
            System.out.println(Utils.ERROR + "Error al inicializar el archivo." + Utils.RESET);
        }
    }

    /** Lee todo el archivo. Devuelve null si no se pudo leer (así nunca se pisa con una lista parcial). */
    private List<Producto> cargarProductos() {
        List<Producto> lista = new ArrayList<>();
        lineasInvalidas.clear();
        int numeroLinea = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO, CODIFICACION))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                numeroLinea++;
                if (linea.trim().isEmpty()) {
                    continue;
                }
                Producto p = interpretarLinea(linea);
                if (p == null) {
                    lineasInvalidas.add(linea);
                    System.out.println(Utils.AVISO + "\t[Aviso] Línea " + numeroLinea
                            + " con formato inválido (se conserva sin cambios): " + linea + Utils.RESET);
                } else {
                    lista.add(p);
                }
            }
        } catch (IOException e) {
            System.out.println(Utils.ERROR + "\tNo se pudo leer el archivo: " + e.getMessage() + Utils.RESET);
            return null;
        }
        return lista;
    }

    /** nombre;precioCompra;precioVenta;stock -> Producto, o null si la línea es inválida. */
    private Producto interpretarLinea(String linea) {
        String[] campos = linea.split(";", -1);
        if (campos.length != 4) {
            return null;
        }
        String nombre = campos[0].trim();
        Float compra = Utils.convertirAFloat(campos[1]);
        Float venta = Utils.convertirAFloat(campos[2]);
        Integer stock = Utils.convertirAInt(campos[3]);
        if (nombre.isEmpty() || compra == null || venta == null || stock == null) {
            return null;
        }
        return new Producto(nombre, compra, venta, stock);
    }

    /** Escribe en un temporal y recién al final reemplaza el original. */
    private boolean guardarTodosLosProductos(List<Producto> lista) {
        try {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(TEMPORAL, CODIFICACION))) {
                for (Producto p : lista) {
                    bw.write(p.toFileFormat());
                    bw.newLine();
                }
                for (String linea : lineasInvalidas) {
                    bw.write(linea);
                    bw.newLine();
                }
            }
            Files.move(TEMPORAL.toPath(), ARCHIVO.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            TEMPORAL.delete();
            System.out.println(Utils.ERROR + "\tError al escribir en el archivo: " + e.getMessage() + Utils.RESET);
            return false;
        }
    }

    private Producto buscar(List<Producto> lista, String nombre) {
        for (Producto p : lista) {
            if (p.getNombre().equalsIgnoreCase(nombre)) {
                return p;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // OPCIONES DEL MENÚ
    // ------------------------------------------------------------------

    public void agregarProducto() {
        System.out.println("\n" + Utils.TITULO + "--- REGISTRAR NUEVO ARTÍCULO ---" + Utils.RESET);
        List<Producto> lista = cargarProductos();
        if (lista == null) {
            return;
        }

        String nombre = Utils.leerStringObligatorio("\tNombre: ");
        if (buscar(lista, nombre) != null) {
            System.out.println(Utils.ERROR + "\tYa existe un producto llamado '" + nombre
                    + "'. Use la opción Editar para modificarlo." + Utils.RESET);
            return;
        }
        float pCompra = Utils.leerDecimalNoNegativo("\tPrecio de Compra: ");
        float pVenta = Utils.leerDecimalNoNegativo("\tPrecio de Venta: ");
        int stock = Utils.leerEnteroNoNegativo("\tStock: ");

        if (pVenta < pCompra) {
            System.out.println(Utils.AVISO + "\t[Aviso] El precio de venta es menor al de compra." + Utils.RESET);
        }

        lista.add(new Producto(nombre, pCompra, pVenta, stock));
        if (guardarTodosLosProductos(lista)) {
            System.out.println(Utils.EXITO + "\n\t¡Producto agregado con éxito!" + Utils.RESET);
        }
    }

    public void mostrarInventario() {
        List<Producto> lista = cargarProductos();
        if (lista == null) {
            return;
        }
        imprimirTabla(lista);
    }

    public void editarProducto() {
        System.out.println("\n" + Utils.TITULO + "--- EDITAR PRODUCTO ---" + Utils.RESET);
        List<Producto> lista = cargarProductos();
        if (lista == null) {
            return;
        }
        if (lista.isEmpty()) {
            System.out.println(Utils.AVISO + "\t[El inventario está vacío]" + Utils.RESET);
            return;
        }
        imprimirTabla(lista);

        String nombreBuscar = Utils.leerString("Ingrese el nombre del producto a editar: ");
        Producto p = buscar(lista, nombreBuscar);
        if (p == null) {
            System.out.println(Utils.ERROR + "\tProducto no encontrado." + Utils.RESET);
            return;
        }

        System.out.println(Utils.EXITO + "\tProducto encontrado. Presione Enter para dejar un valor como está." + Utils.RESET);

        String nuevoNombre;
        while (true) {
            nuevoNombre = Utils.leerString("\tNombre [" + p.getNombre() + "]: ");
            if (nuevoNombre.isEmpty()) {
                nuevoNombre = p.getNombre();
                break;
            }
            if (nuevoNombre.contains(";")) {
                System.out.println(Utils.ERROR + "\t[Error] El nombre no puede contener ';'" + Utils.RESET);
                continue;
            }
            Producto otro = buscar(lista, nuevoNombre);
            if (otro != null && otro != p) {
                System.out.println(Utils.ERROR + "\t[Error] Ya existe otro producto con ese nombre." + Utils.RESET);
                continue;
            }
            break;
        }

        float pCompra = Utils.leerDecimalNoNegativo(
                "\tPrecio de Compra [" + Utils.floatATexto(p.getPrecioCompra()) + "]: ", p.getPrecioCompra());
        float pVenta = Utils.leerDecimalNoNegativo(
                "\tPrecio de Venta [" + Utils.floatATexto(p.getPrecioVenta()) + "]: ", p.getPrecioVenta());
        int stock = Utils.leerEnteroNoNegativo("\tStock [" + p.getStock() + "]: ", p.getStock());

        if (pVenta < pCompra) {
            System.out.println(Utils.AVISO + "\t[Aviso] El precio de venta es menor al de compra." + Utils.RESET);
        }

        p.setNombre(nuevoNombre);
        p.setPrecioCompra(pCompra);
        p.setPrecioVenta(pVenta);
        p.setStock(stock);

        if (guardarTodosLosProductos(lista)) {
            System.out.println(Utils.EXITO + "\t¡Producto modificado correctamente!" + Utils.RESET);
        }
    }

    public void eliminarProducto() {
        System.out.println("\n" + Utils.TITULO + "--- ELIMINAR PRODUCTO ---" + Utils.RESET);
        List<Producto> lista = cargarProductos();
        if (lista == null) {
            return;
        }
        if (lista.isEmpty()) {
            System.out.println(Utils.AVISO + "\t[El inventario está vacío]" + Utils.RESET);
            return;
        }
        imprimirTabla(lista);

        String nombreBuscar = Utils.leerString("Ingrese el nombre del producto a eliminar: ");
        Producto p = buscar(lista, nombreBuscar);
        if (p == null) {
            System.out.println(Utils.ERROR + "\tProducto no encontrado." + Utils.RESET);
            return;
        }
        if (!Utils.confirmar("\t¿Seguro que desea eliminar '" + p.getNombre() + "'?")) {
            System.out.println(Utils.AVISO + "\tOperación cancelada." + Utils.RESET);
            return;
        }

        lista.remove(p);
        if (guardarTodosLosProductos(lista)) {
            System.out.println(Utils.EXITO + "\t¡Producto eliminado correctamente del archivo!" + Utils.RESET);
        }
    }

    // ------------------------------------------------------------------
    // PRESENTACIÓN
    // ------------------------------------------------------------------

    /** Tabla alineada: el ancho de la columna Nombre se adapta al nombre más largo. */
    private void imprimirTabla(List<Producto> lista) {
        int anchoNombre = "NOMBRE".length();
        for (Producto p : lista) {
            anchoNombre = Math.max(anchoNombre, p.getNombre().length());
        }
        String formato = "\t%-" + anchoNombre + "s  %16s  %16s  %8s%n";
        String linea = "\t" + "-".repeat(anchoNombre + 2 + 16 + 2 + 16 + 2 + 8);

        System.out.println();
        System.out.println(Utils.TITULO + linea);
        System.out.printf(formato, "NOMBRE", "P. COMPRA", "P. VENTA", "STOCK");
        System.out.println(linea + Utils.RESET);

        if (lista.isEmpty()) {
            System.out.println("\t[El inventario está vacío]");
        } else {
            for (Producto p : lista) {
                System.out.printf(formato, p.getNombre(), dinero(p.getPrecioCompra()), dinero(p.getPrecioVenta()),
                        String.valueOf(p.getStock()));
            }
        }
        System.out.println(Utils.TITULO + linea + Utils.RESET);
        System.out.println("\tTotal de productos: " + lista.size() + "\n");
    }

    private String dinero(float valor) {
        return String.format(AR, "$%,.2f", valor);
    }
}
