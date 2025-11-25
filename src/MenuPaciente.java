import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MenuPaciente extends JFrame {
    private int idUsuario;
    private int idPaciente;
    private JTable tablaCitas;
    private DefaultTableModel modelo;

    public MenuPaciente(int idUsuario) {
        this.idUsuario = idUsuario;
        this.idPaciente = obtenerIdPaciente(idUsuario);

        // Configuración de la ventana
        setTitle("Menú del Paciente - Usuario: " + idUsuario + " | Paciente: " + idPaciente);
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Título
        JLabel lblTitulo = new JLabel("Mis Citas Médicas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setBounds(30, 20, 300, 30);
        add(lblTitulo);

        // --- BOTÓN CERRAR SESIÓN (Arriba a la derecha) ---
        JButton btnLogout = new JButton("Cerrar Sesión 🚪");
        btnLogout.setBounds(600, 20, 150, 30);
        btnLogout.setBackground(new Color(255, 80, 80));
        btnLogout.setForeground(Color.WHITE);

        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setOpaque(true);
        add(btnLogout);

        btnLogout.addActionListener(e -> {
            this.dispose();
            new Login().setVisible(true);
        });

        String[] columnas = {"ID", "Fecha", "Hora", "Doctor", "Motivo", "Estado"};
        
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaCitas = new JTable(modelo);

        tablaCitas.setRowHeight(30);
        tablaCitas.setFont(new Font("Arial", Font.PLAIN, 14));
        tablaCitas.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        tablaCitas.getTableHeader().setBackground(new Color(230, 230, 230));

        JScrollPane scrollPane = new JScrollPane(tablaCitas);
        scrollPane.setBounds(30, 70, 720, 300);
        add(scrollPane);

        JButton btnRecargar = new JButton("Recargar Citas");
        btnRecargar.setBounds(30, 400, 150, 30);
        add(btnRecargar);

        btnRecargar.addActionListener(e -> cargarCitas());

        JButton btnVerDetalles = new JButton("Ver Detalles / Historia");
        btnVerDetalles.setBounds(200, 400, 200, 30);
        add(btnVerDetalles);

        btnVerDetalles.addActionListener(e -> {
            int filaSeleccionada = tablaCitas.getSelectedRow();

            if (filaSeleccionada != -1) {
                int idCita = (int) modelo.getValueAt(filaSeleccionada, 0);
                System.out.println("🔎 El usuario quiere ver la cita ID: " + idCita);
                new DetalleCita(idCita).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, selecciona una cita de la tabla primero.");
            }
        });

        JButton btnNuevaCita = new JButton("Agendar Nueva Cita");
        btnNuevaCita.setBounds(420, 400, 200, 30);
        btnNuevaCita.setBackground(new Color(50, 100, 200));
        btnNuevaCita.setForeground(Color.WHITE);
        btnNuevaCita.setFocusPainted(false);
        btnNuevaCita.setBorderPainted(false);
        btnNuevaCita.setOpaque(true);
        add(btnNuevaCita);

        btnNuevaCita.addActionListener(e -> {
            new AgendarCita(this.idPaciente).setVisible(true);
            cargarCitas();
        });

        JButton btnCancelar = new JButton("Cancelar Cita");
        btnCancelar.setBounds(630, 400, 150, 30);
        btnCancelar.setBackground(new Color(200, 50, 50));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setOpaque(true);
        add(btnCancelar);

        btnCancelar.addActionListener(e -> cancelarCitaSeleccionada());

        cargarCitas();
    }

    private int obtenerIdPaciente(int idUsuario) {
        int idEncontrado = -1;
        try {
            Connection conexion = Conexion.getConexion();
            String sql = "SELECT ID_PACIENTE FROM PACIENTE WHERE ID_USUARIO = ?";
            PreparedStatement pst = conexion.prepareStatement(sql);
            pst.setInt(1, idUsuario);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                idEncontrado = rs.getInt("ID_PACIENTE");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idEncontrado;
    }

    private void cargarCitas() {
        modelo.setRowCount(0);

        try {
            Connection conexion = Conexion.getConexion();

            String sql = "SELECT C.ID_CITA, C.FECHA_CITA, C.HORA_INICIO, " +
                    "CONCAT(U.NOMBRE, ' ', U.APELLIDO) AS NOMBRE_DOCTOR, " +
                    "C.MOTIVO_CONSULTA, C.ESTADO_CITA " +
                    "FROM CITA C " +
                    "JOIN DOCTOR D ON C.ID_DOCTOR = D.ID_DOCTOR " +
                    "JOIN USUARIO U ON D.ID_USUARIO = U.ID_USUARIO " +
                    "WHERE C.ID_PACIENTE = ? AND C.ESTADO_CITA != 'C'";

            PreparedStatement pst = conexion.prepareStatement(sql);
            pst.setInt(1, this.idPaciente);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String estadoLetra = rs.getString("ESTADO_CITA");
                String estadoTexto = estadoLetra;
                
                if ("P".equals(estadoLetra)) estadoTexto = "Programada";
                else if ("A".equals(estadoLetra)) estadoTexto = "Atendida";

                Object[] fila = {
                        rs.getInt("ID_CITA"),
                        rs.getDate("FECHA_CITA"),
                        rs.getTime("HORA_INICIO"),
                        rs.getString("NOMBRE_DOCTOR"),
                        rs.getString("MOTIVO_CONSULTA"),
                        estadoTexto
                };
                modelo.addRow(fila);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar citas: " + e.getMessage());
        }
    }

    private void cancelarCitaSeleccionada() {
        int fila = tablaCitas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona la cita que deseas cancelar.");
            return;
        }

        int idCita = (int) modelo.getValueAt(fila, 0);
        String estadoActual = (String) modelo.getValueAt(fila, 5);

        if ("Atendida".equals(estadoActual) || "Cancelada".equals(estadoActual)) {
            JOptionPane.showMessageDialog(this, "No puedes cancelar una cita que ya fue atendida o cancelada.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de que deseas cancelar la cita #" + idCita + "?",
                "Confirmar Cancelación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                Connection con = Conexion.getConexion();
                String sql = "UPDATE CITA SET ESTADO_CITA = 'C' WHERE ID_CITA = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setInt(1, idCita);

                int filasAfectadas = pst.executeUpdate();
                if (filasAfectadas > 0) {
                    JOptionPane.showMessageDialog(this, "Cita cancelada correctamente.");
                    cargarCitas();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al cancelar: " + e.getMessage());
            }
        }
    }
}