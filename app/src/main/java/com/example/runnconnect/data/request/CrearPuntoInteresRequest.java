package com.example.runnconnect.data.request;

import com.google.gson.annotations.SerializedName;

public class CrearPuntoInteresRequest {
  @SerializedName("tipo")
  private String tipo; //hidratacion, primerosAuxilios etc

  @SerializedName("nombre")
  private String nombre;
  @SerializedName("latitud")
  private  double latitud;
  @SerializedName("longitud")
  private double longitud;

  public CrearPuntoInteresRequest(String tipo, String nombre,double latitud, double longitud) {
    this.tipo = tipo;
    this.nombre=nombre;
    this.latitud = latitud;
    this.longitud = longitud;
  }
  public String getTipo() { return tipo; }
  public double getLatitud() { return latitud; }
  public double getLongitud() { return longitud; }

}
