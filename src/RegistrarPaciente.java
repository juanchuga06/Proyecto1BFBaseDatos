import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.toedter.calendar.JDateChooser;

public class RegistrarPaciente extends JDialog {
    // Datos de Usuario
    private JTextField txtCedula, txtNombre, txtApellido, txtEmail, txtTelefono, txtPass;

    // Datos de Paciente
    private JComboBox<String> cmbSexo, cmbSangre;
    private JTextField txtDireccion;
    private JSpinner spinPeso, spinAltura;
    private JDateChooser dateNacimiento;

    public RegistrarPaciente() {
        setTitle("Registro de Nuevo Paciente");
        setModal(true);
        setSize(500, 750);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel lblTitulo1 = new JLabel("1. Datos Personales y de Cuenta");
        lblTitulo1.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitulo1.setForeground(Color.BLUE);
        lblTitulo1.setBounds(30, 20, 300, 20);
        add(lblTitulo1);

        agregarCampo(30, 50, "Cédula:", txtCedula = new JTextField());
        agregarCampo(30, 90, "Nombre:", txtNombre = new JTextField());
        agregarCampo(30, 130, "Apellido:", txtApellido = new JTextField());
        agregarCampo(30, 170, "Email:", txtEmail = new JTextField());
        agregarCampo(30, 210, "Teléfono:", txtTelefono = new JTextField());
        agregarCampo(30, 250, "Contraseña:", txtPass = new JTextField());

        JSeparator sep = new JSeparator();
        sep.setBounds(30, 290, 420, 10);
        add(sep);

        JLabel lblTitulo2 = new JLabel("2. Ficha Médica Inicial");
        lblTitulo2.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitulo2.setForeground(new Color(0, 100, 0));
        lblTitulo2.setBounds(30, 310, 300, 20);
        add(lblTitulo2);

        JLabel lblNac = new JLabel("Fecha Nacimiento:");
        lblNac.setBounds(30, 340, 120, 25);
        add(lblNac);
        dateNacimiento = new JDateChooser();
        dateNacimiento.setBounds(150, 340, 150, 25);
        dateNacimiento.setDateFormatString("yyyy-MM-dd");
        add(dateNacimiento);

        JLabel lblSexo = new JLabel("Sexo:");
        lblSexo.setBounds(30, 380, 100, 25);
        add(lblSexo);
        String[] sexos = {"M", "F", "O"};
        cmbSexo = new JComboBox<>(sexos);
        cmbSexo.setBounds(150, 380, 80, 25);
        add(cmbSexo);

        JLabel lblSangre = new JLabel("Grupo Sanguíneo:");
        lblSangre.setBounds(250, 380, 120, 25);
        add(lblSangre);
        String[] tipos = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        cmbSangre = new JComboBox<>(tipos);
        cmbSangre.setBounds(370, 380, 80, 25);
        add(cmbSangre);

        agregarCampo(30, 420, "Dirección:", txtDireccion = new JTextField());

        JLabel lblPeso = new JLabel("Peso (kg):");
        lblPeso.setBounds(30, 460, 100, 25);
        add(lblPeso);
        spinPeso = new JSpinner(new SpinnerNumberModel(70.0, 1.0, 300.0, 0.1));
        spinPeso.setBounds(150, 460, 80, 25);
        add(spinPeso);

        JLabel lblAltura = new JLabel("Altura (m):");
        lblAltura.setBounds(250, 460, 100, 25);
        add(lblAltura);
        spinAltura = new JSpinner(new SpinnerNumberModel(1.70, 0.3, 2.5, 0.01));
        spinAltura.setBounds(370, 460, 80, 25);
        add(spinAltura);

        JButton btnGuardar = new JButton("REGISTRAR PACIENTE");
        btnGuardar.setBounds(100, 620, 280, 40);
        btnGuardar.setBackground(new Color(50, 100, 200));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 14));
        
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setOpaque(true);
        add(btnGuardar);

        btnGuardar.addActionListener(e -> guardarPaciente());
    }

    private void agregarCampo(int x, int y, String titulo, JTextField campo) {
        JLabel lbl = new JLabel(titulo);
        lbl.setBounds(x, y, 100, 25);
        add(lbl);
        campo.setBounds(x + 120, y, 200, 25);
        add(campo);
    }

    private void guardarPaciente() {
        // Validaciones básicas
        if (txtCedula.getText().isEmpty() || txtNombre.getText().isEmpty() || dateNacimiento.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Por favor completa los campos obligatorios.");
            return;
        }

        try {
            Connection con = Conexion.getConexion();
            con.setAutoCommit(false); // INICIO TRANSACCIÓN

            try {
                // 1. Insertar en USUARIO
                String sqlUser = "INSERT INTO USUARIO (CEDULA, NOMBRE, APELLIDO, EMAIL, TELEFONO, CONTRASENA, ROL, ESTADO_USUARIO) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 'P', 1)";

                PreparedStatement pstUser = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
                pstUser.setString(1, txtCedula.getText());
                pstUser.setString(2, txtNombre.getText());
                pstUser.setString(3, txtApellido.getText());
                pstUser.setString(4, txtEmail.getText());
                pstUser.setString(5, txtTelefono.getText());
                pstUser.setString(6, txtPass.getText());
                pstUser.executeUpdate();

                ResultSet rsKey = pstUser.getGeneratedKeys();
                int idUsuarioNuevo = 0;
                if (rsKey.next()) idUsuarioNuevo = rsKey.getInt(1);

                // 2. Insertar en PACIENTE
                String sqlPac = "INSERT INTO PACIENTE (ID_USUARIO, SEXO, DIRECCION, FECHA_NACIMIENTO, GRUPO_SANGUINEO, PESO, ALTURA) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement pstPac = con.prepareStatement(sqlPac, Statement.RETURN_GENERATED_KEYS);
                pstPac.setInt(1, idUsuarioNuevo);
                pstPac.setString(2, (String) cmbSexo.getSelectedItem());
                pstPac.setString(3, txtDireccion.getText());

                java.sql.Date fechaSql = new java.sql.Date(dateNacimiento.getDate().getTime());
                pstPac.setDate(4, fechaSql);

                pstPac.setString(5, (String) cmbSangre.getSelectedItem());
                pstPac.setDouble(6, (double) spinPeso.getValue());
                pstPac.setDouble(7, (double) spinAltura.getValue());
                pstPac.executeUpdate();

                ResultSet rsKeyPac = pstPac.getGeneratedKeys();
                int idPacienteNuevo = 0;
                if (rsKeyPac.next()) idPacienteNuevo = rsKeyPac.getInt(1);

                // 3. Crear HISTORIA CLÍNICA automáticamente
                String sqlHist = "INSERT INTO HISTORIA_CLINICA (ID_PACIENTE, FECHA_INICIO_HISTORIA) VALUES (?, CURDATE())";
                PreparedStatement pstHist = con.prepareStatement(sqlHist);
                pstHist.setInt(1, idPacienteNuevo);
                pstHist.executeUpdate();

                con.commit();
                JOptionPane.showMessageDialog(this, "¡Paciente registrado exitosamente!\n(Historia clínica creada)");
                dispose();

            } catch (SQLException e) {
                con.rollback(); // DESHACER SI HAY ERROR
                if (e.getMessage().contains("Duplicate")) {
                    JOptionPane.showMessageDialog(this, "Error: Ya existe un usuario con esa Cédula o Email.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al registrar: " + e.getMessage());
                }
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}