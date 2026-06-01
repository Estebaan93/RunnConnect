package com.example.runnconnect.data.request;

import com.google.gson.annotations.SerializedName;

public class CambiarEstadoCategoriaRequest {

  @SerializedName("NuevoEstado")
  private String nuevoEstado;

  @SerializedName("Motivo")
  private String motivo;

  public CambiarEstadoCategoriaRequest(String nuevoEstado, String motivo) {
    this.nuevoEstado = nuevoEstado;
    this.motivo = motivo;
  }

  public String getNuevoEstado() {
    return nuevoEstado;
  }

  public void setNuevoEstado(String nuevoEstado) {
    this.nuevoEstado = nuevoEstado;
  }

  public String getMotivo() {
    return motivo;
  }

  public void setMotivo(String motivo) {
    this.motivo = motivo;
  }
}