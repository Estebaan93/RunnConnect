package com.example.runnconnect.data.request;

public class RutaPuntoRequest {
  private int orden;
  private double latitud;
  private double longitud;

  public RutaPuntoRequest(int orden, double latitud, double longitud){
    this.orden=orden;
    this.latitud=latitud;
    this.longitud=longitud;
  }
  public int getOrden() { return orden; }
  public double getLatitud() { return latitud; }
  public double getLongitud() { return longitud; }
}
