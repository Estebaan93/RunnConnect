package com.example.runnconnect.data.response;

public class RutaPuntoResponse { //me representa cada punto geografico del circuito
  private int orden;
  private double latitud;
  private double longitud;

  public int getOrden() {
    return orden;
  }

  public double getLatitud() {
    return latitud;
  }

  public double getLongitud() {
    return longitud;
  }
}
