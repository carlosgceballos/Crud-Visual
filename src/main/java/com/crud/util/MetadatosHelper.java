package com.crud.util;

import java.sql.*;
import java.util.*;

public class MetadatosHelper {
    public static List<String> obtenerNombresColumnas(Connection conn, String tabla) throws SQLException {
      List<String> columnas = new ArrayList<>();
      tabla = tabla.toLowerCase();
      DatabaseMetaData metaData = conn.getMetaData();
      ResultSet rs = metaData.getColumns(null, null, tabla, null);

      while (rs.next()) {
        String nombreColumna = rs.getString("COLUMN_NAME");
        columnas.add(nombreColumna);
      }

      rs.close();
      return columnas;

    }

    public static String obtenerColumnaPK(Connection conn, String tabla) throws SQLException {
      tabla= tabla.toLowerCase();
      DatabaseMetaData metaData = conn.getMetaData();

      ResultSet rs = metaData.getPrimaryKeys(null, null, tabla);

      String nombrePK = null;

      if(rs.next()){
        nombrePK=rs.getString("COLUMN_NAME");

      }

      rs.close();
      return nombrePK;
    }

    public static Map<String, String> obtenerColumnasConTipos(Connection conn, String tabla) throws SQLException {
      Map<String, String> colTipos = new HashMap<>();

      tabla =tabla.toLowerCase();

      DatabaseMetaData metaData = conn.getMetaData();
      ResultSet rs = metaData.getColumns(null, null, tabla, null);

      while (rs.next()) {
        String nombreCol = rs.getString("COLUMN_NAME");
        String tipoCol = rs.getString("TYPE_NAME");

        colTipos.put(nombreCol, tipoCol);
        
      }

      rs.close();
      return colTipos;

    }
    
    public static boolean existeTabla(Connection conn, String tabla) throws SQLException {
      tabla = tabla.toLowerCase();
      DatabaseMetaData metaData = conn.getMetaData();

      ResultSet rs = metaData.getTables(null, null, tabla, null);

      boolean existe = rs.next();
      rs.close();
      return existe;
    }
}