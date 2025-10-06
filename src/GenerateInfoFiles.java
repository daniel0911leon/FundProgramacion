import java.io.PrintWriter;
import java.util.Random;

/**
 * Clase encargada de generar archivos CSV de prueba para un sistema de ventas.
 * <p>
 * Archivos generados:
 * <ul>
 *   <li><b>productos.csv</b>: contiene {@code id;nombre;precio}.</li>
 *   <li><b>vendedores.csv</b>: contiene {@code tipoDoc;numDoc;nombres;apellidos}.</li>
 *   <li><b>vendedor_{id}.csv</b>: archivo individual por vendedor, cuya primera línea
 *       es {@code tipoDoc;id} y las siguientes representan ventas en formato
 *       {@code idProducto;cantidad;}.</li>
 * </ul>
 * <p>
 * Los datos se generan de forma pseudoaleatoria a partir de arreglos de nombres,
 * apellidos, tipos de documento, productos y precios base.
 * </p>
 *
 * @author  Lina Gissela Aya Machado
 * @author  Diego Harney Casallas Sanchez
 * @author  Daniel Enrique León Diaz
 * @author  Laura Cáceres Palma
 * @version 1.0
 */
public class GenerateInfoFiles {

    /** Pool de nombres para generación aleatoria de vendedores. */
    private static final String[] NOMBRES = {"Carlos", "Ana", "Luis", "Maria", "Juan"};

    /** Pool de apellidos para generación aleatoria de vendedores. */
    private static final String[] APELLIDOS = {"Gomez", "Perez", "Rodriguez", "Martinez", "Diaz"};

    /** Tipos de documento permitidos para vendedores. */
    private static final String[] TIPOS_DOC = {"CC", "CE", "TI"};

    /** Lista de nombres base de productos. */
    private static final String[] PRODUCTOS_NOMBRES = {"Laptop", "Mouse", "Teclado", "Monitor"};

    /** Lista de precios base de productos, en la misma posición que {@link #PRODUCTOS_NOMBRES}. */
    private static final double[] PRODUCTOS_PRECIOS = {2500000.50, 80000.00, 150000.99, 950000.00};

    /**
     * Método principal. Inicia la generación de archivos de productos y vendedores.
     * <p>
     * Genera:
     * <ul>
     *   <li>Archivo {@code productos.csv} con el listado de productos.</li>
     *   <li>Archivo {@code vendedores.csv} con cinco vendedores aleatorios.</li>
     *   <li>Archivos de ventas por cada vendedor generado.</li>
     * </ul>
     *
     * @param args argumentos de la línea de comandos (no utilizados en esta implementación).
     */
    public static void main(String[] args) {
        try {
            System.out.println("Iniciando generación de archivos...");

            // Genera archivo de productos
            createProductsFile(PRODUCTOS_NOMBRES.length);

            // Genera archivo con la información de 5 vendedores
            createSalesManInfoFile(5);

            System.out.println("¡Archivos generados exitosamente!");
        } catch (Exception e) { 
            System.err.println("ERROR: " + e.getMessage()); 
        }
    }

    /**
     * Crea el archivo {@code productos.csv} con el listado de productos.
     * Cada línea tiene el formato: {@code idProducto;nombre;precio}.
     *
     * @param productsCount número de productos a escribir. Se recomienda que sea
     *                      menor o igual al tamaño de {@link #PRODUCTOS_NOMBRES}.
     * @throws Exception si ocurre un error al escribir el archivo.
     */
    public static void createProductsFile(int productsCount) throws Exception {
        try (PrintWriter writer = new PrintWriter("productos.csv", "UTF-8")) {
            for (int i = 0; i < productsCount; i++) {
                writer.println((i + 1) + ";" + PRODUCTOS_NOMBRES[i] + ";" + PRODUCTOS_PRECIOS[i]);
            }
        }
    }

    /**
     * Crea el archivo {@code vendedores.csv} con la información básica de un número
     * dado de vendedores, generados de manera pseudoaleatoria.
     * <p>
     * Además, por cada vendedor genera también un archivo individual de ventas,
     * invocando a {@link #createSalesMenFile(int, String, long)}.
     * </p>
     *
     * @param salesmanCount número de vendedores a generar.
     * @throws Exception si ocurre un error al escribir los archivos.
     */
    public static void createSalesManInfoFile(int salesmanCount) throws Exception {
        Random rand = new Random();

        try (PrintWriter writer = new PrintWriter("vendedores.csv", "UTF-8")) {
            for (int i = 0; i < salesmanCount; i++) {
                long id = 100000000 + rand.nextInt(900000000); // ID de 9 dígitos
                String nombre = NOMBRES[rand.nextInt(NOMBRES.length)];

                writer.println(
                    TIPOS_DOC[rand.nextInt(TIPOS_DOC.length)] + ";" + 
                    id + ";" + 
                    nombre + ";" + 
                    APELLIDOS[rand.nextInt(APELLIDOS.length)]
                );

                // Entre 2 y 5 ventas por vendedor
                createSalesMenFile(rand.nextInt(4) + 2, nombre, id); 
            }
        }
    }

    /**
     * Genera el archivo individual de ventas para un vendedor específico.
     * <p>
     * Formato del archivo {@code vendedor_{id}.csv}:
     * <ul>
     *   <li>Línea 1: {@code tipoDoc;id}.</li>
     *   <li>Líneas siguientes: {@code idProducto;cantidad;}.</li>
     * </ul>
     * </p>
     *
     * @param randomSalesCount número de ventas aleatorias a registrar (entre 2 y 5 recomendado).
     * @param name nombre del vendedor (solo referencial, no se utiliza en el archivo).
     * @param id número de documento del vendedor.
     * @throws Exception si ocurre un error al escribir el archivo.
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id) throws Exception {
        Random rand = new Random();
        String fileName = "vendedor_" + id + ".csv";

        try (PrintWriter writer = new PrintWriter(fileName, "UTF-8")) {
            writer.println(TIPOS_DOC[rand.nextInt(TIPOS_DOC.length)] + ";" + id);

            for (int i = 0; i < randomSalesCount; i++) {
                writer.println(
                    (rand.nextInt(PRODUCTOS_NOMBRES.length) + 1) + ";" + 
                    (rand.nextInt(10) + 1) + ";"
                );
            }
        }
    }
}
