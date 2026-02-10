package com.crud.view;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import com.crud.controller.CRUDController;
import com.crud.util.MetadatosHelper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class VentanaFormulario extends Stage {
    
    // ========== ENUM PARA MODO ==========
    public enum Modo {
        INSERTAR,
        EDITAR
    }
    
    // ========== ATRIBUTOS - LÓGICA ==========
    private Connection conexion;
    private String nombreTabla;
    private Modo modo;
    private Map<String, Object> datosActuales; // Solo usado en modo EDITAR
    private CRUDController crudController;
    
    // ========== ATRIBUTO - UI ==========
    // Mapa que relaciona nombre de columna -> TextField
    private Map<String, TextField> camposTexto;
    private Button btnGuardar;
    private Button btnCancelar;
    
    // ========== CONSTRUCTOR ==========
    public VentanaFormulario(Connection conexion, String nombreTabla, Modo modo, Map<String, Object> datosActuales) {
        // Guardar parámetros
        this.conexion = conexion;
        this.nombreTabla = nombreTabla;
        this.modo = modo;
        this.datosActuales = datosActuales;
        
        // Crear controller
        this.crudController = new CRUDController(conexion, nombreTabla);
        
        // Inicializar mapa de campos
        this.camposTexto = new HashMap<>();
        
        // Inicializar UI
        inicializarUI();
        
        // Configurar título según modo
        if (modo == Modo.INSERTAR) {
            this.setTitle("Nuevo Registro - " + nombreTabla);
        } else {
            this.setTitle("Editar Registro - " + nombreTabla);
        }
        
        this.setResizable(false);
    }
    
    // ========== MÉTODO: Inicializar UI ==========
    private void inicializarUI() {
        // ===== CREAR GRIDPANE =====
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        
        try {
            // ===== OBTENER METADATOS =====
            List<String> nombresColumnas = MetadatosHelper.obtenerNombresColumnas(conexion, nombreTabla);
            Map<String, String> tiposColumnas = MetadatosHelper.obtenerColumnasConTipos(conexion, nombreTabla);
            String columnaPK = MetadatosHelper.obtenerColumnaPK(conexion, nombreTabla);
            
            // Verificar si la PK es autoincremental
            boolean pkAutoIncremental = false;
            if (columnaPK != null && tiposColumnas.containsKey(columnaPK)) {
                String tipoPK = tiposColumnas.get(columnaPK).toLowerCase();
                pkAutoIncremental = tipoPK.contains("serial");
            }
            
            // ===== GENERAR CAMPOS DINÁMICAMENTE =====
            int fila = 0;
            
            for (String nombreCol : nombresColumnas) {
                // Si estamos en modo INSERTAR y la columna es PK autoincremental, saltarla
                if (modo == Modo.INSERTAR && nombreCol.equals(columnaPK) && pkAutoIncremental) {
                    continue;
                }
                
                // Crear Label
                Label label = new Label(nombreCol + ":");
                
                // Crear TextField
                TextField textField = new TextField();
                textField.setPrefWidth(250);
                
                // Si estamos en modo EDITAR, llenar con datos actuales
                if (modo == Modo.EDITAR && datosActuales != null) {
                    Object valor = datosActuales.get(nombreCol);
                    if (valor != null) {
                        textField.setText(valor.toString());
                    }
                    
                    // Si es la PK, deshabilitar edición
                    if (nombreCol.equals(columnaPK)) {
                        textField.setDisable(true);
                    }
                }
                
                // Guardar referencia al TextField
                camposTexto.put(nombreCol, textField);
                
                // Agregar al grid
                grid.add(label, 0, fila);
                grid.add(textField, 1, fila);
                
                fila++;
            }
            
            // ===== CREAR BOTONES =====
            this.btnGuardar = new Button("Guardar");
            this.btnCancelar = new Button("Cancelar");
            
            btnGuardar.setPrefWidth(120);
            btnCancelar.setPrefWidth(120);
            
            // ===== CONFIGURAR EVENTOS =====
            btnGuardar.setOnAction(evento -> guardarRegistro());
            btnCancelar.setOnAction(evento -> this.close());
            
            // ===== AGREGAR BOTONES AL GRID =====
            grid.add(btnGuardar, 0, fila);
            grid.add(btnCancelar, 1, fila);
            
            // ===== CREAR SCENE =====
            Scene scene = new Scene(grid, 400, Math.min(600, (fila + 1) * 50 + 60));
            this.setScene(scene);
            
        } catch (SQLException e) {
            mostrarError("Error al generar formulario: " + e.getMessage());
            this.close();
        }
    }
    
    // MÉTODO: Guardar Registro 
    
private void guardarRegistro() {
    try {
        
        Map<String, String> tiposColumnas = MetadatosHelper.obtenerColumnasConTipos(conexion, nombreTabla);
        
        
        Map<String, Object> datos = new HashMap<>();
        
        for (Map.Entry<String, TextField> entry : camposTexto.entrySet()) {
            String nombreCol = entry.getKey();
            TextField campo = entry.getValue();
            String valorTexto = campo.getText().trim();
            
            
            if (valorTexto.isEmpty()) {
                datos.put(nombreCol, null);
                continue;
            }
            
            // Obtener tipo de dato
            String tipoDato = tiposColumnas.get(nombreCol);
            if (tipoDato != null) {
                tipoDato = tipoDato.toLowerCase();
            }

            if (tipoDato != null && (tipoDato.contains("numeric") || 
             tipoDato.contains("decimal") || tipoDato.contains("real") || 
             tipoDato.contains("float") || tipoDato.contains("double"))) {

             datos.put(nombreCol, Double.parseDouble(valorTexto));

           } else if (tipoDato != null && (tipoDato.contains("int") || 
             tipoDato.contains("serial"))) {

             datos.put(nombreCol, Integer.parseInt(valorTexto));

           } else if (tipoDato != null && tipoDato.contains("bool")) {

           datos.put(nombreCol, Boolean.parseBoolean(valorTexto));

           }else if (tipoDato != null && tipoDato.contains("date")) {

              LocalDate fecha = LocalDate.parse(valorTexto); 
              datos.put(nombreCol, java.sql.Date.valueOf(fecha));

            } else {
             datos.put(nombreCol, valorTexto);
            } 

            
        }
        
        // EJECUTAR INSERCIÓN O ACTUALIZACIÓN 
        boolean exito = false;
        
        if (modo == Modo.INSERTAR) {
            exito = crudController.insertRegist(datos);
        } else if (modo == Modo.EDITAR) {
            String columnaPK = MetadatosHelper.obtenerColumnaPK(conexion, nombreTabla);
            Object valorPK = datosActuales.get(columnaPK);
            exito = crudController.updateRegist(valorPK, datos);
        }
        
        // MOSTRAR RESULTADO 
        if (exito) {
            mostrarInfo("Registro guardado correctamente");
            this.close();
        } else {
            mostrarError("No se pudo guardar el registro");
        }    
        
     catch (SQLException e) {
        mostrarError("Error al guardar: " + e.getMessage());
    } catch (NumberFormatException e) {
        mostrarError("Error: El precio debe ser un número válido (ej: 45.99)");
    }
}
    
    // Metodo: Convertir Valor según Tipo 
    private Object convertirValor(String valorTexto, String tipoDato) {
        if (valorTexto == null || valorTexto.isEmpty()) {
            return null;
        }
        
        // Tipos numericos enteros
        if (tipoDato.contains("int") || tipoDato.contains("serial")) {
            return Integer.parseInt(valorTexto);
        }
        
        // Tipos numericos decimales
        if (tipoDato.contains("numeric") || tipoDato.contains("decimal") || 
            tipoDato.contains("real") || tipoDato.contains("double") || 
            tipoDato.contains("float")) {
            return Double.parseDouble(valorTexto);
        }
        
        // Tipo booleano
        if (tipoDato.contains("bool")) {
            return Boolean.parseBoolean(valorTexto);
        }
        
        // Tipo fecha
        if (tipoDato.contains("date")) {
            // Aquí podrías agregar conversión de fecha si es necesario
            // Por ahora lo dejamos como String y PostgreSQL lo convierte
            return valorTexto;
        }
        
        // Por defecto, String (varchar, text, char, etc.)
        return valorTexto;
    }
    
    
    // METODO: Mostrar Error 
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    // METODO: Mostrar Info 
    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}       