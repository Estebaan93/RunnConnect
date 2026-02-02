package com.example.runnconnect.data.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PuntosInteresEventoResponse {
  @SerializedName("puntosInteres") // Debe coincidir con el JSON de C#
  private List<PuntoInteresResponse> puntosInteres;

  public List<PuntoInteresResponse> getPuntosInteres() {
    return puntosInteres;
  }

}
