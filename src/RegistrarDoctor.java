import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Date;
import com.toedter.calendar.JDateChooser;

public class RegistrarDoctor extends JDialog {
    // Datos Usuario
    private JTextField txtCedula, txtNombre, txtApellido, txtEmail, txtTelefono, txtPass;
    // Datos Doctor
    private JComboBox<String> cmbEspecialidad;
    private JDateChooser dateContrato;

    public RegistrarDoctor() {
        setTitle("Registrar Nuevo Doctor");
        setModal(true);
        setSize(500, 550);
        setLocationRelativeTo(null);
        setLayout(null);

        // --- DATOS PERSONALES ---
        JLabel lblTitulo = new JLabel("Datos del Médico");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(0, 100, 0)); // Verde Doctor
        lblTitulo.setBounds(30, 20, 300, 20);
        add(lblTitulo);

        agregarCampo(30, 60, "Cédula:", txtCedula = new JTextField());
        agregarCampo(30, 100, "Nombre:", txtNombre = new JTextField());
        agregarCampo(30, 140, "Apellido:", txtApellido = new JTextField());
        agregarCampo(30, 180, "Email:", txtEmail = new JTextField());
        agregarCampo(30, 220, "Teléfono:", txtTelefono = new JTextField());
        agregarCampo(30, 260, "Contraseña:", txtPass = new JTextField());

        JSeparator sep = new JSeparator();
        sep.setBounds(30, 300, 420, 10);
        add(sep);

        // --- DATOS PROFESIONALES ---
        JLabel lblEsp = new JLabel("Especialidad:");
        lblEsp.setBounds(30, 320, 100, 25);
        add(lblEsp);

        // Lista de especialidades comunes
        String[] especialidades = {"Medicina General", "Pediatría", "Cardiología", "Dermatología", 
                                   "Ginecología", "Traumatología", "Oftalmología", "Neurología"};
        cmbEspecialidad = new JComboBox<>(especialidades);
        cmbEspecialidad.setEditable(true); // ¡Permitimos escribir una nueva si no está en la lista!
        cmbEspecialidad.setBounds(150, 320, 200, 25);
        add(cmbEspecialidad);

        JLabel lblFecha = new JLabel("Fecha Contrato:");
        lblFecha.setBounds(30, 360, 120, 25);
        add(lblFecha);
        
        dateContrato = new JDateChooser();
        dateContrato.setDateFormatString("yyyy-MM-dd");
        dateContrato.setDate(new Date()); // Hoy por defecto
        dateContrato.setBounds(150, 360, 150, 25);
        add(dateContrato);

        // BOTÓN REGISTRAR
        JButton btnGuardar = new JButton("REGISTRAR DOCTOR 👨‍⚕️");
        btnGuardar.setBounds(100, 430, 280, 40);
        btnGuardar.setBackground(new Color(0, 100, 0)); // Verde
        btnGuardar.setForeground(Color.WHITE);
        // Estilo plano
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setOpaque(true);
        add(btnGuardar);

        btnGuardar.addActionListener(e -> guardarDoctor());
    }

    private void agregarCampo(int x, int y, String titulo, JTextField campo) {
        JLabel lbl = new JLabel(titulo);
        lbl.setBounds(x, y, 100, 25);
        add(lbl);
        campo.setBounds(x + 120, y, 200, 25);
        add(campo);
    }

    private void guardarDoctor() {
        if (txtCedula.getText().isEmpty() || txtNombre.getText().isEmpty() || txtEmail.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa los campos obligatorios.");
            return;
        }

        try {
            Connection con = Conexion.getConexion();
            con.setAutoCommit(false); // Transacción

            try {
                // 1. Insertar USUARIO (Rol 'D')
                String sqlUser = "INSERT INTO USUARIO (CEDULA, NOMBRE, APELLIDO, EMAIL, TELEFONO, CONTRASENA, ROL, ESTADO_USUARIO) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, 'D', 1)";
                PreparedStatement pstUser = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
                pstUser.setString(1, txtCedula.getText());
                pstUser.setString(2, txtNombre.getText());
                pstUser.setString(3, txtApellido.getText());
                pstUser.setString(4, txtEmail.getText());
                pstUser.setString(5, txtTelefono.getText());
                pstUser.setString(6, txtPass.getText());
                pstUser.executeUpdate();

                ResultSet rs = pstUser.getGeneratedKeys();
                int idUsuario = 0;
                if (rs.next()) idUsuario = rs.getInt(1);

                // 2. Insertar DOCTOR
                String sqlDoc = "INSERT INTO DOCTOR (ID_USUARIO, ESPECIALIDAD, FECHA_CONTRATO) VALUES (?, ?, ?)";
                PreparedStatement pstDoc = con.prepareStatement(sqlDoc);
                pstDoc.setInt(1, idUsuario);
                pstDoc.setString(2, (String) cmbEspecialidad.getSelectedItem());
                pstDoc.setDate(3, new java.sql.Date(dateContrato.getDate().getTime()));
                pstDoc.executeUpdate();

                con.commit();
                JOptionPane.showMessageDialog(this, "Doctor registrado exitosamente.");
                dispose();

            } catch (SQLException e) {
                con.rollback();
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}