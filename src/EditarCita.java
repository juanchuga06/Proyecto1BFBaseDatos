import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;
import com.toedter.calendar.JDateChooser;

public class EditarCita extends JDialog {
    private int idCita;
    private int idPaciente; // NUEVO: Necesitamos saber de quién es la cita
    private JComboBox<ItemDoctor> cmbDoctores;
    private JDateChooser dateChooser;
    private JComboBox<String> cmbHora;
    private JTextArea txtMotivo;

    public EditarCita(int idCita) {
        this.idCita = idCita;

        setTitle("Editar Cita #" + idCita);
        setSize(450, 550);
        setModal(true); // Bloquea la ventana de atrás
        setLocationRelativeTo(null);
        setLayout(null);

        // 1. SELECCIÓN DE DOCTOR
        JLabel lblDoc = new JLabel("Doctor:");
        lblDoc.setBounds(30, 30, 200, 20);
        add(lblDoc);

        cmbDoctores = new JComboBox<>();
        cmbDoctores.setBounds(30, 55, 350, 30);
        add(cmbDoctores);
        cargarDoctores(); // Llenamos la lista

        // 2. FECHA
        JLabel lblFecha = new JLabel("Fecha:");
        lblFecha.setBounds(30, 100, 200, 20);
        add(lblFecha);

        dateChooser = new JDateChooser();
        dateChooser.setBounds(30, 125, 150, 30);
        dateChooser.setDateFormatString("yyyy-MM-dd");
        
        // Restricciones (Igual que al agendar)
        Date hoy = new Date();
        dateChooser.setMinSelectableDate(hoy);
        add(dateChooser);

        // 3. HORA
        JLabel lblHora = new JLabel("Hora:");
        lblHora.setBounds(200, 100, 150, 20);
        add(lblHora);

        cmbHora = new JComboBox<>();
        cmbHora.setBounds(200, 125, 120, 30);
        add(cmbHora);
        cargarHorarios();

        // 4. MOTIVO
        JLabel lblMotivo = new JLabel("Motivo:");
        lblMotivo.setBounds(30, 170, 200, 20);
        add(lblMotivo);

        txtMotivo = new JTextArea();
        txtMotivo.setLineWrap(true);
        JScrollPane scrollMotivo = new JScrollPane(txtMotivo);
        scrollMotivo.setBounds(30, 195, 350, 100);
        add(scrollMotivo);

        // 5. BOTÓN GUARDAR CAMBIOS
        JButton btnGuardar = new JButton("GUARDAR CAMBIOS 💾");
        btnGuardar.setBounds(100, 330, 220, 40);
        btnGuardar.setBackground(new Color(255, 140, 0)); // Naranja para diferenciar de "Crear"
        btnGuardar.setForeground(Color.WHITE);
        
        // Estilo plano
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setOpaque(true);
        
        add(btnGuardar);
        btnGuardar.addActionListener(e -> intentarGuardar());

        // --- ¡IMPORTANTE! CARGAR LOS DATOS DE LA CITA ---
        cargarDatosActuales();
    }

