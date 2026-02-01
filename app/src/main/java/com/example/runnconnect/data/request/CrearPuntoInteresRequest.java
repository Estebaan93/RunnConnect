package com.example.runnconnect.data.request;

public class CrearPuntoInteresRequest {
  private String tipo; //hidratacion, primerosAuxilios etc
  private  double latitud;
  private double longitud;

  public CrearPuntoInteresRequest(String tipo, double latitud, double longitud) {
    this.tipo = tipo;
    this.latitud = latitud;
    this.longitud = longitud;
  }
  public String getTipo() { return tipo; }
  public double getLatitud() { return latitud; }
  public double getLongitud() { return longitud; }

}
