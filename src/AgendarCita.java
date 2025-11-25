import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;
import com.toedter.calendar.JDateChooser;

public class AgendarCita extends JDialog {
    private int idPaciente;
    private JComboBox<ItemDoctor> cmbDoctores;
    private JDateChooser dateChooser;
    private JComboBox<String> cmbHora;
    private JTextArea txtMotivo;

    public AgendarCita(int idPaciente) {
        this.idPaciente = idPaciente;

        setModal(true);

        setTitle("Agendar Nueva Cita");
        setSize(450, 550);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel lblDoc = new JLabel("Seleccione Doctor:");
        lblDoc.setBounds(30, 30, 200, 20);
        add(lblDoc);

        cmbDoctores = new JComboBox<>();
        cmbDoctores.setBounds(30, 55, 350, 30);
        add(cmbDoctores);
        cargarDoctores();

        JLabel lblFecha = new JLabel("Fecha de la cita (Máx 7 días):");
        lblFecha.setBounds(30, 100, 200, 20);
        add(lblFecha);

        dateChooser = new JDateChooser();
        dateChooser.setBounds(30, 125, 150, 30);
        dateChooser.setDateFormatString("yyyy-MM-dd");

        Date hoy = new Date();
        dateChooser.setMinSelectableDate(hoy);
        dateChooser.setDate(hoy);

        Calendar limite = Calendar.getInstance();
        limite.add(Calendar.DAY_OF_YEAR, 7);
        dateChooser.setMaxSelectableDate(limite.getTime());

        add(dateChooser);

        JLabel lblHora = new JLabel("Horario Disponible:");
        lblHora.setBounds(200, 100, 150, 20);
        add(lblHora);

        cmbHora = new JComboBox<>();
        cmbHora.setBounds(200, 125, 120, 30);
        add(cmbHora);
        cargarHorarios();

        JLabel lblMotivo = new JLabel("Motivo de consulta:");
        lblMotivo.setBounds(30, 170, 200, 20);
        add(lblMotivo);

        txtMotivo = new JTextArea();
        txtMotivo.setLineWrap(true);
        JScrollPane scrollMotivo = new JScrollPane(txtMotivo);
        scrollMotivo.setBounds(30, 195, 350, 100);
        add(scrollMotivo);

        JButton btnGuardar = new JButton("Agendar Cita");
        btnGuardar.setBounds(120, 330, 180, 40);
        btnGuardar.setBackground(new Color(0, 100, 0));
        btnGuardar.setForeground(Color.WHITE);
        
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setOpaque(true);
        add(btnGuardar);

        btnGuardar.addActionListener(e -> intentarGuardar());
    }

    private void cargarHorarios() {
        int horaInicio = 8;
        int horaFin = 16;
        for (int h = horaInicio; h <= horaFin; h++) {
            for (int m = 0; m < 60; m += 20) {
                String horaStr = String.format("%02d:%02d", h, m);
                cmbHora.addItem(horaStr);
            }
        }
    }

    private void cargarDoctores() {
        try {
            Connection con = Conexion.getConexion();
            String sql = "SELECT D.ID_DOCTOR, U.NOMBRE, U.APELLIDO " +
                    "FROM DOCTOR D JOIN USUARIO U ON D.ID_USUARIO = U.ID_USUARIO";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                cmbDoctores.addItem(new ItemDoctor(rs.getInt("ID_DOCTOR"),
                        rs.getString("NOMBRE") + " " + rs.getString("APELLIDO")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void intentarGuardar() {
        Date fechaSeleccionada = dateChooser.getDate();

        if (fechaSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Por favor selecciona una fecha.");
            return;
        }

        String horaTexto = (String) cmbHora.getSelectedItem();
        ItemDoctor doctor = (ItemDoctor) cmbDoctores.getSelectedItem();

        java.sql.Time horaSql = java.sql.Time.valueOf(horaTexto + ":00");
        java.sql.Date fechaSql = new java.sql.Date(fechaSeleccionada.getTime());

        if (elDoctorEstaOcupado(doctor.getId(), fechaSql, horaSql)) {
            JOptionPane.showMessageDialog(this,
                    "El doctor " + doctor + " ya tiene una cita a esa hora.\nPor favor selecciona otro horario.",
                    "Horario no disponible", JOptionPane.WARNING_MESSAGE);
            return;
        }

        guardarCita(doctor.getId(), fechaSql, horaSql);
    }

    private boolean elDoctorEstaOcupado(int idDoctor, java.sql.Date fecha, java.sql.Time hora) {
        boolean ocupado = false;
        try {
            Connection con = Conexion.getConexion();
            String sql = "SELECT COUNT(*) FROM CITA WHERE ID_DOCTOR = ? " +
                    "AND FECHA_CITA = ? AND HORA_INICIO = ? AND ESTADO_CITA IN ('P', 'A')";

            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, idDoctor);
            pst.setDate(2, fecha);
            pst.setTime(3, hora);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                ocupado = (rs.getInt(1) > 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ocupado;
    }

    private void guardarCita(int idDoctor, java.sql.Date fecha, java.sql.Time hora) {
        try {
            Connection con = Conexion.getConexion();
            String sql = "INSERT INTO CITA (ID_PACIENTE, ID_DOCTOR, FECHA_CITA, HORA_INICIO, MOTIVO_CONSULTA, ESTADO_CITA) " +
                    "VALUES (?, ?, ?, ?, ?, 'P')";

            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, this.idPaciente);
            pst.setInt(2, idDoctor);
            pst.setDate(3, fecha);
            pst.setTime(4, hora);
            pst.setString(5, txtMotivo.getText());

            int filas = pst.executeUpdate();
            if (filas > 0) {
                JOptionPane.showMessageDialog(this, "¡Cita agendada con éxito!");
                dispose();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}