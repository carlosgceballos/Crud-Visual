package com.crud.util;

import java.sql.*;

public class ConexionDB {
  private Connection conexion;
  
  public ConexionDB(){
    this.conexion=null;
  }

  public Connection conect(String host, String puerto, String bd, String usuario, String password) throws SQLException{

    if (host == null || host.isBlank()) {
      throw new IllegalArgumentException("El parámetro 'host' es requerido");
    }
    
    if (bd == null || bd.isBlank()) {
      throw new IllegalArgumentException("El parámetro 'base de datos' es requerido");
    }
    
    if (usuario == null || usuario.isBlank()) {
      throw new IllegalArgumentException("El parámetro 'usuario' es requerido");
    }
    
    int puertoNumerico;
    try {
      puertoNumerico = Integer.parseInt(puerto.trim());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
        "El puerto debe ser un número. Valor recibido: '" + puerto + "'", e
      );
    }
    
    if (puertoNumerico < 1 || puertoNumerico > 65535) {
      throw new IllegalArgumentException(
        "Puerto fuera de rango válido (1-65535). Valor: " + puertoNumerico
      );
    }
    
    if (host.contains(" ")) {
      throw new IllegalArgumentException("El host no puede contener espacios");
    }
    
    host = host.trim();
    bd = bd.trim();
    usuario = usuario.trim();

    var url = "jdbc:postgresql://"+host+":"+puertoNumerico+"/"+bd;

    if (conexion != null){ 
      try {
        desconect(); 
      } catch (Exception e) {
        conexion = null;
      }
    }
    
    this.conexion = DriverManager.getConnection(url,usuario,password);
    return this.conexion;

  }

  public boolean hayConexionActiva() {
    if (conexion == null) {
      return false; 
    }
    try {
      return !conexion.isClosed();
    } catch (SQLException e) {
      return false;
    }
  }

  public void desconect() {
    if (conexion != null) {
      try {
        if (!conexion.isClosed()) {
            conexion.close();
        }
      } catch (SQLException e) {
          System.err.println("Error en desconect(): " + e.getMessage());
        } finally {
        conexion = null;
      }
    }
  }

  public Connection getConexion() {
    return this.conexion;
  }

}
