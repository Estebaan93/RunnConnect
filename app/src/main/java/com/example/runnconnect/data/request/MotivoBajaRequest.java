package com.example.runnconnect.data.request;

public class MotivoBajaRequest {
  private String motivo;

  //constructor


  public MotivoBajaRequest(String motivo) {
    this.motivo = motivo;
  }

  //getset
  public String getMotivo() { return motivo; }

  public void setMotivo(String motivo) { this.motivo = motivo; }
}
