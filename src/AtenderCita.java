import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class AtenderCita extends JDialog {
    private int idCita;
    private JTextArea txtObservaciones;
    private JTextArea txtTratamiento;
    private JSpinner spinDiasTratamiento;

    // Componentes de la Receta
    private JComboBox<ItemMedicamento> cmbMedicinas;
    private JTextField txtDosis;
    private JTextField txtFrecuencia;
    private JSpinner spinDiasMedicina;
    private JTable tablaReceta;
    private DefaultTableModel modeloReceta;

    private ArrayList<Object[]> listaMedicinasGuardar = new ArrayList<>();

    public AtenderCita(int idCita) {
        this.idCita = idCita;

        setTitle("Atención Médica - Cita #" + idCita);
        setModal(true);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel lblObs = new JLabel("1. Diagnóstico / Observaciones de la Cita:");
        lblObs.setFont(new Font("Arial", Font.BOLD, 14));
        lblObs.setBounds(30, 20, 350, 20);
        add(lblObs);

        txtObservaciones = new JTextArea();
        txtObservaciones.setLineWrap(true);
        JScrollPane scrollObs = new JScrollPane(txtObservaciones);
        scrollObs.setBounds(30, 45, 400, 80);
        add(scrollObs);

        JLabel lblTrat = new JLabel("2. Descripción del Tratamiento (Indicaciones):");
        lblTrat.setFont(new Font("Arial", Font.BOLD, 14));
        lblTrat.setBounds(30, 140, 350, 20);
        add(lblTrat);

        txtTratamiento = new JTextArea();
        txtTratamiento.setLineWrap(true);
        JScrollPane scrollTrat = new JScrollPane(txtTratamiento);
        scrollTrat.setBounds(30, 165, 400, 80);
        add(scrollTrat);

        JLabel lblDiasT = new JLabel("Duración (Días):");
        lblDiasT.setBounds(30, 255, 120, 20);
        add(lblDiasT);
        spinDiasTratamiento = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
        spinDiasTratamiento.setBounds(130, 255, 60, 25);
        add(spinDiasTratamiento);

        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setBounds(450, 20, 10, 440);
        add(sep);

        JLabel lblReceta = new JLabel("3. Receta / Medicamentos:");
        lblReceta.setFont(new Font("Arial", Font.BOLD, 14));
        lblReceta.setBounds(470, 20, 300, 20);
        add(lblReceta);

        cmbMedicinas = new JComboBox<>();
        cmbMedicinas.setBounds(470, 50, 380, 30);
        add(cmbMedicinas);
        cargarMedicamentos();

        agregarCampo(470, 90, "Dosis:", txtDosis = new JTextField());
        agregarCampo(660, 90, "Frecuencia:", txtFrecuencia = new JTextField());

        JLabel lblDurMed = new JLabel("Por cuántos días:");
        lblDurMed.setBounds(470, 130, 120, 25);
        add(lblDurMed);
        spinDiasMedicina = new JSpinner(new SpinnerNumberModel(1, 1, 90, 1));
        spinDiasMedicina.setBounds(580, 130, 60, 25);
        add(spinDiasMedicina);

        JButton btnAgregarMed = new JButton("Agregar a Receta ⬇");
        btnAgregarMed.setBounds(680, 130, 170, 25);
        add(btnAgregarMed);
        btnAgregarMed.addActionListener(e -> agregarMedicinaALaTabla());

        String[] cols = {"Medicamento", "Dosis", "Frecuencia", "Días"};
        
        modeloReceta = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaReceta = new JTable(modeloReceta);
        JScrollPane scrollTabla = new JScrollPane(tablaReceta);
        scrollTabla.setBounds(470, 170, 380, 250);
        add(scrollTabla);

        JButton btnFinalizar = new JButton("FINALIZAR CONSULTA");
        btnFinalizar.setBounds(300, 480, 300, 50);
        btnFinalizar.setBackground(new Color(0, 100, 0));
        btnFinalizar.setForeground(Color.WHITE);
        btnFinalizar.setFont(new Font("Arial", Font.BOLD, 16));
        
        btnFinalizar.setFocusPainted(false);
        btnFinalizar.setBorderPainted(false);
        btnFinalizar.setOpaque(true);
        add(btnFinalizar);

        btnFinalizar.addActionListener(e -> guardarTodo());
    }

    private void agregarCampo(int x, int y, String titulo, Component comp) {
        JLabel lbl = new JLabel(titulo);
        lbl.setBounds(x, y, 80, 25);
        add(lbl);
        comp.setBounds(x + 70, y, 110, 25);
        add(comp);
    }

    private void cargarMedicamentos() {
        try {
            Connection con = Conexion.getConexion();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM MEDICAMENTO ORDER BY NOMBRE_MEDICAMENTO");
            while (rs.next()) {
                cmbMedicinas.addItem(new ItemMedicamento(
                        rs.getInt("ID_MEDICAMENTO"),
                        rs.getString("NOMBRE_MEDICAMENTO"),
                        rs.getString("PRESENTACION")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void agregarMedicinaALaTabla() {
        ItemMedicamento med = (ItemMedicamento) cmbMedicinas.getSelectedItem();
        String dosis = txtDosis.getText();
        String frec = txtFrecuencia.getText();
        int dias = (int) spinDiasMedicina.getValue();

        if (dosis.isEmpty() || frec.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribe dosis y frecuencia.");
            return;
        }

        modeloReceta.addRow(new Object[]{med.toString(), dosis, frec, dias});

        listaMedicinasGuardar.add(new Object[]{med.getId(), dosis, frec, dias});

        txtDosis.setText("");
        txtFrecuencia.setText("");
    }

    private void guardarTodo() {
        if (txtObservaciones.getText().isEmpty() || txtTratamiento.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debes llenar el diagnóstico y el tratamiento.");
            return;
        }

        try {
            Connection con = Conexion.getConexion();
            con.setAutoCommit(false);

            try {
                String sqlCita = "UPDATE CITA SET ESTADO_CITA = 'A', OBSERVACIONES = ? WHERE ID_CITA = ?";
                PreparedStatement pstCita = con.prepareStatement(sqlCita);
                pstCita.setString(1, txtObservaciones.getText());
                pstCita.setInt(2, idCita);
                pstCita.executeUpdate();

                String sqlTrat = "INSERT INTO TRATAMIENTO (ID_CITA, DESCRIPCION, FECHA_INICIO_TRATAMIENTO, DURACION_DIAS) VALUES (?, ?, CURDATE(), ?)";
                PreparedStatement pstTrat = con.prepareStatement(sqlTrat, Statement.RETURN_GENERATED_KEYS);
                pstTrat.setInt(1, idCita);
                pstTrat.setString(2, txtTratamiento.getText());
                pstTrat.setInt(3, (int) spinDiasTratamiento.getValue());
                pstTrat.executeUpdate();

                ResultSet rsKeys = pstTrat.getGeneratedKeys();
                int idTratamiento = 0;
                if (rsKeys.next()) {
                    idTratamiento = rsKeys.getInt(1);
                }

                String sqlMed = "INSERT INTO TRATAMIENTO_MEDICAMENTO (ID_TRATAMIENTO, ID_MEDICAMENTO, DOSIS, FRECUENCIA, DURACION) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstMed = con.prepareStatement(sqlMed);

                for (Object[] fila : listaMedicinasGuardar) {
                    pstMed.setInt(1, idTratamiento);
                    pstMed.setInt(2, (int) fila[0]);
                    pstMed.setString(3, (String) fila[1]);
                    pstMed.setString(4, (String) fila[2]);
                    pstMed.setInt(5, (int) fila[3]);
                    pstMed.executeUpdate();
                }

                con.commit();
                JOptionPane.showMessageDialog(this, "¡Consulta finalizada exitosamente!");
                dispose();

            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error grave: " + e.getMessage());
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}