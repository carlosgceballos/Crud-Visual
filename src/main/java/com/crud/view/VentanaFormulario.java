package com.crud.view;

import java.sql.*;
import java.util.*;
import com.crud.controller.CRUDController;
import com.crud.util.MetadatosHelper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class VentanaFormulario extends Stage {
    
    
    public enum Modo {
        INSERTAR,
        EDITAR
    }
    
   
    private Connection conexion;
    private String nombreTabla;
    private Modo modo;
    private Map<String, Object> datosActuales; 
    private CRUDController crudController;
    
   
    private Map<String, TextField> camposTexto;
    private Button btnGuardar;
    private Button btnCancelar;
    
    
    public VentanaFormulario(Connection conexion, String nombreTabla, Modo modo, Map<String, Object> datosActuales) {
        // Guardar parámetros
        this.conexion = conexion;
        this.nombreTabla = nombreTabla;
        this.modo = modo;
        this.datosActuales = datosActuales;
        
        
        this.crudController = new CRUDController(conexion, nombreTabla);
        
        
        this.camposTexto = new HashMap<>();
        
       
        inicializarUI();
        
        
        if (modo == Modo.INSERTAR) {
            this.setTitle("Nuevo Registro - " + nombreTabla);
        } else {
            this.setTitle("Editar Registro - " + nombreTabla);
        }
        
        this.setResizable(false);
    }
    
   
    private void inicializarUI() {
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        
        try {
           
            List<String> nombresColumnas = MetadatosHelper.obtenerNombresColumnas(conexion, nombreTabla);
            Map<String, String> tiposColumnas = MetadatosHelper.obtenerColumnasConTipos(conexion, nombreTabla);
            String columnaPK = MetadatosHelper.obtenerColumnaPK(conexion, nombreTabla);
            
           
            boolean pkAutoIncremental = false;
            if (columnaPK != null && tiposColumnas.containsKey(columnaPK)) {
                String tipoPK = tiposColumnas.get(columnaPK).toLowerCase();
                pkAutoIncremental = tipoPK.contains("serial");
            }
            
           
            int fila = 0;
            
            for (String nombreCol : nombresColumnas) {
               
                if (modo == Modo.INSERTAR && nombreCol.equals(columnaPK) && pkAutoIncremental) {
                    continue;
                }
                
               
                Label label = new Label(nombreCol + ":");
                
                TextField textField = new TextField();
                textField.setPrefWidth(250);
                
                
                if (modo == Modo.EDITAR && datosActuales != null) {
                    Object valor = datosActuales.get(nombreCol);
                    if (valor != null) {
                        textField.setText(valor.toString());
                    }
                    
                   
                    if (nombreCol.equals(columnaPK)) {
                        textField.setDisable(true);
                    }
                }
                
                
                camposTexto.put(nombreCol, textField);
                
                
                grid.add(label, 0, fila);
                grid.add(textField, 1, fila);
                
                fila++;
            }
            
            
            this.btnGuardar = new Button("Guardar");
            this.btnCancelar = new Button("Cancelar");
            
            btnGuardar.setPrefWidth(120);
            btnCancelar.setPrefWidth(120);
            
            
            btnGuardar.setOnAction(evento -> guardarRegistro());
            btnCancelar.setOnAction(evento -> this.close());
            
            
            grid.add(btnGuardar, 0, fila);
            grid.add(btnCancelar, 1, fila);
            
           
            Scene scene = new Scene(grid, 400, Math.min(600, (fila + 1) * 50 + 60));
            this.setScene(scene);
            
        } catch (SQLException e) {
            mostrarError("Error al generar formulario: " + e.getMessage());
            this.close();
        }
    }
    

   //metodo guardar registro
   
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
            
           
            String tipoDato = tiposColumnas.get(nombreCol);
            if (tipoDato != null) {
                tipoDato = tipoDato.toLowerCase();
            }
            
            
            if (tipoDato != null && (tipoDato.contains("numeric") || 
                tipoDato.contains("decimal") || tipoDato.contains("real") || 
                tipoDato.contains("float") || tipoDato.contains("double"))) {
                datos.put(nombreCol, Double.parseDouble(valorTexto));
            } 
            else if (tipoDato != null && (tipoDato.contains("int") || 
                     tipoDato.contains("serial"))) {
                datos.put(nombreCol, Integer.parseInt(valorTexto));
            }
            
            else {
                datos.put(nombreCol, valorTexto);
            }
        }
        
        
        boolean exito = false;
        
        if (modo == Modo.INSERTAR) {
            exito = crudController.insertRegist(datos);
        } else if (modo == Modo.EDITAR) {
            String columnaPK = MetadatosHelper.obtenerColumnaPK(conexion, nombreTabla);
            Object valorPK = datosActuales.get(columnaPK);
            exito = crudController.updateRegist(valorPK, datos);
        }
        
        
        if (exito) {
            mostrarInfo("Registro guardado correctamente");
            this.close();
        } else {
            mostrarError("No se pudo guardar el registro");
        }
        
    } catch (SQLException e) {
        mostrarError("Error al guardar: " + e.getMessage());
    } catch (NumberFormatException e) {
        mostrarError("Error: El precio debe ser un número válido (ej: 45.99)");
    }
}
    
 
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
   
    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
