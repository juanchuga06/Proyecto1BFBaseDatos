import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class DetalleCita extends JFrame {
    private int idCita;
    private JTextArea txtTratamiento;
    private JTable tablaMedicinas;
    private DefaultTableModel modeloMedicinas;

    public DetalleCita(int idCita) {
        this.idCita = idCita;

        // Configuración de la ventana
        setTitle("Detalles de la Cita #" + idCita);
        setSize(600, 500);
        // DISPOSE_ON_CLOSE cierra SOLO esta ventana, no toda la app
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // --- SECCIÓN 1: DESCRIPCIÓN DEL TRATAMIENTO ---
        JLabel lblTitulo = new JLabel("Tratamiento e Indicaciones:");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setBounds(30, 20, 300, 25);
        add(lblTitulo);

        txtTratamiento = new JTextArea();
        txtTratamiento.setEditable(false); // El paciente solo lee, no escribe
        txtTratamiento.setLineWrap(true); // Ajuste de línea automático
        txtTratamiento.setWrapStyleWord(true);

        JScrollPane scrollTratamiento = new JScrollPane(txtTratamiento);
        scrollTratamiento.setBounds(30, 50, 520, 100);
        add(scrollTratamiento);

        // --- SECCIÓN 2: MEDICAMENTOS RECETADOS ---
        JLabel lblMeds = new JLabel("Medicamentos Recetados:");
        lblMeds.setFont(new Font("Arial", Font.BOLD, 16));
        lblMeds.setBounds(30, 170, 300, 25);
        add(lblMeds);

        // Columnas para la tabla de medicinas
        String[] columnas = {"Medicamento", "Dosis", "Frecuencia", "Duración"};
        
        // Creamos el modelo pero bloqueando la edición
        modeloMedicinas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // ¡Aquí está la magia! Retorna siempre falso.
            }
        };
        tablaMedicinas = new JTable(modeloMedicinas);

        JScrollPane scrollMedicinas = new JScrollPane(tablaMedicinas);
        scrollMedicinas.setBounds(30, 200, 520, 200);
        add(scrollMedicinas);

        // Botón Cerrar
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setBounds(250, 420, 100, 30);
        add(btnCerrar);
        btnCerrar.addActionListener(e -> dispose());

        // ¡A cargar los datos!
        cargarDatos();
    }

    private void cargarDatos() {
        try {
            Connection conexion = Conexion.getConexion();

            // PASO 1: Buscar el tratamiento asociado a esta cita
            String sqlTratamiento = "SELECT ID_TRATAMIENTO, DESCRIPCION, DURACION_DIAS " +
                    "FROM TRATAMIENTO WHERE ID_CITA = ?";

            PreparedStatement pst = conexion.prepareStatement(sqlTratamiento);
            pst.setInt(1, this.idCita);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // Si encontramos tratamiento, lo mostramos
                int idTratamiento = rs.getInt("ID_TRATAMIENTO");
                String descripcion = rs.getString("DESCRIPCION");
                int dias = rs.getInt("DURACION_DIAS");

                txtTratamiento.setText(descripcion + "\n\n(Duración estimada: " + dias + " días)");

                // PASO 2: Buscar los medicamentos de ESE tratamiento
                cargarMedicamentos(idTratamiento, conexion);

            } else {
                txtTratamiento.setText("No hay tratamiento registrado para esta cita aún.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar detalles: " + e.getMessage());
        }
    }

    private void cargarMedicamentos(int idTratamiento, Connection conexion) throws SQLException {
        // Unimos la tabla intermedia con la de medicamentos para saber los nombres
        String sqlMeds = "SELECT M.NOMBRE_MEDICAMENTO, TM.DOSIS, TM.FRECUENCIA, TM.DURACION " +
                "FROM TRATAMIENTO_MEDICAMENTO TM " +
                "JOIN MEDICAMENTO M ON TM.ID_MEDICAMENTO = M.ID_MEDICAMENTO " +
                "WHERE TM.ID_TRATAMIENTO = ?";

        PreparedStatement pst = conexion.prepareStatement(sqlMeds);
        pst.setInt(1, idTratamiento);

        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Object[] fila = {
                    rs.getString("NOMBRE_MEDICAMENTO"),
                    rs.getString("DOSIS"),
                    rs.getString("FRECUENCIA"),
                    rs.getString("DURACION") + " días" // Agregamos "días" para que se entienda
            };
            modeloMedicinas.addRow(fila);
        }
    }
}