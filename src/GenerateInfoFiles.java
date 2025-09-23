import java.io.PrintWriter;
import java.util.Random;

/**
 * 
 * @author Integrantes:
 * Lina Gissela Aya Machado
 * Diego Harney Casallas Sanchez
 * Daniel Enrique León Diaz
 * Laura Cáceres Palma
 *
 */

public class GenerateInfoFiles {

    // Arrays con datos de prueba para generar información aleatoria.
    private static final String[] NOMBRES = {"Carlos", "Ana", "Luis", "Maria", "Juan"};
    private static final String[] APELLIDOS = {"Gomez", "Perez", "Rodriguez", "Martinez", "Diaz"};
    private static final String[] TIPOS_DOC = {"CC", "CE", "TI"}; // Tipos de documento
    private static final String[] PRODUCTOS_NOMBRES = {"Laptop", "Mouse", "Teclado", "Monitor"};
    private static final double[] PRODUCTOS_PRECIOS = {2500000.50, 80000.00, 150000.99, 950000.00};

    public static void main(String[] args) {
        try {
            System.out.println("Iniciando generación de archivos...");

            // Crea un archivo con los productos disponibles
            createProductsFile(PRODUCTOS_NOMBRES.length);

            // Crea un archivo con la información de 5 vendedores
            createSalesManInfoFile(5);

            System.out.println("¡Archivos generados exitosamente!");
        } catch (Exception e) { 
            System.err.println("ERROR: " + e.getMessage()); 
        }
    }

    // Genera el archivo productos.csv con el listado de productos
    public static void createProductsFile(int productsCount) throws Exception {
        try (PrintWriter writer = new PrintWriter("productos.csv", "UTF-8")) {
            for (int i = 0; i < productsCount; i++) {
                // Se escribe: id_producto;nombre_producto;precio
                writer.println((i + 1) + ";" + PRODUCTOS_NOMBRES[i] + ";" + PRODUCTOS_PRECIOS[i]);
            }
        }
    }

    // Genera el archivo vendedores.csv con la información básica de los vendedores
    public static void createSalesManInfoFile(int salesmanCount) throws Exception {
        Random rand = new Random();

        try (PrintWriter writer = new PrintWriter("vendedores.csv", "UTF-8")) {
            for (int i = 0; i < salesmanCount; i++) {
                // Genera un ID aleatorio de 9 dígitos
                long id = 100000000 + rand.nextInt(900000000);

                // Selecciona un nombre aleatorio
                String nombre = NOMBRES[rand.nextInt(NOMBRES.length)];

                // Escribe: tipo_doc;id;nombre;apellido
                writer.println(
                    TIPOS_DOC[rand.nextInt(TIPOS_DOC.length)] + ";" + 
                    id + ";" + 
                    nombre + ";" + 
                    APELLIDOS[rand.nextInt(APELLIDOS.length)]
                );

                // Para cada vendedor, genera también un archivo con sus ventas
                createSalesMenFile(rand.nextInt(4) + 2, nombre, id); 
                // Genera entre 2 y 5 ventas por vendedor
            }
        }
    }

    // Genera el archivo de ventas de un vendedor específico
    public static void createSalesMenFile(int randomSalesCount, String name, long id) throws Exception {
        Random rand = new Random();

        // Nombre del archivo individual para el vendedor
        String fileName = "vendedor_" + id + ".csv";

        try (PrintWriter writer = new PrintWriter(fileName, "UTF-8")) {
            // Primera línea: tipo_doc;id
            writer.println(TIPOS_DOC[rand.nextInt(TIPOS_DOC.length)] + ";" + id);

            // Se agregan las ventas del vendedor
            for (int i = 0; i < randomSalesCount; i++) {
                // Cada línea: id_producto;cantidad;
                writer.println(
                    (rand.nextInt(PRODUCTOS_NOMBRES.length) + 1) + ";" + 
                    (rand.nextInt(10) + 1) + ";"
                );
            }
        }
    }
}
