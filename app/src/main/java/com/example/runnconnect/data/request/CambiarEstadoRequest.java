package com.example.runnconnect.data.request;

public class CambiarEstadoRequest {
  private String nuevoEstado;
  private String motivo;
  public CambiarEstadoRequest(String nuevoEstado, String motivo) {
    this.nuevoEstado = nuevoEstado;
    this.motivo = motivo;
  }


}
