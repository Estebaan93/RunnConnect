package com.example.runnconnect.data.response;

import java.util.List;
import java.util.Map;

public class ListaInscriptosResponse {
  private int idEvento;
  private String nombreEvento;
  private int totalInscripciones;
  private int paginaActual;
  private int totalPaginas;
  private Map<String, Integer> estadisticas; // "pagado": 10, "pendiente": 5
  private List<InscriptoEventoResponse> inscripciones;

  public List<InscriptoEventoResponse> getInscripciones() { return inscripciones; }
  public Map<String, Integer> getEstadisticas() { return estadisticas; }
  public int getTotalInscripciones() { return totalInscripciones; }


}
