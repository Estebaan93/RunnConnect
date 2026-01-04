package com.example.runnconnect.data.response;

public class EventoResumenResponse {
  private int idEvento;
  private String nombre;
  private String fechaHora; // Viene como string ISO
  private String lugar;
  private String estado; // "publicado", "finalizado", etc.
  private Integer cupoTotal;
  private int inscriptosActuales;
  private int cantidadCategorias;

  // Getters
  public int getIdEvento() { return idEvento; }
  public String getNombre() { return nombre; }
  public String getFechaHora() { return fechaHora; }
  public String getLugar() { return lugar; }
  public String getEstado() { return estado; }
  public Integer getCupoTotal() { return cupoTotal; }
  public int getInscriptosActuales() { return inscriptosActuales; }
  public int getCantidadCategorias() { return cantidadCategorias; }

}
