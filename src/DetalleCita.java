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

        setTitle("Detalles de la Cita #" + idCita);
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel lblTitulo = new JLabel("Tratamiento e Indicaciones:");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setBounds(30, 20, 300, 25);
        add(lblTitulo);

        txtTratamiento = new JTextArea();
        txtTratamiento.setEditable(false);
        txtTratamiento.setLineWrap(true);
        txtTratamiento.setWrapStyleWord(true);

        JScrollPane scrollTratamiento = new JScrollPane(txtTratamiento);
        scrollTratamiento.setBounds(30, 50, 520, 100);
        add(scrollTratamiento);

        JLabel lblMeds = new JLabel("Medicamentos Recetados:");
        lblMeds.setFont(new Font("Arial", Font.BOLD, 16));
        lblMeds.setBounds(30, 170, 300, 25);
        add(lblMeds);

        String[] columnas = {"Medicamento", "Dosis", "Frecuencia", "Duración"};
        
        modeloMedicinas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaMedicinas = new JTable(modeloMedicinas);

        JScrollPane scrollMedicinas = new JScrollPane(tablaMedicinas);
        scrollMedicinas.setBounds(30, 200, 520, 200);
        add(scrollMedicinas);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setBounds(250, 420, 100, 30);
        add(btnCerrar);
        btnCerrar.addActionListener(e -> dispose());

        cargarDatos();
    }

    private void cargarDatos() {
        try {
            Connection conexion = Conexion.getConexion();

            String sqlTratamiento = "SELECT ID_TRATAMIENTO, DESCRIPCION, DURACION_DIAS " +
                    "FROM TRATAMIENTO WHERE ID_CITA = ?";

            PreparedStatement pst = conexion.prepareStatement(sqlTratamiento);
            pst.setInt(1, this.idCita);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int idTratamiento = rs.getInt("ID_TRATAMIENTO");
                String descripcion = rs.getString("DESCRIPCION");
                int dias = rs.getInt("DURACION_DIAS");

                txtTratamiento.setText(descripcion + "\n\n(Duración estimada: " + dias + " días)");

                cargarMedicamentos(idTratamiento, conexion);

            } else {
                txtTratamiento.setText("No hay tratamiento registrado para esta cita aún.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar detalles: " + e.getMessage());
        }
    }

    private void cargarMedicamentos(int idTratamiento, Connection conexion) throws SQLException {
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
                    rs.getString("DURACION") + " días"
            };
            modeloMedicinas.addRow(fila);
        }
    }
}