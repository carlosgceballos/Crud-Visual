package com.crud.view;

import java.lang.reflect.GenericDeclaration;
import java.sql.*;
import com.crud.util.ConexionDB;
import com.crud.util.MetadatosHelper;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;


public class VentanaConexion extends Stage {
    private TextField txtHost;
    private TextField txtPuerto;
    private TextField txtBD;
    private TextField txtUsuario;
    private PasswordField txtPassword;
    private TextField txtTabla;
    private Button btnConectar;

    //atributo- logica
    private ConexionDB conexionDB;

    //constructor
    public VentanaConexion(){
       this.conexionDB =  new ConexionDB(); 

       inicializarUI();

        
       this.setTitle("Conexion a Base de datos");
       this.setResizable(false);
    }

    //metodo para inicializarUI

    private void inicializarUI(){
        GridPane grid = new GridPane();

        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setAlignment(Pos.CENTER);

        Label lblHost = new Label("Host");
        Label lblPuerto = new Label("Puerto");
        Label lblDB = new Label("Base de Datos");
        Label lblUsuario = new Label("Usuario");
        Label lblPassword = new Label("Password");
        Label lblTabla = new Label("Tabla");

        this.txtHost = new TextField();
        this.txtPuerto = new TextField();
        this.txtBD = new TextField();
        this.txtUsuario = new TextField();
        this.txtPassword = new PasswordField();
        this.txtTabla = new TextField();

        txtHost.setText("localhost");
        txtPuerto.setText("5432");
        txtUsuario.setText("postgres");
        
        txtHost.setPrefWidth(200);
        txtPuerto.setPrefWidth(200);
        txtBD.setPrefWidth(200);
        txtUsuario.setPrefWidth(200);
        txtPassword.setPrefWidth(200);
        txtTabla.setPrefWidth(200);

        this.btnConectar = new Button("Conectar");
        btnConectar.setPrefWidth(200);

        btnConectar.setOnAction(evento -> intentarConexion());

        //filao: host
        grid.add(lblHost,0 ,0 );
        grid.add(txtHost, 1, 0);

        //fila1: puerto
        grid.add(lblPuerto, 0, 1);
        grid.add(txtPuerto, 1, 1);

        //fila2: base de datos
        grid.add(lblDB, 0, 2);
        grid.add(txtBD, 1, 2);

        //fila3: usuario
        grid.add(lblUsuario, 0, 3);
        grid.add(txtUsuario, 1, 3);

        //fila4: password
        grid.add(lblPassword, 0, 4);
        grid.add(txtPassword, 1, 4);

        //fila5:tabla
        grid.add(lblTabla, 0, 5);
        grid.add(txtTabla, 1, 5);

        //fila6: boton
        grid.add(btnConectar, 0,6, 2,  1);
        GridPane.setHalignment(btnConectar, HPos.CENTER );

        Scene scene = new Scene(grid, 350, 300);

        this.setScene(scene);

    

        

    }
     
   
    //Metodo para intentar conectar
    private void intentarConexion(){

        String host = txtHost.getText().trim();
        String puerto = txtPuerto.getText().trim();
        String bd = txtBD.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText();
        String tabla = txtTabla.getText().trim();

        if (host.isEmpty()|| puerto.isEmpty() || bd.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
        mostarError("Todos los campos son obligatorios");
        return;    
        }

        try{

            Connection conexion = this.conexionDB.conect(host, puerto, bd, usuario, password);
            
            boolean existe = MetadatosHelper.existeTabla(conexion, tabla);

            if (!existe) {
                mostarError("La tabla "+tabla+" no existe en la base de datos");
                this.conexionDB.desconect();

                return;
            }

            VentanaPrincipal ventanaPrincipal = new VentanaPrincipal(this.conexionDB, tabla);
            ventanaPrincipal.show();

            this.close();

        } catch(SQLException e) {
            mostarError("Error de conexion:" + e.getMessage());

        } catch (IllegalArgumentException e) {
            mostarError(e.getMessage());
        }
    }

    //funcion mostarError
    private void mostarError(String mensaje){
        
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText(mensaje);
    alert.setContentText(mensaje);

    alert.showAndWait();
    
    }

    


    
}
