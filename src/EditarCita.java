import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Date;
import com.toedter.calendar.JDateChooser;

public class EditarCita extends JDialog {
    private int idCita;
    
    private JComboBox<ItemPaciente> cmbPacientes;
    private JComboBox<ItemDoctor> cmbDoctores;
    private JDateChooser dateChooser;
    private JComboBox<String> cmbHora;
    private JComboBox<String> cmbEstado;
    private JTextArea txtMotivo;
    private JTextArea txtObservaciones;

    public EditarCita(int idCita) {
        this.idCita = idCita;

        setTitle("Editar Cita #" + idCita + " (Modo Administrador)");
        setSize(500, 700);
        setModal(true);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel lblPac = new JLabel("Paciente:");
        lblPac.setBounds(30, 20, 200, 20);
        add(lblPac);

        cmbPacientes = new JComboBox<>();
        cmbPacientes.setBounds(30, 45, 420, 30);
        add(cmbPacientes);
        cargarPacientes(); 

        JLabel lblDoc = new JLabel("Doctor:");
        lblDoc.setBounds(30, 85, 200, 20);
        add(lblDoc);

        cmbDoctores = new JComboBox<>();
        cmbDoctores.setBounds(30, 110, 420, 30);
        add(cmbDoctores);
        cargarDoctores();

        JLabel lblFecha = new JLabel("Fecha:");
        lblFecha.setBounds(30, 150, 200, 20);
        add(lblFecha);

        dateChooser = new JDateChooser();
        dateChooser.setBounds(30, 175, 200, 30);
        dateChooser.setDateFormatString("yyyy-MM-dd");
        add(dateChooser);

        JLabel lblHora = new JLabel("Hora:");
        lblHora.setBounds(250, 150, 100, 20);
        add(lblHora);

        cmbHora = new JComboBox<>();
        cmbHora.setBounds(250, 175, 120, 30);
        add(cmbHora);
        cargarHorarios();
        
        JLabel lblEstado = new JLabel("Estado:");
        lblEstado.setBounds(390, 150, 100, 20);
        add(lblEstado);
        
        String[] estados = {"Programada", "Atendida", "Cancelada"};
        cmbEstado = new JComboBox<>(estados);
        cmbEstado.setBounds(390, 175, 100, 30);
        add(cmbEstado);

        JLabel lblMotivo = new JLabel("Motivo Consulta:");
        lblMotivo.setBounds(30, 220, 200, 20);
        add(lblMotivo);

        txtMotivo = new JTextArea();
        txtMotivo.setLineWrap(true);
        JScrollPane scrollMotivo = new JScrollPane(txtMotivo);
        scrollMotivo.setBounds(30, 245, 420, 60);
        add(scrollMotivo);
        
        JLabel lblObs = new JLabel("Observaciones:");
        lblObs.setBounds(30, 315, 250, 20);
        add(lblObs);

        txtObservaciones = new JTextArea();
        txtObservaciones.setLineWrap(true);
        JScrollPane scrollObs = new JScrollPane(txtObservaciones);
        scrollObs.setBounds(30, 340, 420, 60);
        add(scrollObs);

        JButton btnGuardar = new JButton("GUARDAR CAMBIOS 💾");
        btnGuardar.setBounds(100, 600, 280, 40);
        btnGuardar.setBackground(new Color(255, 140, 0));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setOpaque(true);
        add(btnGuardar);
        
        btnGuardar.addActionListener(e -> intentarGuardar());

        cargarDatosActuales();
    }

    private void cargarHorarios() {
        for (int h = 8; h <= 16; h++) for (int m = 0; m < 60; m += 20) cmbHora.addItem(String.format("%02d:%02d", h, m));
    }
    
    private void cargarPacientes() {
        try { Connection con = Conexion.getConexion();
        ResultSet rs = con.createStatement().executeQuery("SELECT P.ID_PACIENTE, U.NOMBRE, U.APELLIDO FROM PACIENTE P JOIN USUARIO U ON P.ID_USUARIO = U.ID_USUARIO WHERE U.ESTADO_USUARIO = 1");
        while(rs.next()) cmbPacientes.addItem(new ItemPaciente(rs.getInt(1), rs.getString(2)+" "+rs.getString(3))); 
        } catch(Exception e){} 
    }
    
    private void cargarDoctores() {
        try { Connection con = Conexion.getConexion();
        ResultSet rs = con.createStatement().executeQuery("SELECT D.ID_DOCTOR, U.NOMBRE, U.APELLIDO FROM DOCTOR D JOIN USUARIO U ON D.ID_USUARIO = U.ID_USUARIO");
        while(rs.next()) cmbDoctores.addItem(new ItemDoctor(rs.getInt(1), rs.getString(2)+" "+rs.getString(3))); 
        } catch(Exception e){}
    }

