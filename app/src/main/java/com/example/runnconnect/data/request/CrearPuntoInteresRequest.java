package com.example.runnconnect.data.request;

public class CrearPuntoInteresRequest {
  private String tipo; //hidratacion, primerosAuxilios etc
  private String nombre;
  private  double latitud;
  private double longitud;

  public CrearPuntoInteresRequest(String tipo, String nombre, double latitud, double longitud) {
    this.tipo = tipo;
    this.nombre = nombre;
    this.latitud = latitud;
    this.longitud = longitud;
  }

}
