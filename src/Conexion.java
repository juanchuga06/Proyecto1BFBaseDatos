import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    // 1. Configuración de la conexión
    // "jdbc:mysql://localhost:3306/NOMBRE_DE_TU_BASE_DE_DATOS"
    private static final String URL = "jdbc:mysql://localhost:3306/SISTEMACITASMEDICAS";
    private static final String USER = "root"; // Tu usuario de MySQL (usualmente es root)
    private static final String PASSWORD = "1987"; // <--- ¡PON TU CONTRASEÑA DE MYSQL AQUÍ!

    // Método para obtener la conexión
    public static Connection getConexion() {
        Connection conexion = null;
        try {
            // Intentamos conectar
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ ¡Conexión exitosa a la base de datos!");
        } catch (SQLException e) {
            System.out.println("❌ Error al conectar: " + e.getMessage());
        }
        return conexion;
    }

    // Método main solo para probar que funciona ahora mismo
    public static void main(String[] args) {
        getConexion();
    }
}