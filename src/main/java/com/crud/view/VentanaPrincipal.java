package com.crud.view;

import javafx.stage.Stage;
import java.lang.reflect.GenericDeclaration;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.crud.controller.CRUDController;
import com.crud.util.ConexionDB;
import com.crud.util.MetadatosHelper;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;


public class VentanaPrincipal extends Stage {

    private ConexionDB conexionDB;
    private String nombreTabla;
    private CRUDController crudController;

    private TableView<Map<String, Object>>tableView;
    private Button btnNuevo;
    private Button btnEliminar;
    private Button btnRefrescar;
    private Button btnEditar;

    public VentanaPrincipal(ConexionDB conexionDB, String nombreTabla){

        this.conexionDB = conexionDB;
        this.nombreTabla =  nombreTabla;

        this.crudController = new CRUDController(conexionDB.getConexion(), nombreTabla);

        inicialiarUI();

        generarColumnasDinamicas();

        cargarDatos();

        this.setTitle("CRUD - " + nombreTabla);

        this.setOnCloseRequest(evento -> {
            conexionDB.desconect();
        });
        

    }

    //metodo inicializar UI

    private void inicialiarUI(){

        this.tableView = new TableView<>();

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        this.btnNuevo = new Button("Nuevo");
        this.btnEditar = new Button("Editar");
        this.btnEliminar=new Button("Eliminar");
        this.btnRefrescar = new Button("Refrescar");

        btnNuevo.setPrefWidth(100);
        btnEditar.setPrefWidth(100);
        btnEliminar.setPrefWidth(100);
        btnRefrescar.setPrefWidth(100);

        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);

        btnNuevo.setOnAction(evento -> abrirFormularioNuevo());
        btnEditar.setOnAction(evento -> abrirFormularioEditar());
        btnEliminar.setOnAction(evento -> eliminarRegistro());
        btnRefrescar.setOnAction(evento -> cargarDatos());

        //Listener para la seleccion en tableview

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean haySeleccion = (newValue != null);
            btnEditar.setDisable(!haySeleccion);
            btnEliminar.setDisable(!haySeleccion);
        });

        //Hbox para los botones

        HBox botonesBox = new HBox(10);
        botonesBox.setPadding(new Insets(10, 10, 10, 10));
        botonesBox.getChildren().addAll(btnNuevo,btnEditar,btnEliminar,btnRefrescar);

        BorderPane root = new BorderPane();
        root.setTop(botonesBox);
        root.setCenter(tableView);

        Scene scene = new Scene(root, 900, 600);
        this.setScene(scene);


    }

    private void generarColumnasDinamicas() {
        try {
            // Obtener nombres de columnas de la tabla
            List<String> nombresColumnas = MetadatosHelper.obtenerNombresColumnas(
                conexionDB.getConexion(),
                nombreTabla
            );
            
            // Por cada columna, crear un TableColumn
            for (String nombreCol : nombresColumnas) {
                // Crear TableColumn genérico
                TableColumn<Map<String, Object>, String> columna = 
                    new TableColumn<>(nombreCol);
                
                // Configurar cómo obtener el valor de la celda
                columna.setCellValueFactory(cellData -> {
                    // cellData representa una fila completa
                    Map<String, Object> fila = cellData.getValue(); // Es un Map<String, Object>
                    
                    // Obtener el valor de esta columna específica
                    Object valor = fila.get(nombreCol);
                    
                    // Convertir a String
                    String valorStr = (valor == null) ? "" : valor.toString();
                    
                    // Retornar como SimpleStringProperty (requerido por JavaFX)
                    return new SimpleStringProperty(valorStr);
                });
                
                // Agregar columna al TableView
                tableView.getColumns().add(columna);
            }
            
        } catch (SQLException e) {
            mostrarError("Error al generar columnas: " + e.getMessage());
        }
    }
    
   
    private void cargarDatos() {
        try {
           
            ObservableList<Map<String, Object>> datos = crudController.loadData();
            
            
            tableView.setItems(datos);
            
        } catch (SQLException e) {
            mostrarError("Error al cargar datos: " + e.getMessage());
        }
    }
    

    private void abrirFormularioNuevo() {
        // Crear ventana de formulario en modo INSERT
        VentanaFormulario ventanaFormulario = new VentanaFormulario(
            conexionDB.getConexion(),
            nombreTabla,
            VentanaFormulario.Modo.INSERTAR,
            null // No hay datos actuales
        );
        
        
        ventanaFormulario.setOnHidden(evento -> {
            // Cuando el formulario se cierre, recargar datos
            cargarDatos();
        });
        
        // Mostrar ventana
        ventanaFormulario.show();
    }
    
   
    private void abrirFormularioEditar() {
        
        Map<String, Object> filaSeleccionada = tableView.getSelectionModel().getSelectedItem();
        
        
        if (filaSeleccionada == null) {
            mostrarError("Selecciona un registro para editar");
            return;
        }
        
       
        VentanaFormulario ventanaFormulario = new VentanaFormulario(
            conexionDB.getConexion(),
            nombreTabla,
            VentanaFormulario.Modo.EDITAR,
            filaSeleccionada 
        );
        
       
        ventanaFormulario.setOnHidden(evento -> cargarDatos());
        
       
        ventanaFormulario.show();
    }
    
    
    private void eliminarRegistro() {
     
        Map<String, Object> filaSeleccionada = tableView.getSelectionModel().getSelectedItem();
        
       
        if (filaSeleccionada == null) {
            mostrarError("Selecciona un registro para eliminar");
            return;
        }
        
        // Pedir confirmación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Estás seguro?");
        confirmacion.setContentText("Esta acción no se puede deshacer");
        
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        
        
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            return; 
        }
        
        
        try {
            String columnaPK = MetadatosHelper.obtenerColumnaPK(
                conexionDB.getConexion(),
                nombreTabla
            );
            
            Object valorPK = filaSeleccionada.get(columnaPK);
            
            
            boolean exito = crudController.deleteRegist(valorPK);
            
            
            if (exito) {
                mostrarInfo("Registro eliminado correctamente");
                cargarDatos(); 
            } else {
                mostrarError("No se pudo eliminar el registro");
            }
            
        } catch (SQLException e) {
            mostrarError("Error al eliminar: " + e.getMessage());
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
