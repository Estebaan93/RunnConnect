package com.example.runnconnect.data.response;

import com.google.gson.annotations.SerializedName;

public class RutaPuntoResponse { //me representa cada punto geografico del circuito
  @SerializedName(value = "orden", alternate = {"Orden"})
  private int orden;

  @SerializedName(value = "latitud", alternate = {"Latitud"})
  private double latitud;

  @SerializedName(value = "longitud", alternate = {"Longitud"})
  private double longitud;

  public int getOrden() { return orden; }
  public double getLatitud() { return latitud; }
  public double getLongitud() { return longitud; }


}
