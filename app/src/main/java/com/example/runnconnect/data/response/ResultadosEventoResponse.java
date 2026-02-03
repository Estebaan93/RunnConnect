package com.example.runnconnect.data.response;

import java.util.List;

public class ResultadosEventoResponse {
  // Coincide con la propiedad "Evento" de C#
  private EventoResultadoInfo evento;

  // Coincide con "TotalParticipantes"
  private int totalParticipantes;

  // Coincide con "TotalConResultado"
  private int totalConResultado;

  // Coincide con "TotalSinResultado"
  private int totalSinResultado;

  // Coincide con la lista "Resultados"
  private List<ResultadoEventoItem> resultados;

  // GETTERS
  public EventoResultadoInfo getEvento() { return evento; }
  public int getTotalParticipantes() { return totalParticipantes; }
  public int getTotalConResultado() { return totalConResultado; }
  public int getTotalSinResultado() { return totalSinResultado; }
  public List<ResultadoEventoItem> getResultados() { return resultados; }

  //  CLASES INTERNAS (DTOs Anidados)
  public static class EventoResultadoInfo {
    private int idEvento;
    private String nombre;
    private String fechaHora; // Usamos String para recibir la fecha sin problemas
    private String lugar;
    private String estado;

    public int getIdEvento() { return idEvento; }
    public String getNombre() { return nombre; }
    public String getFechaHora() { return fechaHora; }
    public String getLugar() { return lugar; }
    public String getEstado() { return estado; }
  }

  public static class ResultadoEventoItem {
    private int idResultado;
    private int idInscripcion;

    // runner
    private String nombreRunner;
    private Integer dniRunner; // Integer permite null
    private String genero;
    private String agrupacion;

    // categoria
    private String nombreCategoria;

    // resultados
    private String tiempoOficial;
    private Integer posicionGeneral;
    private Integer posicionCategoria;

    // getters
    public int getIdResultado() { return idResultado; }
    public int getIdInscripcion() { return idInscripcion; }
    public String getNombreRunner() { return nombreRunner; }
    public Integer getDniRunner() { return dniRunner; }
    public String getGenero() { return genero; }
    public String getAgrupacion() { return agrupacion; }
    public String getNombreCategoria() { return nombreCategoria; }
    public String getTiempoOficial() { return tiempoOficial; }
    public Integer getPosicionGeneral() { return posicionGeneral; }
    public Integer getPosicionCategoria() { return posicionCategoria; }
  }

}
