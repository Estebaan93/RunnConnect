package com.example.runnconnect.data.response;

public class ContadorNotificacionesResponse {
  private int cantidadRecientes;
  private boolean tieneNuevas;

  public int getCantidadRecientes() {
    return cantidadRecientes;
  }

  public void setCantidadRecientes(int cantidadRecientes) {
    this.cantidadRecientes = cantidadRecientes;
  }

  public boolean isTieneNuevas() {
    return tieneNuevas;
  }

  public void setTieneNuevas(boolean tieneNuevas) {
    this.tieneNuevas = tieneNuevas;
  }
}
