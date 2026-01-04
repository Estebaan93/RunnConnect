package com.example.runnconnect.data.response;

import java.util.List;

public class EventosPaginadosResponse {
  private List<EventoResumenResponse> eventos;
  private int paginaActual;
  private int totalPaginas;

  public List<EventoResumenResponse> getEventos() {
    return eventos;
  }

}
