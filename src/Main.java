import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Programa principal que procesa archivos CSV generados de productos y vendedores,
 * acumula la información de ventas y produce reportes ordenados.
 * <p>
 * Archivos esperados como entrada:
 * <ul>
 *   <li><b>productos.csv</b>: {@code id;nombre;precio}</li>
 *   <li><b>vendedores.csv</b>: {@code tipoDoc;numDoc;nombres;apellidos}</li>
 *   <li><b>vendedor_{numDoc}.csv</b>: primera línea {@code tipoDoc;numDoc}, 
 *       y líneas siguientes {@code idProducto;cantidad;}.</li>
 * </ul>
 * Archivos de salida:
 * <ul>
 *   <li><b>reporte_vendedores.csv</b>: vendedores ordenados por ventas totales descendentes.</li>
 *   <li><b>reporte_productos.csv</b>: productos ordenados por cantidad vendida descendente.</li>
 * </ul>
 *
 * @author  Lina Gissela Aya Machado
 * @author  Diego Harney Casallas Sanchez
 * @author  Daniel Enrique León Diaz
 * @author  Laura Cáceres Palma
 * @version 1.0
 */
class Producto {
    /** Identificador del producto (coincide con el campo id en vendedor_*.csv). */
    String id;
    /** Nombre del producto. */
    String nombre;
    /** Precio unitario del producto. */
    double precio;
    /** Cantidad total vendida (se acumula al procesar los archivos de ventas). */
    int cantidadVendida = 0;

    /**
     * Constructor de la clase Producto.
     *
     * @param id identificador único del producto.
     * @param n nombre del producto.
     * @param p precio unitario del producto.
     */
    Producto(String id, String n, double p) { this.id = id; this.nombre = n; this.precio = p; }

    /**
     * Obtiene el identificador del producto.
     *
     * @return id del producto.
     */
    public String getId() { return id; }
}

class Vendedor {
    /** Tipo de documento (CC, CE, TI). */
    String tipoDoc;
    /** Número de documento del vendedor. */
    String numDoc;
    /** Nombres del vendedor. */
    String nombres;
    /** Apellidos del vendedor. */
    String apellidos;
    /** Monto total vendido por este vendedor. */
    double ventasTotales = 0.0;

    /**
     * Constructor de la clase Vendedor.
     *
     * @param td tipo de documento.
     * @param nd número de documento.
     * @param n nombres del vendedor.
     * @param a apellidos del vendedor.
     */
    Vendedor(String td, String nd, String n, String a) { 
        this.tipoDoc = td; 
        this.numDoc = nd; 
        this.nombres = n; 
        this.apellidos = a; 
    }

    /**
     * Obtiene el número de documento del vendedor.
     *
     * @return número de documento.
     */
    public String getNumDoc() { return numDoc; }
}

public class Main {
    /**
     * Método principal. Carga la información de productos y vendedores desde archivos CSV,
     * procesa los archivos de ventas de cada vendedor y genera reportes consolidados.
     *
     * @param args argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        try {
            System.out.println("Iniciando...");

            // Carga productos en un mapa
            Map<String, Producto> mapaProductos = cargarDatos(
                "productos.csv",
                linea -> {
                    String[] d = linea.split(";");
                    return new Producto(d[0], d[1], Double.parseDouble(d[2]));
                },
                Producto::getId
            );

            // Carga vendedores en un mapa
            Map<String, Vendedor> mapaVendedores = cargarDatos(
                "vendedores.csv",
                linea -> {
                    String[] d = linea.split(";");
                    return new Vendedor(d[0], d[1], d[2], d[3]);
                },
                Vendedor::getNumDoc
            );

            // Procesa archivos vendedor_*.csv
            Files.walk(Paths.get("."))
                 .filter(path -> path.getFileName().toString().startsWith("vendedor_"))
                 .forEach(path -> procesarArchivoVenta(path, mapaProductos, mapaVendedores));

            // Genera reportes
            generarReportes(mapaVendedores, mapaProductos);

            System.out.println("¡Reportes generados!");
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    /**
     * Carga un archivo CSV y construye un mapa de objetos indexados por clave.
     *
     * @param archivo nombre del archivo CSV a leer.
     * @param constructor función que convierte cada línea en una instancia de tipo T.
     * @param getKey función que extrae la clave del objeto T.
     * @param <T> tipo de objeto a crear por cada línea (Producto, Vendedor, etc.).
     * @param <K> tipo de clave del mapa (String usualmente).
     * @return mapa con los objetos construidos indexados por su clave.
     * @throws IOException si ocurre un error al leer el archivo.
     */
    private static <T, K> Map<K, T> cargarDatos(
            String archivo,
            Function<String, T> constructor,
            Function<T, K> getKey) throws IOException {

        try (var stream = Files.lines(Paths.get(archivo))) {
            return stream
                .map(constructor)
                .collect(Collectors.toMap(getKey, Function.identity()));
        }
    }