    private void cargarDatosActuales() {
        try {
            Connection con = Conexion.getConexion();
            String sql = "SELECT ID_PACIENTE, ID_DOCTOR, FECHA_CITA, HORA_INICIO, MOTIVO_CONSULTA, OBSERVACIONES, ESTADO_CITA FROM CITA WHERE ID_CITA = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, this.idCita);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int idPac = rs.getInt("ID_PACIENTE");
                for (int i=0; i<cmbPacientes.getItemCount(); i++) if(cmbPacientes.getItemAt(i).getId() == idPac) cmbPacientes.setSelectedIndex(i);
                
                int idDoc = rs.getInt("ID_DOCTOR");
                for (int i=0; i<cmbDoctores.getItemCount(); i++) if(cmbDoctores.getItemAt(i).getId() == idDoc) cmbDoctores.setSelectedIndex(i);

                dateChooser.setDate(rs.getDate("FECHA_CITA"));
                cmbHora.setSelectedItem(rs.getTime("HORA_INICIO").toString().substring(0, 5));
                txtMotivo.setText(rs.getString("MOTIVO_CONSULTA"));
                txtObservaciones.setText(rs.getString("OBSERVACIONES"));
                
                String estadoLetra = rs.getString("ESTADO_CITA");
                if ("P".equals(estadoLetra)) cmbEstado.setSelectedItem("Programada");
                else if ("A".equals(estadoLetra)) cmbEstado.setSelectedItem("Atendida");
                else if ("C".equals(estadoLetra)) cmbEstado.setSelectedItem("Cancelada");
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

        ItemPaciente paciente = (ItemPaciente) cmbPacientes.getSelectedItem();
        ItemDoctor doctor = (ItemDoctor) cmbDoctores.getSelectedItem();
        String horaTexto = (String) cmbHora.getSelectedItem();
        
        String estadoTexto = (String) cmbEstado.getSelectedItem();
        String estadoLetra = "P";
        if ("Atendida".equals(estadoTexto)) estadoLetra = "A";
        else if ("Cancelada".equals(estadoTexto)) estadoLetra = "C";
        
        java.sql.Time horaSql = java.sql.Time.valueOf(horaTexto + ":00");
        java.sql.Date fechaSql = new java.sql.Date(fechaSeleccionada.getTime());

        if (!"C".equals(estadoLetra) && elDoctorEstaOcupado(doctor.getId(), fechaSql, horaSql)) {
            JOptionPane.showMessageDialog(this, "El doctor ya tiene cita a esa hora (Conflicto Doctor).");
            return;
        }
        if (!"C".equals(estadoLetra) && elPacienteEstaOcupado(paciente.getId(), fechaSql, horaSql)) {
            JOptionPane.showMessageDialog(this, "El paciente ya tiene cita a esa hora (Conflicto Paciente).");
            return;
        }

        try {
            Connection con = Conexion.getConexion();
            String sql = "UPDATE CITA SET ID_PACIENTE=?, ID_DOCTOR=?, FECHA_CITA=?, HORA_INICIO=?, MOTIVO_CONSULTA=?, OBSERVACIONES=?, ESTADO_CITA=? WHERE ID_CITA=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, paciente.getId());
            pst.setInt(2, doctor.getId());
            pst.setDate(3, fechaSql);
            pst.setTime(4, horaSql);
            pst.setString(5, txtMotivo.getText());
            pst.setString(6, txtObservaciones.getText());
            pst.setString(7, estadoLetra);
            pst.setInt(8, this.idCita);
            
            if (pst.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "¡Cambios guardados exitosamente!");
                dispose();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + e.getMessage());
        }
    }

    private boolean elDoctorEstaOcupado(int id, java.sql.Date f, java.sql.Time h) {
        try { Connection con = Conexion.getConexion();
              PreparedStatement pst = con.prepareStatement("SELECT COUNT(*) FROM CITA WHERE ID_DOCTOR=? AND FECHA_CITA=? AND HORA_INICIO=? AND ESTADO_CITA IN ('P','A') AND ID_CITA != ?");
              pst.setInt(1, id); pst.setDate(2, f); pst.setTime(3, h); pst.setInt(4, this.idCita);
              ResultSet rs = pst.executeQuery(); if(rs.next()) return rs.getInt(1)>0;
        } catch(Exception e){} return false;
    }
    
    private boolean elPacienteEstaOcupado(int id, java.sql.Date f, java.sql.Time h) {
        try { Connection con = Conexion.getConexion();
              PreparedStatement pst = con.prepareStatement("SELECT COUNT(*) FROM CITA WHERE ID_PACIENTE=? AND FECHA_CITA=? AND HORA_INICIO=? AND ESTADO_CITA IN ('P','A') AND ID_CITA != ?");
              pst.setInt(1, id); pst.setDate(2, f); pst.setTime(3, h); pst.setInt(4, this.idCita);
              ResultSet rs = pst.executeQuery(); if(rs.next()) return rs.getInt(1)>0;
        } catch(Exception e){} return false;
    }
}