    private void cargarHorarios() {
        int horaInicio = 8;
        int horaFin = 16;
        for (int h = horaInicio; h <= horaFin; h++) {
            for (int m = 0; m < 60; m += 20) {
                cmbHora.addItem(String.format("%02d:%02d", h, m));
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
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void cargarDatosActuales() {
        try {
            Connection con = Conexion.getConexion();
            // CAMBIO: Ahora traemos también el ID_PACIENTE
            String sql = "SELECT ID_PACIENTE, ID_DOCTOR, FECHA_CITA, HORA_INICIO, MOTIVO_CONSULTA FROM CITA WHERE ID_CITA = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, this.idCita);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                this.idPaciente = rs.getInt("ID_PACIENTE"); // ¡Lo guardamos!
                
                // 1. Poner Fecha
                dateChooser.setDate(rs.getDate("FECHA_CITA"));
                
                // 2. Poner Hora (Convertimos 08:00:00 a 08:00)
                String hora = rs.getTime("HORA_INICIO").toString().substring(0, 5);
                cmbHora.setSelectedItem(hora);
                
                // 3. Poner Motivo
                txtMotivo.setText(rs.getString("MOTIVO_CONSULTA"));

                // 4. Seleccionar al Doctor correcto en el ComboBox
                int idDoc = rs.getInt("ID_DOCTOR");
                for (int i = 0; i < cmbDoctores.getItemCount(); i++) {
                    ItemDoctor item = cmbDoctores.getItemAt(i);
                    if (item.getId() == idDoc) {
                        cmbDoctores.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage());
        }
    }

    private void intentarGuardar() {
        Date fechaSeleccionada = dateChooser.getDate();
        if (fechaSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una fecha.");
            return;
        }

        // Validar disponibilidad (¡Igual que al agendar!)
        ItemDoctor doctor = (ItemDoctor) cmbDoctores.getSelectedItem();
        String horaTexto = (String) cmbHora.getSelectedItem();
        java.sql.Time horaSql = java.sql.Time.valueOf(horaTexto + ":00");
        java.sql.Date fechaSql = new java.sql.Date(fechaSeleccionada.getTime());

        // VALIDACIÓN 1: ¿El DOCTOR está libre?
        if (elDoctorEstaOcupado(doctor.getId(), fechaSql, horaSql)) {
            JOptionPane.showMessageDialog(this, "El doctor ya tiene otra cita a esa hora.", "Conflicto de Doctor", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // VALIDACIÓN 2: ¿El PACIENTE está libre? (¡NUEVO!)
        if (elPacienteEstaOcupado(this.idPaciente, fechaSql, horaSql)) {
            JOptionPane.showMessageDialog(this, "El paciente ya tiene otra cita a esa misma hora.", "Conflicto de Paciente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Si pasa ambas, guardamos (UPDATE)
        try {
            Connection con = Conexion.getConexion();
            String sql = "UPDATE CITA SET ID_DOCTOR=?, FECHA_CITA=?, HORA_INICIO=?, MOTIVO_CONSULTA=? WHERE ID_CITA=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, doctor.getId());
            pst.setDate(2, fechaSql);
            pst.setTime(3, horaSql);
            pst.setString(4, txtMotivo.getText());
            pst.setInt(5, this.idCita);

            int filas = pst.executeUpdate();
            if (filas > 0) {
                JOptionPane.showMessageDialog(this, "¡Cambios guardados correctamente!");
                dispose();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + e.getMessage());
        }
    }

    private boolean elDoctorEstaOcupado(int idDoctor, java.sql.Date fecha, java.sql.Time hora) {
        try {
            Connection con = Conexion.getConexion();
            // ¡OJO! Importante: AND ID_CITA != ?
            // (Para que no diga que está ocupado por MÍ MISMO si no cambié la hora)
            String sql = "SELECT COUNT(*) FROM CITA WHERE ID_DOCTOR = ? AND FECHA_CITA = ? AND HORA_INICIO = ? " +
                         "AND ESTADO_CITA IN ('P', 'A') AND ID_CITA != ?";
            
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, idDoctor);
            pst.setDate(2, fecha);
            pst.setTime(3, hora);
            pst.setInt(4, this.idCita); // Excluimos la cita actual
            
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ¡NUEVO MÉTODO!
    private boolean elPacienteEstaOcupado(int idPaciente, java.sql.Date fecha, java.sql.Time hora) {
        try {
            Connection con = Conexion.getConexion();
            // Buscamos si este paciente ya tiene cita a esa hora, ignorando la que estamos editando
            String sql = "SELECT COUNT(*) FROM CITA WHERE ID_PACIENTE = ? AND FECHA_CITA = ? AND HORA_INICIO = ? " +
                         "AND ESTADO_CITA IN ('P', 'A') AND ID_CITA != ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, idPaciente);
            pst.setDate(2, fecha);
            pst.setTime(3, hora);
            pst.setInt(4, this.idCita);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}