import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MenuDoctor extends JFrame {
    private int idUsuario;
    private int idDoctor;
    private JTable tablaCitas;
    private DefaultTableModel modelo;

    public MenuDoctor(int idUsuario) {
        this.idUsuario = idUsuario;
        this.idDoctor = obtenerIdDoctor(idUsuario);

        // Configuración de la ventana
        setTitle("Panel Médico - Dr. ID: " + idDoctor);
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel lblTitulo = new JLabel("Agenda de Citas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setBounds(30, 20, 300, 30);
        add(lblTitulo);

        // --- BOTÓN CERRAR SESIÓN ---
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

        String[] columnas = {"ID", "Fecha", "Hora", "Paciente", "Motivo", "Estado"};
        
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaCitas = new JTable(modelo);

        // --- MEJORAS VISUALES DE LA TABLA ---
        tablaCitas.setRowHeight(30);
        tablaCitas.setFont(new Font("Arial", Font.PLAIN, 14));
        tablaCitas.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        tablaCitas.getTableHeader().setBackground(new Color(230, 230, 230));
        // -----------------------------------

        JScrollPane scrollPane = new JScrollPane(tablaCitas);
        scrollPane.setBounds(30, 70, 720, 300);
        add(scrollPane);

        JButton btnAtender = new JButton("Atender Cita / Ver Detalles");
        btnAtender.setBounds(30, 400, 200, 30);
        btnAtender.setBackground(new Color(0, 100, 0));
        btnAtender.setForeground(Color.WHITE);
        btnAtender.setFocusPainted(false);
        btnAtender.setBorderPainted(false);
        btnAtender.setOpaque(true);
        add(btnAtender);

        btnAtender.addActionListener(e -> {
            int fila = tablaCitas.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona una cita primero.");
                return;
            }

            int idCita = (int) modelo.getValueAt(fila, 0);
            String estado = (String) modelo.getValueAt(fila, 5);

            if (!"Programada".equals(estado)) {
                JOptionPane.showMessageDialog(this, "Solo se pueden atender citas Programadas (Pendientes).");
                return;
            }

            new AtenderCita(idCita).setVisible(true);

            cargarAgenda();
        });

        cargarAgenda();
    }

    private int obtenerIdDoctor(int idUsuario) {
        int idEncontrado = -1;
        try {
            Connection con = Conexion.getConexion();
            String sql = "SELECT ID_DOCTOR FROM DOCTOR WHERE ID_USUARIO = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, idUsuario);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                idEncontrado = rs.getInt("ID_DOCTOR");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idEncontrado;
    }

    private void cargarAgenda() {
        modelo.setRowCount(0);
        try {
            Connection con = Conexion.getConexion();
            String sql = "SELECT C.ID_CITA, C.FECHA_CITA, C.HORA_INICIO, " +
                    "CONCAT(U.NOMBRE, ' ', U.APELLIDO) AS NOMBRE_PACIENTE, " +
                    "C.MOTIVO_CONSULTA, C.ESTADO_CITA " +
                    "FROM CITA C " +
                    "JOIN PACIENTE P ON C.ID_PACIENTE = P.ID_PACIENTE " +
                    "JOIN USUARIO U ON P.ID_USUARIO = U.ID_USUARIO " +
                    "WHERE C.ID_DOCTOR = ?";

            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, this.idDoctor);

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String estadoLetra = rs.getString("ESTADO_CITA");
                String estadoTexto = estadoLetra;
                
                switch (estadoLetra) {
                    case "P": estadoTexto = "Programada"; break;
                    case "A": estadoTexto = "Atendida"; break;
                    case "C": estadoTexto = "Cancelada"; break;
                }

                Object[] fila = {
                        rs.getInt("ID_CITA"),
                        rs.getDate("FECHA_CITA"),
                        rs.getTime("HORA_INICIO"),
                        rs.getString("NOMBRE_PACIENTE"),
                        rs.getString("MOTIVO_CONSULTA"),
                        estadoTexto
                };
                modelo.addRow(fila);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar agenda: " + e.getMessage());
        }
    }
}