import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.toedter.calendar.JDateChooser;

public class EditarDoctor extends JDialog {
    private int idDoctor;
    private int idUsuario;
    
    private JTextField txtCedula, txtNombre, txtApellido, txtEmail, txtTelefono;
    private JComboBox<String> cmbEspecialidad;
    private JDateChooser dateContrato;

    public EditarDoctor(int idDoctor) {
        this.idDoctor = idDoctor;
        setTitle("Editar Doctor #" + idDoctor);
        setModal(true);
        setSize(500, 550);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel lblTitulo = new JLabel("Editar Datos del Médico");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(Color.BLUE);
        lblTitulo.setBounds(30, 20, 300, 20);
        add(lblTitulo);

        agregarCampo(30, 60, "Cédula:", txtCedula = new JTextField());
        txtCedula.setEditable(false); // No editable
        agregarCampo(30, 100, "Nombre:", txtNombre = new JTextField());
        agregarCampo(30, 140, "Apellido:", txtApellido = new JTextField());
        agregarCampo(30, 180, "Email:", txtEmail = new JTextField());
        agregarCampo(30, 220, "Teléfono:", txtTelefono = new JTextField());

        JSeparator sep = new JSeparator();
        sep.setBounds(30, 260, 420, 10);
        add(sep);

        JLabel lblEsp = new JLabel("Especialidad:");
        lblEsp.setBounds(30, 280, 100, 25);
        add(lblEsp);
        String[] especialidades = {"Medicina General", "Pediatría", "Cardiología", "Dermatología", 
                                   "Ginecología", "Traumatología", "Oftalmología", "Neurología"};
        cmbEspecialidad = new JComboBox<>(especialidades);
        cmbEspecialidad.setEditable(true);
        cmbEspecialidad.setBounds(150, 280, 200, 25);
        add(cmbEspecialidad);

        JLabel lblFecha = new JLabel("Fecha Contrato:");
        lblFecha.setBounds(30, 320, 120, 25);
        add(lblFecha);
        dateContrato = new JDateChooser();
        dateContrato.setDateFormatString("yyyy-MM-dd");
        dateContrato.setBounds(150, 320, 150, 25);
        add(dateContrato);

        JButton btnGuardar = new JButton("GUARDAR CAMBIOS 💾");
        btnGuardar.setBounds(100, 400, 280, 40);
        btnGuardar.setBackground(new Color(255, 140, 0));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setOpaque(true);
        add(btnGuardar);

        btnGuardar.addActionListener(e -> guardarCambios());

        cargarDatos();
    }

    private void agregarCampo(int x, int y, String titulo, JTextField campo) {
        JLabel lbl = new JLabel(titulo);
        lbl.setBounds(x, y, 100, 25);
        add(lbl);
        campo.setBounds(x + 120, y, 200, 25);
        add(campo);
    }

    private void cargarDatos() {
        try {
            Connection con = Conexion.getConexion();
            String sql = "SELECT U.ID_USUARIO, U.CEDULA, U.NOMBRE, U.APELLIDO, U.EMAIL, U.TELEFONO, " +
                         "D.ESPECIALIDAD, D.FECHA_CONTRATO " +
                         "FROM DOCTOR D JOIN USUARIO U ON D.ID_USUARIO = U.ID_USUARIO " +
                         "WHERE D.ID_DOCTOR = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, this.idDoctor);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                this.idUsuario = rs.getInt("ID_USUARIO");
                txtCedula.setText(rs.getString("CEDULA"));
                txtNombre.setText(rs.getString("NOMBRE"));
                txtApellido.setText(rs.getString("APELLIDO"));
                txtEmail.setText(rs.getString("EMAIL"));
                txtTelefono.setText(rs.getString("TELEFONO"));
                cmbEspecialidad.setSelectedItem(rs.getString("ESPECIALIDAD"));
                dateContrato.setDate(rs.getDate("FECHA_CONTRATO"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void guardarCambios() {
        try {
            Connection con = Conexion.getConexion();
            con.setAutoCommit(false);

            try {
                // 1. Update USUARIO
                String sqlUser = "UPDATE USUARIO SET NOMBRE=?, APELLIDO=?, EMAIL=?, TELEFONO=? WHERE ID_USUARIO=?";
                PreparedStatement pstUser = con.prepareStatement(sqlUser);
                pstUser.setString(1, txtNombre.getText());
                pstUser.setString(2, txtApellido.getText());
                pstUser.setString(3, txtEmail.getText());
                pstUser.setString(4, txtTelefono.getText());
                pstUser.setInt(5, this.idUsuario);
                pstUser.executeUpdate();

                String sqlDoc = "UPDATE DOCTOR SET ESPECIALIDAD=?, FECHA_CONTRATO=? WHERE ID_DOCTOR=?";
                PreparedStatement pstDoc = con.prepareStatement(sqlDoc);
                pstDoc.setString(1, (String) cmbEspecialidad.getSelectedItem());
                pstDoc.setDate(2, new java.sql.Date(dateContrato.getDate().getTime()));
                pstDoc.setInt(3, this.idDoctor);
                pstDoc.executeUpdate();

                con.commit();
                JOptionPane.showMessageDialog(this, "Datos actualizados.");
                dispose();
            } catch (SQLException e) {
                con.rollback();
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}