import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 
 * @author Integrantes:
 * Lina Gissela Aya Machado
 * Diego Harney Casallas Sanchez
 * Daniel Enrique León Diaz
 * Laura Cáceres Palma
 *
 */

class Producto {
    // Identificador del producto (coincide con el "id" que aparece en vendedor_*.csv)
    String id, nombre;
    // Precio unitario
    double precio;
    // Cantidad total vendida (se acumula al procesar archivos vendedor_*.csv)
    int cantidadVendida = 0;

    // Constructor sencillo
    Producto(String id, String n, double p) { this.id = id; this.nombre = n; this.precio = p; }

    // Getter usado como "key extractor" al construir el mapa de productos
    public String getId() { return id; }
}

class Vendedor {
    // Datos de identificación
    String tipoDoc, numDoc, nombres, apellidos;
    // Monto total vendido por este vendedor (se acumula)
    double ventasTotales = 0.0;

    // Constructor sencillo
    Vendedor(String td, String nd, String n, String a) { this.tipoDoc = td; this.numDoc = nd; this.nombres = n; this.apellidos = a; }

    // Getter usado como "key extractor" al construir el mapa de vendedores
    public String getNumDoc() { return numDoc; }
}

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("Iniciando...");

            // Carga productos desde productos.csv a un Map<idProducto, Producto>
            // "cargarDatos" es genérico: recibe cómo construir T a partir de una línea y cómo obtener su clave.
            Map<String, Producto> mapaProductos = cargarDatos(
                "productos.csv",
                linea -> {
                    // Se espera formato: id;nombre;precio
                    String[] d = linea.split(";");
                    return new Producto(d[0], d[1], Double.parseDouble(d[2]));
                },
                Producto::getId // clave: id del producto
            );

            // Carga vendedores desde vendedores.csv a un Map<numDoc, Vendedor>
            Map<String, Vendedor> mapaVendedores = cargarDatos(
                "vendedores.csv",
                linea -> {
                    // Se espera formato: tipoDoc;numDoc;nombres;apellidos
                    String[] d = linea.split(";");
                    return new Vendedor(d[0], d[1], d[2], d[3]);
                },
                Vendedor::getNumDoc // clave: número de documento
            );

            // Recorre el directorio actual (.) buscando archivos cuyo nombre empiece por "vendedor_"
            // Por cada archivo de ventas encontrado, lo procesa y acumula montos/cantidades.
            Files.walk(Paths.get("."))
                 .filter(path -> path.getFileName().toString().startsWith("vendedor_"))
                 .forEach(path -> procesarArchivoVenta(path, mapaProductos, mapaVendedores));

            // Genera reportes CSV a partir de los mapas ya acumulados
            generarReportes(mapaVendedores, mapaProductos);

            System.out.println("¡Reportes generados!");
        } catch (Exception e) {
            // Cualquier error general se reporta por consola
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    // -------- MÉTODOS GENÉRICOS / UTILITARIOS --------

    /**
     * Carga un archivo CSV (línea a línea) y crea un Map<K, T>.
     *
     * @param archivo     Nombre del archivo a leer.
     * @param constructor Función que transforma cada línea en una instancia T.
     * @param getKey      Función que extrae la clave K de cada T para el Map.
     * @param <T>         Tipo de objeto a construir por línea (Producto, Vendedor, etc.).
     * @param <K>         Tipo de la clave del mapa (String generalmente).
     * @return            Mapa con clave K y valor T.
     */
    private static <T, K> Map<K, T> cargarDatos(
            String archivo,
            Function<String, T> constructor,
            Function<T, K> getKey) throws IOException {

        // Files.lines abre un Stream<String> con cada línea del archivo
        try (var stream = Files.lines(Paths.get(archivo))) {
            // Se mapea cada línea al objeto T y se colecciona a un Map usando la clave extraída con getKey
            return stream
                .map(constructor)
                .collect(Collectors.toMap(getKey, Function.identity()));
        }
    }

    /**
     * Procesa un archivo de ventas de un vendedor específico.
     * Formato esperado:
     *   Línea 0: "TIPO_DOC;NUM_DOC"  (identifica al vendedor)
     *   Desde línea 1: "idProducto;cantidad;" (una venta por línea)
     *
     * @param archivo Ruta del archivo vendedor_*.csv
     * @param prods   Mapa de productos por id (para consultar precio y acumular cantidad)
     * @param vends   Mapa de vendedores por numDoc (para acumular total vendido)
     */
    private static void procesarArchivoVenta(
            Path archivo,
            Map<String, Producto> prods,
            Map<String, Vendedor> vends) {

        try {
            // Lee todo el contenido del archivo en memoria (lista de líneas)
            List<String> lineas = Files.readAllLines(archivo);

            // De la primera línea obtiene el numDoc del vendedor (posición 1)
            String idVendedor = lineas.get(0).split(";")[1];

            // Busca al vendedor. Si no existe en el mapa, no procesa este archivo.
            Vendedor vendedor = vends.get(idVendedor);
            if (vendedor == null) return;

            // Recorre las líneas de ventas (desde la 1 en adelante)
            for (int i = 1; i < lineas.size(); i++) {
                String[] datos = lineas.get(i).split(";");
                // datos[0] = idProducto, datos[1] = cantidad
                Producto producto = prods.get(datos[0]);
                int cantidad = Integer.parseInt(datos[1]);

                if (producto != null) {
                    // Acumula total vendido por el vendedor (precio * cantidad)
                    vendedor.ventasTotales += producto.precio * cantidad;
                    // Acumula cantidad vendida por producto
                    producto.cantidadVendida += cantidad;
                }
            }
        } catch (Exception e) {
            // Si hay cualquier problema (formato, lectura, etc.), se advierte el nombre del archivo
            System.err.println("ADVERTENCIA: " + archivo.getFileName());
        }
    }

    /**
     * Genera dos archivos de reporte a partir de los mapas acumulados:
     *  - reporte_vendedores.csv: vendedores ordenados por ventasTotales desc.
     *  - reporte_productos.csv : productos ordenados por cantidadVendida desc.
     */
    private static void generarReportes(
            Map<String, Vendedor> mapaVendedores,
            Map<String, Producto> mapaProductos) throws IOException {

        // Ordena vendedores por ventasTotales de mayor a menor
        List<Vendedor> vendedoresOrdenados = mapaVendedores.values().stream()
            .sorted(Comparator.comparingDouble((Vendedor v) -> v.ventasTotales).reversed())
            .collect(Collectors.toList());

        // Escribe "Nombre Apellido;Total" con dos decimales
        try (PrintWriter writer = new PrintWriter("reporte_vendedores.csv")) {
            for (Vendedor v : vendedoresOrdenados) {
                writer.printf("%s %s;%.2f%n", v.nombres, v.apellidos, v.ventasTotales);
            }
        }

        // Ordena productos por cantidadVendida de mayor a menor
        List<Producto> productosOrdenados = mapaProductos.values().stream()
            .sorted(Comparator.comparingInt((Producto p) -> p.cantidadVendida).reversed())
            .collect(Collectors.toList());

        // Escribe "NombreProducto;Precio" (nota: no incluye la cantidad vendida en el CSV)
        try (PrintWriter writer = new PrintWriter("reporte_productos.csv")) {
            for (Producto p : productosOrdenados) {
                writer.printf("%s;%.2f%n", p.nombre, p.precio);
            }
        }
    }
}
