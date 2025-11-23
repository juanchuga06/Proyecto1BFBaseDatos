import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// 1. IMPORTAR LAS CLASES SQL (¡Muy importante!)
import java.sql.*;

public class Login extends JFrame {
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnIngresar;

    public Login() {
        // Configuración de la ventana
        setTitle("Sistema de Citas Médicas - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        // Componentes
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setBounds(50, 50, 80, 25);
        add(lblEmail);

        txtEmail = new JTextField();
        txtEmail.setBounds(150, 50, 200, 25);
        add(txtEmail);

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setBounds(50, 100, 80, 25);
        add(lblPass);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(150, 100, 200, 25);
        add(txtPassword);

        // Botón
        btnIngresar = new JButton("Ingresar");
        btnIngresar.setBounds(150, 160, 100, 30);
        add(btnIngresar);

        // Acción del botón
        btnIngresar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verificarUsuario();
            }
        });
    }

    private void verificarUsuario() {
        String email = txtEmail.getText();
        String password = new String(txtPassword.getPassword());

        // Usamos try-catch porque conectar a BD puede fallar
        try {
            // 2. CORREGIDO: Tipo Connection y método getConexion()
            Connection conexion = Conexion.getConexion();

            String sql = "SELECT * FROM USUARIO WHERE EMAIL = ? AND CONTRASENA = ?";
            PreparedStatement pst = conexion.prepareStatement(sql);

            System.out.println("Intentando ingresar con: " + email);
            pst.setString(1, email);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String rol = rs.getString("ROL");
                // 3. RECUPERAR EL ID (¡Esto faltaba!)
                int idUsuario = rs.getInt("ID_USUARIO");

                // 4. CORREGIDO: Comillas dobles para Strings ("P")
                switch (rol) {
                    case "P": // Paciente
                        new MenuPaciente(idUsuario).setVisible(true);
                        this.dispose();
                        break;
                    case "D": // Doctor
                        new MenuDoctor(idUsuario).setVisible(true);
                        this.dispose();
                        break;
                    case "O": // Operador
                        new MenuOperador().setVisible(true);
                        this.dispose();
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Rol no encontrado");  //Creo que esto nunca se llegará a ejecutar y por tanto puedo borrarlo
                        break;
                }
            } else {
                JOptionPane.showMessageDialog(null, "Acceso denegado");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error de conexión: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Login().setVisible(true);
    }
}