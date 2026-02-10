package com.crud;

import com.crud.view.VentanaConexion;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Título
        Label titulo = new Label("Mi CRUD con JavaFX + PostgreSQL");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Botón para probar conexión
        Button btnConectar = new Button("Probar Conexión a PostgreSQL");
        btnConectar.setStyle("-fx-font-size: 14px;");

         Button btnAbrirCRUD = new Button("Abrir CRUD Completo");
        btnAbrirCRUD.setStyle("-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        // Área de texto para mostrar resultados
        TextArea txtResultado = new TextArea();
        txtResultado.setEditable(false);
        txtResultado.setPrefHeight(250);
        
        // Acción del botón
        btnConectar.setOnAction(e -> {
            String resultado = probarConexion();
            txtResultado.setText(resultado);
        });
        
        btnAbrirCRUD.setOnAction(e -> {
            VentanaConexion ventanaCRUD = new VentanaConexion();
            ventanaCRUD.show();
        });
        // Layout
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(titulo, btnConectar, btnAbrirCRUD, txtResultado);
        
        // Escena y ventana
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("CRUD JavaFX + PostgreSQL");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    // Método para probar la conexión a PostgreSQL
    private String probarConexion() {
        String url = "jdbc:postgresql://localhost:5432/crud_visual";
        String usuario = "postgres";
        String password = "M@fer.98";
        
        try {
            Connection conn = DriverManager.getConnection(url, usuario, password);
            String mensaje = "✓ ¡Conexión exitosa a PostgreSQL!\n\n";
            mensaje += "Base de datos: " + conn.getCatalog() + "\n";
            mensaje += "Usuario: " + usuario + "\n";
            mensaje += "URL: " + url;
            conn.close();
            return mensaje;
            
        } catch (SQLException ex) {
            return "✗ Error de conexión:\n\n" + ex.getMessage() + "\n\n" +
                   "Verifica que PostgreSQL esté corriendo y que los datos sean correctos.";
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}