import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MenuOperador extends JFrame {
    private JTextField txtCedula;
    private JLabel lblNombrePaciente;
    private JTable tablaCitas;
    private DefaultTableModel modelo;
    private JButton btnEditarPac, btnEliminarPac; // Botones de gestión

    // Variables para controlar qué tenemos seleccionado
    private int idPacienteActual = -1;
    
    // --- NUEVAS VARIABLES ---
    private int idDoctorActual = -1; // Para guardar el ID si encontramos un médico
    private JRadioButton radioPaciente, radioDoctor; // Los selectores
    private ButtonGroup grupoTipo; // Para agrupar los radios
    private boolean mostrandoUsuarios = false; // Bandera para saber qué hay en la tabla

    public MenuOperador() {
        setTitle("Panel de Operador de Admisión - IESS");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // --- ZONA DE BÚSQUEDA ---
        JLabel lblTitulo = new JLabel("Gestión de Usuarios y Citas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setBounds(30, 20, 350, 30);
        add(lblTitulo);

        // --- BOTÓN CERRAR SESIÓN ---
        JButton btnLogout = new JButton("Cerrar Sesión 🚪");
        btnLogout.setBounds(700, 20, 150, 30);
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

        // SELECTORES DE TIPO (NUEVO)
        radioPaciente = new JRadioButton("Paciente", true); // Marcado por defecto
        radioPaciente.setBounds(160, 60, 80, 25);
        radioDoctor = new JRadioButton("Médico");
        radioDoctor.setBounds(250, 60, 80, 25);
        
        // Agrupar para que solo se pueda elegir uno
        grupoTipo = new ButtonGroup();
        grupoTipo.add(radioPaciente);
        grupoTipo.add(radioDoctor);
        
        add(radioPaciente);
        add(radioDoctor);

        // BUSCADOR (Bajamos un poco las coordenadas Y a 90)
        JLabel lblBuscar = new JLabel("Cédula:");
        lblBuscar.setBounds(30, 90, 100, 20);
        add(lblBuscar);

        txtCedula = new JTextField();
        txtCedula.setBounds(160, 90, 150, 30);
        add(txtCedula);

        JButton btnBuscar = new JButton("Buscar 🔍");
        btnBuscar.setBounds(320, 90, 100, 30);
        add(btnBuscar);
        btnBuscar.addActionListener(e -> buscarUsuario());

        // BOTÓN VER TODOS (NUEVO)
        JButton btnVerTodos = new JButton("Ver Todos 📋");
        btnVerTodos.setBounds(430, 90, 150, 30);
        btnVerTodos.setBackground(new Color(0, 100, 150)); // Azul cerceta
        btnVerTodos.setForeground(Color.WHITE);
        btnVerTodos.setFocusPainted(false);
        btnVerTodos.setBorderPainted(false);
        btnVerTodos.setOpaque(true);
        add(btnVerTodos);

        btnVerTodos.addActionListener(e -> cargarListaUsuarios());

        // BOTÓN REGISTRAR (Ahora es inteligente)
        JButton btnRegistrar = new JButton("Registrar Nuevo 👤+");
        btnRegistrar.setBackground(new Color(50, 100, 200));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.setBorderPainted(false);
        btnRegistrar.setOpaque(true);
        btnRegistrar.setBounds(600, 90, 200, 30);
        add(btnRegistrar);
        
        // Lógica inteligente para registrar
        btnRegistrar.addActionListener(e -> {
            if (radioPaciente.isSelected()) {
                new RegistrarPaciente().setVisible(true);
            } else {
                new RegistrarDoctor().setVisible(true);
            }
        });

        // --- SEPARADOR ---
        JSeparator sep = new JSeparator();
        sep.setBounds(30, 120, 820, 10);
        add(sep);

        // --- INFO DEL PACIENTE ENCONTRADO ---
        lblNombrePaciente = new JLabel("Paciente: (Ninguno seleccionado)");
        lblNombrePaciente.setFont(new Font("Arial", Font.BOLD, 18));
        lblNombrePaciente.setForeground(new Color(100, 100, 100)); // Gris al inicio
        lblNombrePaciente.setBounds(30, 140, 500, 25);
        add(lblNombrePaciente);

        // --- BOTONES DE GESTIÓN DE PACIENTE (Editar / Eliminar) ---
        btnEditarPac = new JButton("Editar Datos 📝");
        btnEditarPac.setBounds(550, 140, 140, 30);
        btnEditarPac.setBackground(new Color(255, 140, 0)); // Naranja
        btnEditarPac.setForeground(Color.WHITE);
        // Estilo plano
        btnEditarPac.setFocusPainted(false);
        btnEditarPac.setBorderPainted(false);
        btnEditarPac.setOpaque(true);
        btnEditarPac.setVisible(false); // Oculto al inicio
        add(btnEditarPac);

        btnEliminarPac = new JButton("Eliminar ❌");
        btnEliminarPac.setBounds(700, 140, 120, 30);
        btnEliminarPac.setBackground(Color.RED);
        btnEliminarPac.setForeground(Color.WHITE);
        // Estilo plano
        btnEliminarPac.setFocusPainted(false);
        btnEliminarPac.setBorderPainted(false);
        btnEliminarPac.setOpaque(true);
        btnEliminarPac.setVisible(false); // Oculto al inicio
        add(btnEliminarPac);

        // LÓGICA EDITAR (Inteligente)
        btnEditarPac.addActionListener(e -> {
            if (idPacienteActual != -1) {
                new EditarPaciente(this.idPacienteActual).setVisible(true);
            } else if (idDoctorActual != -1) {
                new EditarDoctor(this.idDoctorActual).setVisible(true);
            }
            buscarUsuario(); // Refrescar al volver
        });

        // LÓGICA ELIMINAR (Inteligente)
        btnEliminarPac.addActionListener(e -> eliminarUsuario());

        // --- TABLA DE CITAS ---
        String[] columnas = {"ID", "Fecha", "Hora", "Doctor", "Motivo", "Estado"};
        
        // Creamos el modelo pero bloqueando la edición
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // ¡Aquí está la magia! Retorna siempre falso.
            }
        };
        tablaCitas = new JTable(modelo);

        // --- MEJORAS VISUALES DE LA TABLA ---
        tablaCitas.setRowHeight(30); // Más altura a las filas (¡Se verá mucho mejor!)
        tablaCitas.setFont(new Font("Arial", Font.PLAIN, 14)); // Letra un poco más grande
        tablaCitas.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14)); // Encabezados en negrita
        tablaCitas.getTableHeader().setBackground(new Color(230, 230, 230)); // Fondo gris suave para encabezados
        // -----------------------------------

        JScrollPane scroll = new JScrollPane(tablaCitas);
        scroll.setBounds(30, 180, 820, 250);
        add(scroll);

        // LÓGICA DE SELECCIÓN EN LA TABLA
        tablaCitas.getSelectionModel().addListSelectionListener(e -> {
            // Evitar eventos duplicados
            if (!e.getValueIsAdjusting() && tablaCitas.getSelectedRow() != -1) {
                if (mostrandoUsuarios) {
                    // Si estamos viendo la lista de usuarios, al hacer clic seleccionamos al usuario
                    int fila = tablaCitas.getSelectedRow();
                    int id = (int) modelo.getValueAt(fila, 0); // Asumimos que ID está en columna 0
                    String nombre = (String) modelo.getValueAt(fila, 2) + " " + modelo.getValueAt(fila, 3); // Nombre + Apellido

                    if (radioPaciente.isSelected()) {
                        idPacienteActual = id;
                        idDoctorActual = -1;
                        lblNombrePaciente.setText("Paciente Seleccionado: " + nombre);
                    } else {
                        idDoctorActual = id;
                        idPacienteActual = -1;
                        lblNombrePaciente.setText("Médico Seleccionado: " + nombre);
                    }
                    
                    // Habilitar botones de gestión
                    btnEditarPac.setVisible(true);
                    btnEliminarPac.setVisible(true);
                }
            }
        });

        // --- BOTONES DE ACCIÓN (Agendar / Editar / Cancelar) ---
        JButton btnAgendar = new JButton("Agendar Cita para este Paciente");
        btnAgendar.setBounds(30, 450, 250, 40);
        btnAgendar.setBackground(new Color(0, 100, 0)); // Verde
        btnAgendar.setForeground(Color.WHITE);
        btnAgendar.setFocusPainted(false);
        btnAgendar.setBorderPainted(false); // Quita el borde 3D de Windows
        btnAgendar.setOpaque(true); // Obliga a pintar el fondo
        add(btnAgendar);

        // --- BOTÓN EDITAR ---
        JButton btnEditar = new JButton("Editar Cita ✏️");
        btnEditar.setBounds(300, 450, 200, 40);
        btnEditar.setBackground(new Color(255, 140, 0)); // Naranja
        btnEditar.setForeground(Color.WHITE);
        // Estilo plano
        btnEditar.setFocusPainted(false);
        btnEditar.setBorderPainted(false);
        btnEditar.setOpaque(true);
        add(btnEditar);

        JButton btnCancelar = new JButton("Cancelar Cita Seleccionada");
        btnCancelar.setBounds(520, 450, 250, 40); // Movido más a la derecha
        btnCancelar.setBackground(new Color(200, 50, 50)); // Rojo
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorderPainted(false); // Quita el borde 3D de Windows
        btnCancelar.setOpaque(true); // Obliga a pintar el fondo
        add(btnCancelar);

        // Lógica del Botón AGENDAR
        btnAgendar.addActionListener(e -> {
            if (idPacienteActual != -1) {
                // ¡REUTILIZAMOS TU VENTANA DE AGENDA! ♻️
                // Como AgendarCita es un JDialog modal, el código se pausa aquí
                new AgendarCita(idPacienteActual).setVisible(true);

                // Al cerrar la ventana de agendar, refrescamos la tabla automáticamente
                cargarCitas(true);
            } else {
                JOptionPane.showMessageDialog(this, "Primero busca y selecciona un paciente.");
            }
        });

        // Lógica del Botón EDITAR
        btnEditar.addActionListener(e -> {
            int fila = tablaCitas.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona una cita para editar.");
                return;
            }
            
            int idCita = (int) modelo.getValueAt(fila, 0);
            String estado = (String) modelo.getValueAt(fila, 5);
            
            // Validación Nueva
            if ("Atendida".equals(estado) || "Cancelada".equals(estado)) {
                JOptionPane.showMessageDialog(this, "No se pueden editar citas pasadas o canceladas.");
                return;
            }

            // Abrir ventana modal
            new EditarCita(idCita).setVisible(true);
            
            // Recargar tabla al volver
            if (idPacienteActual != -1) cargarCitas(true);
            if (idDoctorActual != -1) cargarCitas(false);
        });

        // Lógica del Botón CANCELAR (Reutilizada)
        btnCancelar.addActionListener(e -> cancelarCita());
    }

    private void buscarUsuario() {
        String cedula = txtCedula.getText().trim();
        if (cedula.isEmpty()) return;

        // Limpiamos estados anteriores
        idPacienteActual = -1;
        idDoctorActual = -1;
        modelo.setRowCount(0); // Limpiar tabla
        lblNombrePaciente.setForeground(Color.BLACK);

        try {
            Connection con = Conexion.getConexion();
            String sql = "";
            
            // DECISIÓN: ¿Buscamos Paciente o Doctor?
            if (radioPaciente.isSelected()) {
                sql = "SELECT U.NOMBRE, U.APELLIDO, P.ID_PACIENTE " +
                      "FROM USUARIO U JOIN PACIENTE P ON U.ID_USUARIO = P.ID_USUARIO " +
                      "WHERE U.CEDULA = ? AND U.ESTADO_USUARIO = 1";
            } else {
                sql = "SELECT U.NOMBRE, U.APELLIDO, D.ID_DOCTOR " +
                      "FROM USUARIO U JOIN DOCTOR D ON U.ID_USUARIO = D.ID_USUARIO " +
                      "WHERE U.CEDULA = ? AND U.ESTADO_USUARIO = 1";
            }

            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, cedula);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String nombre = rs.getString("NOMBRE") + " " + rs.getString("APELLIDO");
                
                if (radioPaciente.isSelected()) {
                    this.idPacienteActual = rs.getInt("ID_PACIENTE");
                    lblNombrePaciente.setText("Paciente: " + nombre);
                    cargarCitas(true); // true = es paciente
                } else {
                    this.idDoctorActual = rs.getInt("ID_DOCTOR");
                    lblNombrePaciente.setText("Médico: " + nombre);
                    cargarCitas(false); // false = es médico
                }
                
                // Mostrar botones de gestión
                btnEditarPac.setVisible(true);
                btnEliminarPac.setVisible(true);

            } else {
                lblNombrePaciente.setText("Usuario NO ENCONTRADO");
                lblNombrePaciente.setForeground(Color.RED);
                btnEditarPac.setVisible(false);
                btnEliminarPac.setVisible(false);
                
                int r = JOptionPane.showConfirmDialog(this,
                    "No existe. ¿Deseas registrarlo?", "No encontrado", JOptionPane.YES_NO_OPTION);
                if (r == JOptionPane.YES_OPTION) {
                     if (radioPaciente.isSelected()) new RegistrarPaciente().setVisible(true);
                     else new RegistrarDoctor().setVisible(true);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void cargarCitas(boolean esPaciente) {
        mostrandoUsuarios = false; // <--- IMPORTANTE: Apagamos la bandera de lista
        modelo.setRowCount(0);
        try {
            Connection con = Conexion.getConexion();
            String sql = "";

            if (esPaciente) {
                // Si es paciente, mostramos el DOCTOR
                modelo.setColumnIdentifiers(new String[]{"ID", "Fecha", "Hora", "Doctor", "Motivo", "Estado"});
                sql = "SELECT C.ID_CITA, C.FECHA_CITA, C.HORA_INICIO, " +
                      "CONCAT(U.NOMBRE, ' ', U.APELLIDO) AS OTRO, " +
                      "C.MOTIVO_CONSULTA, C.ESTADO_CITA " +
                      "FROM CITA C " +
                      "JOIN DOCTOR D ON C.ID_DOCTOR = D.ID_DOCTOR " +
                      "JOIN USUARIO U ON D.ID_USUARIO = U.ID_USUARIO " +
                      "WHERE C.ID_PACIENTE = ?";
            } else {
                // Si es médico, mostramos el PACIENTE
                modelo.setColumnIdentifiers(new String[]{"ID", "Fecha", "Hora", "Paciente", "Motivo", "Estado"});
                sql = "SELECT C.ID_CITA, C.FECHA_CITA, C.HORA_INICIO, " +
                      "CONCAT(U.NOMBRE, ' ', U.APELLIDO) AS OTRO, " +
                      "C.MOTIVO_CONSULTA, C.ESTADO_CITA " +
                      "FROM CITA C " +
                      "JOIN PACIENTE P ON C.ID_PACIENTE = P.ID_PACIENTE " +
                      "JOIN USUARIO U ON P.ID_USUARIO = U.ID_USUARIO " +
                      "WHERE C.ID_DOCTOR = ?";
            }
            
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, esPaciente ? idPacienteActual : idDoctorActual);
            
            ResultSet rs = pst.executeQuery();
            while(rs.next()) {
                // 1. TRADUCCIÓN
                String estadoLetra = rs.getString("ESTADO_CITA");
                String estadoTexto = estadoLetra;
                
                switch (estadoLetra) {
                    case "P": estadoTexto = "Programada"; break;
                    case "A": estadoTexto = "Atendida"; break;
                    case "C": estadoTexto = "Cancelada"; break;
                }

                modelo.addRow(new Object[]{
                    rs.getInt("ID_CITA"), rs.getDate("FECHA_CITA"), rs.getTime("HORA_INICIO"),
                    rs.getString("OTRO"), rs.getString("MOTIVO_CONSULTA"),
                    estadoTexto // <--- Palabra completa
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void cancelarCita() {
        int fila = tablaCitas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una cita para cancelar.");
            return;
        }

        int idCita = (int) modelo.getValueAt(fila, 0);
        String estado = (String) modelo.getValueAt(fila, 5);

        // Validación Nueva
        if ("Atendida".equals(estado) || "Cancelada".equals(estado)) {
            JOptionPane.showMessageDialog(this, "No se puede cancelar esta cita.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Cancelar la cita #" + idCita + "?");
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection con = Conexion.getConexion();
                PreparedStatement pst = con.prepareStatement("UPDATE CITA SET ESTADO_CITA='C' WHERE ID_CITA=?");
                pst.setInt(1, idCita);
                pst.executeUpdate();
                if (idPacienteActual != -1) cargarCitas(true);
                if (idDoctorActual != -1) cargarCitas(false);
                JOptionPane.showMessageDialog(this, "Cita cancelada.");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void eliminarUsuario() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Estás seguro de ELIMINAR a este usuario?", "Confirmar", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection con = Conexion.getConexion();
                String sqlGetId = "";
                int idBusqueda = 0;

                if (radioPaciente.isSelected()) {
                    sqlGetId = "SELECT ID_USUARIO FROM PACIENTE WHERE ID_PACIENTE = ?";
                    idBusqueda = idPacienteActual;
                } else {
                    sqlGetId = "SELECT ID_USUARIO FROM DOCTOR WHERE ID_DOCTOR = ?";
                    idBusqueda = idDoctorActual;
                }

                PreparedStatement pstGet = con.prepareStatement(sqlGetId);
                pstGet.setInt(1, idBusqueda);
                ResultSet rs = pstGet.executeQuery();
                
                if (rs.next()) {
                    int idUsuario = rs.getInt("ID_USUARIO");
                    // Borrado lógico
                    PreparedStatement pstDel = con.prepareStatement("UPDATE USUARIO SET ESTADO_USUARIO=0 WHERE ID_USUARIO=?");
                    pstDel.setInt(1, idUsuario);
                    pstDel.executeUpdate();
                    
                    JOptionPane.showMessageDialog(this, "Usuario eliminado.");
                    // Limpiar todo
                    txtCedula.setText("");
                    lblNombrePaciente.setText("Usuario: (Ninguno seleccionado)");
                    lblNombrePaciente.setForeground(new Color(100, 100, 100));
                    modelo.setRowCount(0);
                    idPacienteActual = -1;
                    idDoctorActual = -1;
                    btnEditarPac.setVisible(false);
                    btnEliminarPac.setVisible(false);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
            }
        }
    }

    private void cargarListaUsuarios() {
        mostrandoUsuarios = true; // Activamos la bandera
        modelo.setRowCount(0); // Limpiar tabla
        
        // Ocultamos botones de citas porque en esta vista no tienen sentido
        // (O podrías dejarlos si quisieras implementar "Ver citas de este usuario seleccionado")
        lblNombrePaciente.setText("Viendo lista completa...");
        lblNombrePaciente.setForeground(Color.BLACK);
        btnEditarPac.setVisible(false);
        btnEliminarPac.setVisible(false);
        
        try {
            Connection con = Conexion.getConexion();
            String sql = "";
            
            if (radioPaciente.isSelected()) {
                // CAMBIAMOS LAS COLUMNAS PARA PACIENTE
                modelo.setColumnIdentifiers(new String[]{"ID", "Cédula", "Nombre", "Apellido", "Email", "Teléfono"});
                
                sql = "SELECT P.ID_PACIENTE, U.CEDULA, U.NOMBRE, U.APELLIDO, U.EMAIL, U.TELEFONO " +
                      "FROM PACIENTE P JOIN USUARIO U ON P.ID_USUARIO = U.ID_USUARIO " +
                      "WHERE U.ESTADO_USUARIO = 1";
                      
            } else {
                // CAMBIAMOS LAS COLUMNAS PARA MÉDICO
                modelo.setColumnIdentifiers(new String[]{"ID", "Cédula", "Nombre", "Apellido", "Especialidad", "Teléfono"});
                
                sql = "SELECT D.ID_DOCTOR, U.CEDULA, U.NOMBRE, U.APELLIDO, D.ESPECIALIDAD, U.TELEFONO " +
                      "FROM DOCTOR D JOIN USUARIO U ON D.ID_USUARIO = U.ID_USUARIO " +
                      "WHERE U.ESTADO_USUARIO = 1";
            }
            
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            
            while(rs.next()) {
                Object[] fila = new Object[6];
                fila[0] = rs.getInt(1); // ID (Paciente o Doctor)
                fila[1] = rs.getString("CEDULA");
                fila[2] = rs.getString("NOMBRE");
                fila[3] = rs.getString("APELLIDO");
                // El campo 5 varía (Email o Especialidad)
                fila[4] = radioPaciente.isSelected() ? rs.getString("EMAIL") : rs.getString("ESPECIALIDAD");
                fila[5] = rs.getString("TELEFONO");
                
                modelo.addRow(fila);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar lista: " + e.getMessage());
        }
    }
}