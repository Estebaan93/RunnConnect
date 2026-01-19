package com.example.runnconnect.data.request;

public class CambiarEstadoPagoRequest {
  private String nuevoEstado;
  private String motivo;
  public CambiarEstadoPagoRequest(String nuevoEstado, String motivo) {
    this.nuevoEstado = nuevoEstado;
    this.motivo = motivo;
  }

}
