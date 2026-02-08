package com.crud.model;

public class ColumnInfo {
  private String ColumnName;
  private String DataType;
  private boolean isPK;
  private boolean isNullable;
  private boolean isAutoIncrement;


  public ColumnInfo(){
    this.ColumnName = null;
    this.DataType = null;
    this.isPK = false;
    this.isNullable = false;
    this.isAutoIncrement = false;
  }

  public ColumnInfo(String ColumnName, String DataType, boolean isPK, boolean isNullable, boolean isAutoIncrement) {
    this.ColumnName= ColumnName;
    this.DataType=DataType;
    this.isPK=isPK;
    this.isNullable=isNullable;
    this.isAutoIncrement=isAutoIncrement;
  }
    
  // GETTERS Y SETTERS
  public String getName(){
    return this.ColumnName;
  }

  public void setName(String ColumnName){
    this.ColumnName = ColumnName;
  }

  public String getDataType(){
    return this.DataType;
  }

  public void setDataType(String DataType){
    this.DataType= DataType;
  }

  public boolean isPK(){
    return this.isPK;
  }

  public void setPK(boolean isPK){
    this.isPK=isPK;
  }

  public boolean isNullable(){
    return this.isNullable;
  }

  public void setNullable(boolean isNullable){
    this.isNullable=isNullable;
  }

  public boolean isAutoIncrement(){
    return this.isAutoIncrement;
  }

  public void setAutoIncrement(boolean isAutoIncrement){
    this.isAutoIncrement=isAutoIncrement;
  }

  @Override
  public String toString() {
    return String.format(
        "ColumnaInfo{nombreColumna='%s', tipoDato='%s', esPrimaryKey=%s, esNullable=%s, esAutoincremental=%s}",
        ColumnName, DataType, isPK, isNullable, isAutoIncrement
    );
  }


}
