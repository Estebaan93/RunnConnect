package com.example.runnconnect.data.response;

import java.util.List;

public class EventosPaginadosResponse {
  private List<EventoResumenResponse> eventos;
  private int paginaActual;
  private int totalPaginas;
  private int totalEventos;
  private int tamanioPagina;

  public List<EventoResumenResponse> getEventos() {
    return eventos;
  }
  public int getTotalPaginas() { return totalPaginas; }
  public int getPaginaActual() { return paginaActual; }
  public int getTotalEventos() { return totalEventos; }

}
