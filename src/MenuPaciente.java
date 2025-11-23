import javax.swing.*;
import javax.swing.table.DefaultTableModel; // Importante para el modelo de la tabla
import java.awt.*;
import java.sql.*;

public class MenuPaciente extends JFrame {
    private int idUsuario;
    private int idPaciente;
    private JTable tablaCitas; // La tabla visual
    private DefaultTableModel modelo; // El modelo de datos

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
        btnLogout.setBounds(600, 20, 150, 30); // Posición superior derecha
        btnLogout.setBackground(new Color(255, 80, 80)); // Rojo suave
        btnLogout.setForeground(Color.WHITE);

        // Estilo Moderno
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setOpaque(true);
        add(btnLogout);

        btnLogout.addActionListener(e -> {
            this.dispose(); // Cierra esta ventana
            new Login().setVisible(true); // Vuelve al Login
        });

        // --- CONFIGURACIÓN DE LA TABLA ---
        // 1. Definimos las columnas
        String[] columnas = {"ID", "Fecha", "Hora", "Doctor", "Motivo", "Estado"};
        
        // Creamos el modelo pero bloqueando la edición
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // ¡Aquí está la magia! Retorna siempre falso.
            }
        };

        // 2. Creamos la tabla visual con ese modelo
        tablaCitas = new JTable(modelo);

        // --- MEJORAS VISUALES DE LA TABLA ---
        tablaCitas.setRowHeight(30); // Más altura a las filas (¡Se verá mucho mejor!)
        tablaCitas.setFont(new Font("Arial", Font.PLAIN, 14)); // Letra un poco más grande
        tablaCitas.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14)); // Encabezados en negrita
        tablaCitas.getTableHeader().setBackground(new Color(230, 230, 230)); // Fondo gris suave para encabezados
        // -----------------------------------

        // 3. Ponemos la tabla dentro de un panel con scroll (por si hay muchas citas)
        JScrollPane scrollPane = new JScrollPane(tablaCitas);
        scrollPane.setBounds(30, 70, 720, 300);
        add(scrollPane);

        // 4. Botón para recargar (útil para probar)
        JButton btnRecargar = new JButton("Recargar Citas");
        btnRecargar.setBounds(30, 400, 150, 30);
        add(btnRecargar);

        btnRecargar.addActionListener(e -> cargarCitas());

        // 5. Botón para ver detalles de la cita seleccionada
        JButton btnVerDetalles = new JButton("Ver Detalles / Historia");
        btnVerDetalles.setBounds(200, 400, 200, 30); // Lo ponemos al lado del otro botón
        add(btnVerDetalles);

        btnVerDetalles.addActionListener(e -> {
            // 1. Preguntamos a la tabla: "¿Qué fila seleccionó el usuario?"
            int filaSeleccionada = tablaCitas.getSelectedRow();

            // 2. Si filaSeleccionada es -1, significa que no seleccionó nada
            if (filaSeleccionada != -1) {
                // 3. Obtenemos el valor de la columna 0 (que es el ID_CITA) de esa fila
                // Ojo: getValueAt devuelve un Object, así que hay que convertirlo a (int)
                int idCita = (int) modelo.getValueAt(filaSeleccionada, 0);

                // Por ahora, solo imprimimos para probar
                System.out.println("🔎 El usuario quiere ver la cita ID: " + idCita);

                // AQUÍ llamaremos a la nueva ventana de detalles más adelante
                // mostrarDetalleCita(idCita);
                // Esto abre la ventana nueva pasándole el ID que obtuvimos de la fila seleccionada
                new DetalleCita(idCita).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, selecciona una cita de la tabla primero.");
            }
        });

        // Botón NUEVA CITA (Azul sólido)
        JButton btnNuevaCita = new JButton("Agendar Nueva Cita");
        btnNuevaCita.setBounds(420, 400, 200, 30);
        btnNuevaCita.setBackground(new Color(50, 100, 200)); // Azul
        btnNuevaCita.setForeground(Color.WHITE); // Letra blanca
        btnNuevaCita.setFocusPainted(false); // Quita el recuadro punteado feo al hacer clic
        btnNuevaCita.setBorderPainted(false); // Quita el borde 3D de Windows
        btnNuevaCita.setOpaque(true); // Obliga a pintar el fondo
        add(btnNuevaCita);

        btnNuevaCita.addActionListener(e -> {
            // Abrimos la ventana de agendar pasándole el carnet del paciente
            new AgendarCita(this.idPaciente).setVisible(true);
            cargarCitas();
        });

        // Botón CANCELAR CITA (Rojo sólido)
        JButton btnCancelar = new JButton("Cancelar Cita");
        btnCancelar.setBounds(630, 400, 150, 30);
        btnCancelar.setBackground(new Color(200, 50, 50)); // Rojo
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorderPainted(false); // Quita el borde 3D de Windows
        btnCancelar.setOpaque(true); // Obliga a pintar el fondo
        add(btnCancelar);

        btnCancelar.addActionListener(e -> cancelarCitaSeleccionada());

        // Cargar las citas al iniciar
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
        // Limpiamos la tabla antes de recargar para no duplicar filas
        modelo.setRowCount(0);

        try {
            Connection conexion = Conexion.getConexion();

            // 1. FILTRO SQL: Agregamos "AND C.ESTADO_CITA != 'C'" para ocultar las canceladas
            String sql = "SELECT C.ID_CITA, C.FECHA_CITA, C.HORA_INICIO, " +
                    "CONCAT(U.NOMBRE, ' ', U.APELLIDO) AS NOMBRE_DOCTOR, " +
                    "C.MOTIVO_CONSULTA, C.ESTADO_CITA " +
                    "FROM CITA C " +
                    "JOIN DOCTOR D ON C.ID_DOCTOR = D.ID_DOCTOR " +
                    "JOIN USUARIO U ON D.ID_USUARIO = U.ID_USUARIO " +
                    "WHERE C.ID_PACIENTE = ? AND C.ESTADO_CITA != 'C'"; // <--- OJO AQUÍ

            PreparedStatement pst = conexion.prepareStatement(sql);
            pst.setInt(1, this.idPaciente);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                // 2. TRADUCCIÓN VISUAL
                String estadoLetra = rs.getString("ESTADO_CITA");
                String estadoTexto = estadoLetra;
                
                // Solo traducimos P y A (C no llega por el filtro SQL)
                if ("P".equals(estadoLetra)) estadoTexto = "Programada";
                else if ("A".equals(estadoLetra)) estadoTexto = "Atendida";

                Object[] fila = {
                        rs.getInt("ID_CITA"),
                        rs.getDate("FECHA_CITA"),
                        rs.getTime("HORA_INICIO"),
                        rs.getString("NOMBRE_DOCTOR"),
                        rs.getString("MOTIVO_CONSULTA"),
                        estadoTexto // <--- Ponemos la palabra bonita
                };
                modelo.addRow(fila);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar citas: " + e.getMessage());
        }
    }

    private void cancelarCitaSeleccionada() {
        // 1. Verificamos si seleccionó algo
        int fila = tablaCitas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona la cita que deseas cancelar.");
            return;
        }

        // 2. Obtenemos datos clave de la fila (ID y Estado)
        int idCita = (int) modelo.getValueAt(fila, 0);
        String estadoActual = (String) modelo.getValueAt(fila, 5); // Leemos "Atendida" o "Programada"

        // 3. VALIDACIÓN CORREGIDA: Comparamos con la palabra completa
        if ("Atendida".equals(estadoActual) || "Cancelada".equals(estadoActual)) {
            JOptionPane.showMessageDialog(this, "No puedes cancelar una cita que ya fue atendida o cancelada.");
            return;
        }

        // 3. Confirmación de seguridad
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de que deseas cancelar la cita #" + idCita + "?",
                "Confirmar Cancelación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                Connection con = Conexion.getConexion();
                // ACTUALIZAMOS el estado a 'C' (Cancelada)
                String sql = "UPDATE CITA SET ESTADO_CITA = 'C' WHERE ID_CITA = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setInt(1, idCita);

                int filasAfectadas = pst.executeUpdate();
                if (filasAfectadas > 0) {
                    JOptionPane.showMessageDialog(this, "Cita cancelada correctamente.");
                    cargarCitas(); // Refrescamos la tabla para ver el cambio
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al cancelar: " + e.getMessage());
            }
        }
    }
}