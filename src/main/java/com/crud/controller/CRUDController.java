package com.crud.controller;

import java.sql.*;
import java.util.*;

import com.crud.util.MetadatosHelper;

import javafx.collections.*;

public class CRUDController {

  private Connection conex;
  private String tableName;


  //construct
  public CRUDController(Connection conex, String tableName){
    this.conex=conex;
    this.tableName=tableName;
  }

  //read
  public ObservableList<Map<String, Object>> loadData() throws SQLException{
    ObservableList<Map<String, Object>> data = FXCollections.observableArrayList();
    String sql = "SELECT * FROM "+ this.tableName;

    Statement stmt = conex.createStatement();
    ResultSet rs = stmt.executeQuery(sql);

    ResultSetMetaData metaData = rs.getMetaData();
    int columnCount = metaData.getColumnCount();
    while (rs.next()) {
      Map<String, Object> fila = new HashMap<>();

      for(int i =1; i<=columnCount; i++){
        String columnName = metaData.getColumnName(i);
        Object valor = rs.getObject(i);
        fila.put(columnName, valor);
      }

      data.add(fila);
    }

    rs.close();
    stmt.close();

    return data;
  }

  //create
  public boolean insertRegist(Map<String, Object> datos) throws SQLException {
    String columnPK = com.crud.util.MetadatosHelper.obtenerColumnaPK(this.conex, this.tableName);
    Map<String, String> tipos = com.crud.util.MetadatosHelper.obtenerColumnasConTipos(this.conex,this.tableName);
    boolean isAutoIncrement = false;
    if(columnPK != null && tipos.containsKey(columnPK)){
      String tipoPK = tipos.get(columnPK).toLowerCase();
      isAutoIncrement=tipoPK.contains("serial");
    }

    List<String> columnNames = new ArrayList<>();
    List<Object> columnVals = new ArrayList<>();

    for (Map.Entry<String, Object> entry : datos.entrySet()){
      String columnName = entry.getKey();
      Object valor = entry.getValue();

      //saltar pk
      if(columnName.equals(columnPK) && isAutoIncrement){
        continue;
      }

      columnNames.add(columnName);
      columnVals.add(valor);
    }

    String columnStr = String.join(", ", columnNames);
    columnStr = "("+columnStr+")";

    String placeholder = String.join(", ", Collections.nCopies(columnVals.size(), "?"));
    placeholder = "("+placeholder+")";

    String sql = "insert into "+tableName+" "+columnStr+" values "+placeholder;

    PreparedStatement pStatement = conex.prepareStatement(sql);

     for(int i =0; i< columnVals.size(); i++){
      pStatement.setObject(i+1, columnVals.get(i));
     }

     int filasTocadas =  pStatement.executeUpdate();
     pStatement.close();
     return filasTocadas >0;
  }
    
  //UPDATE
  public boolean updateRegist(Object valorPK, Map<String, Object> nuevosDatos) throws SQLException {
    String columnPK = com.crud.util.MetadatosHelper.obtenerColumnaPK(this.conex, this.tableName);
    List<String> columnsName = new ArrayList<>();
    List<Object> columnsVal = new ArrayList<>();

    for(Map.Entry<String, Object> entry : nuevosDatos.entrySet()){
      String columnName = entry.getKey();
      Object valor = entry.getValue();

      if(columnName.equals(columnPK)){
        continue;
      }

      columnsName.add(columnName);
      columnsVal.add(valor);
    }

    //validacion antes del query
    if (columnsName.isEmpty()) {
      return false; 
    }

    List<String> setTemp = new ArrayList<>();
    for(String column : columnsName){
      setTemp.add(column+"=?");
    }
    String sqlSet = String.join(", ", setTemp);

    String sql = "update "+this.tableName+" set "+sqlSet+" where "+columnPK+"=?";

    PreparedStatement pStatement = conex.prepareStatement(sql);

    for(int i=0; i<columnsVal.size();i++){
      pStatement.setObject(i+1, columnsVal.get(i));
    }

    int finalPosicion = columnsVal.size() +1;
    pStatement.setObject(finalPosicion, valorPK);
    int filasTocadas = pStatement.executeUpdate();
    return filasTocadas > 0;
  }
    
  //DELETE
  public boolean deleteRegist(Object valorPK) throws SQLException {
    String columnPK = com.crud.util.MetadatosHelper.obtenerColumnaPK(conex, tableName);

    String sql = "delete from "+this.tableName+" where "+columnPK+"=?";
    PreparedStatement pStatement = conex.prepareStatement(sql);

    pStatement.setObject(1, valorPK);

    int filasTocadas=pStatement.executeUpdate();
    pStatement.close();
    return filasTocadas >0;
  }

}
