import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.toedter.calendar.JDateChooser;

public class EditarPaciente extends JDialog {
    private int idPaciente;
    private int idUsuario; // Necesitamos ambos IDs para actualizar las dos tablas
    
    // Datos de Usuario
    private JTextField txtCedula, txtNombre, txtApellido, txtEmail, txtTelefono;
    // Datos de Paciente
    private JComboBox<String> cmbSexo, cmbSangre;
    private JTextField txtDireccion;
    private JSpinner spinPeso, spinAltura;
    private JDateChooser dateNacimiento;

    public EditarPaciente(int idPaciente) {
        this.idPaciente = idPaciente;
        
        setTitle("Editar Datos del Paciente #" + idPaciente);
        setModal(true);
        setSize(500, 700);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel lblTitulo1 = new JLabel("1. Datos Personales (Usuario)");
        lblTitulo1.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitulo1.setForeground(Color.BLUE);
        lblTitulo1.setBounds(30, 20, 300, 20);
        add(lblTitulo1);

        agregarCampo(30, 50, "Cédula:", txtCedula = new JTextField());
        txtCedula.setEditable(false); // La cédula NO se debe cambiar por seguridad
        
        agregarCampo(30, 90, "Nombre:", txtNombre = new JTextField());
        agregarCampo(30, 130, "Apellido:", txtApellido = new JTextField());
        agregarCampo(30, 170, "Email:", txtEmail = new JTextField());
        agregarCampo(30, 210, "Teléfono:", txtTelefono = new JTextField());

        JSeparator sep = new JSeparator();
        sep.setBounds(30, 250, 420, 10);
        add(sep);

        JLabel lblTitulo2 = new JLabel("2. Ficha Médica");
        lblTitulo2.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitulo2.setForeground(new Color(0, 100, 0));
        lblTitulo2.setBounds(30, 270, 300, 20);
        add(lblTitulo2);

        JLabel lblNac = new JLabel("Fecha Nacimiento:");
        lblNac.setBounds(30, 300, 120, 25);
        add(lblNac);
        dateNacimiento = new JDateChooser();
        dateNacimiento.setBounds(150, 300, 150, 25);
        dateNacimiento.setDateFormatString("yyyy-MM-dd");
        add(dateNacimiento);

        JLabel lblSexo = new JLabel("Sexo:");
        lblSexo.setBounds(30, 340, 100, 25);
        add(lblSexo);
        String[] sexos = {"M", "F", "O"};
        cmbSexo = new JComboBox<>(sexos);
        cmbSexo.setBounds(150, 340, 80, 25);
        add(cmbSexo);

        JLabel lblSangre = new JLabel("Grupo:");
        lblSangre.setBounds(250, 340, 120, 25);
        add(lblSangre);
        String[] tipos = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        cmbSangre = new JComboBox<>(tipos);
        cmbSangre.setBounds(370, 340, 80, 25);
        add(cmbSangre);

        agregarCampo(30, 380, "Dirección:", txtDireccion = new JTextField());

        JLabel lblPeso = new JLabel("Peso (kg):");
        lblPeso.setBounds(30, 420, 100, 25);
        add(lblPeso);
        spinPeso = new JSpinner(new SpinnerNumberModel(70.0, 1.0, 300.0, 0.1));
        spinPeso.setBounds(150, 420, 80, 25);
        add(spinPeso);

        JLabel lblAltura = new JLabel("Altura (m):");
        lblAltura.setBounds(250, 420, 100, 25);
        add(lblAltura);
        spinAltura = new JSpinner(new SpinnerNumberModel(1.70, 0.3, 2.5, 0.01));
        spinAltura.setBounds(370, 420, 80, 25);
        add(spinAltura);

        // BOTÓN GUARDAR
        JButton btnGuardar = new JButton("GUARDAR CAMBIOS 💾");
        btnGuardar.setBounds(100, 580, 280, 40);
        btnGuardar.setBackground(new Color(255, 140, 0)); // Naranja
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setOpaque(true);
        add(btnGuardar);

        btnGuardar.addActionListener(e -> guardarCambios());

        cargarDatosActuales();
    }

