package com.example.runnconnect.data.request;

import java.util.List;

public class GuardarRutaRequest {
  private List<RutaPuntoRequest> puntos;

  public GuardarRutaRequest(List<RutaPuntoRequest> puntos){
    this.puntos=puntos;
  }

}
