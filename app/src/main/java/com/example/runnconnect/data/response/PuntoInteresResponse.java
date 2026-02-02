package com.example.runnconnect.data.response;

import java.math.BigDecimal;

public class PuntoInteresResponse {
  private int idPuntoInteres;
  private int idEvento;
  private String tipo;
  private String nombre;
  private BigDecimal latitud;
  private BigDecimal longitud;

  public int getIdPuntoInteres() {
    return idPuntoInteres;
  }

  public String getTipo() {
    return tipo;
  }

  public String getNombre() {
    return nombre;
  }

  public BigDecimal getLatitud() {
    return latitud;
  }

  public BigDecimal getLongitud() {
    return longitud;
  }

}
