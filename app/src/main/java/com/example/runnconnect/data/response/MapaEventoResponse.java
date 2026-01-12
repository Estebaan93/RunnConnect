package com.example.runnconnect.data.response;

import java.util.List;

public class MapaEventoResponse { //obj contenedor que me devuelve la api
  private int idEvento;
  private List<RutaPuntoResponse> ruta;
  /*Puntos de hidratacion*/


  public int getIdEvento() {
    return idEvento;
  }

  public List<RutaPuntoResponse> getRuta() {
    return ruta;
  }
}