    /**
     * Procesa un archivo de ventas de un vendedor específico, acumulando la información
     * en el mapa de vendedores y en el mapa de productos.
     * <p>
     * Formato esperado del archivo:
     * <ul>
     *   <li>Línea 0: {@code tipoDoc;numDoc}</li>
     *   <li>Líneas siguientes: {@code idProducto;cantidad;}</li>
     * </ul>
     * </p>
     *
     * @param archivo ruta del archivo vendedor_*.csv a procesar.
     * @param prods mapa de productos indexado por id.
     * @param vends mapa de vendedores indexado por número de documento.
     */
    private static void procesarArchivoVenta(
            Path archivo,
            Map<String, Producto> prods,
            Map<String, Vendedor> vends) {

        try {
            List<String> lineas = Files.readAllLines(archivo);
            String idVendedor = lineas.get(0).split(";")[1];
            Vendedor vendedor = vends.get(idVendedor);
            if (vendedor == null) return;

            for (int i = 1; i < lineas.size(); i++) {
                String[] datos = lineas.get(i).split(";");
                Producto producto = prods.get(datos[0]);
                int cantidad = Integer.parseInt(datos[1]);

                if (producto != null) {
                    vendedor.ventasTotales += producto.precio * cantidad;
                    producto.cantidadVendida += cantidad;
                }
            }
        } catch (Exception e) {
            System.err.println("ADVERTENCIA: " + archivo.getFileName());
        }
    }

    /**
     * Genera los reportes finales en formato CSV:
     * <ul>
     *   <li><b>reporte_vendedores.csv</b>: listado de vendedores con el total vendido,
     *       ordenados de mayor a menor.</li>
     *   <li><b>reporte_productos.csv</b>: listado de productos con su precio,
     *       ordenados por la cantidad vendida de mayor a menor.</li>
     * </ul>
     *
     * @param mapaVendedores mapa de vendedores con las ventas acumuladas.
     * @param mapaProductos mapa de productos con cantidades acumuladas.
     * @throws IOException si ocurre un error al escribir los reportes.
     */
    private static void generarReportes(
            Map<String, Vendedor> mapaVendedores,
            Map<String, Producto> mapaProductos) throws IOException {

        List<Vendedor> vendedoresOrdenados = mapaVendedores.values().stream()
            .sorted(Comparator.comparingDouble((Vendedor v) -> v.ventasTotales).reversed())
            .collect(Collectors.toList());

        try (PrintWriter writer = new PrintWriter("reporte_vendedores.csv")) {
            for (Vendedor v : vendedoresOrdenados) {
                writer.printf("%s %s;%.2f%n", v.nombres, v.apellidos, v.ventasTotales);
            }
        }

        List<Producto> productosOrdenados = mapaProductos.values().stream()
            .sorted(Comparator.comparingInt((Producto p) -> p.cantidadVendida).reversed())
            .collect(Collectors.toList());

        try (PrintWriter writer = new PrintWriter("reporte_productos.csv")) {
            for (Producto p : productosOrdenados) {
                writer.printf("%s;%.2f%n", p.nombre, p.precio);
            }
        }
    }
}