    private void agregarCampo(int x, int y, String titulo, JTextField campo) {
        JLabel lbl = new JLabel(titulo);
        lbl.setBounds(x, y, 100, 25);
        add(lbl);
        campo.setBounds(x + 120, y, 200, 25);
        add(campo);
    }

    private void cargarDatosActuales() {
        try {
            Connection con = Conexion.getConexion();
            // JOIN para traer todo de una sola vez
            String sql = "SELECT U.ID_USUARIO, U.CEDULA, U.NOMBRE, U.APELLIDO, U.EMAIL, U.TELEFONO, " +
                         "P.SEXO, P.DIRECCION, P.FECHA_NACIMIENTO, P.GRUPO_SANGUINEO, P.PESO, P.ALTURA " +
                         "FROM PACIENTE P JOIN USUARIO U ON P.ID_USUARIO = U.ID_USUARIO " +
                         "WHERE P.ID_PACIENTE = ?";
            
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, this.idPaciente);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                this.idUsuario = rs.getInt("ID_USUARIO"); // Guardamos esto para el UPDATE
                
                txtCedula.setText(rs.getString("CEDULA"));
                txtNombre.setText(rs.getString("NOMBRE"));
                txtApellido.setText(rs.getString("APELLIDO"));
                txtEmail.setText(rs.getString("EMAIL"));
                txtTelefono.setText(rs.getString("TELEFONO"));
                
                dateNacimiento.setDate(rs.getDate("FECHA_NACIMIENTO"));
                cmbSexo.setSelectedItem(rs.getString("SEXO"));
                cmbSangre.setSelectedItem(rs.getString("GRUPO_SANGUINEO"));
                txtDireccion.setText(rs.getString("DIRECCION"));
                spinPeso.setValue(rs.getDouble("PESO"));
                spinAltura.setValue(rs.getDouble("ALTURA"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void guardarCambios() {
        try {
            Connection con = Conexion.getConexion();
            con.setAutoCommit(false); // TRANSACCIÓN

            try {
                // 1. Actualizar USUARIO
                String sqlUser = "UPDATE USUARIO SET NOMBRE=?, APELLIDO=?, EMAIL=?, TELEFONO=? WHERE ID_USUARIO=?";
                PreparedStatement pstUser = con.prepareStatement(sqlUser);
                pstUser.setString(1, txtNombre.getText());
                pstUser.setString(2, txtApellido.getText());
                pstUser.setString(3, txtEmail.getText());
                pstUser.setString(4, txtTelefono.getText());
                pstUser.setInt(5, this.idUsuario);
                pstUser.executeUpdate();

                String sqlPac = "UPDATE PACIENTE SET SEXO=?, DIRECCION=?, FECHA_NACIMIENTO=?, GRUPO_SANGUINEO=?, PESO=?, ALTURA=? WHERE ID_PACIENTE=?";
                PreparedStatement pstPac = con.prepareStatement(sqlPac);
                pstPac.setString(1, (String) cmbSexo.getSelectedItem());
                pstPac.setString(2, txtDireccion.getText());
                pstPac.setDate(3, new java.sql.Date(dateNacimiento.getDate().getTime()));
                pstPac.setString(4, (String) cmbSangre.getSelectedItem());
                pstPac.setDouble(5, (double) spinPeso.getValue());
                pstPac.setDouble(6, (double) spinAltura.getValue());
                pstPac.setInt(7, this.idPaciente);
                pstPac.executeUpdate();

                con.commit();
                JOptionPane.showMessageDialog(this, "Datos actualizados correctamente.");
                dispose();

            } catch (SQLException e) {
                con.rollback();
                JOptionPane.showMessageDialog(this, "Error al actualizar: " + e.getMessage());
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}