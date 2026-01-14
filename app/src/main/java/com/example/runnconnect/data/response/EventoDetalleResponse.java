package com.example.runnconnect.data.response;

public class EventoDetalleResponse {
  private int idEvento;
  private String nombre;
  private String descripcion;
  private String fechaHora;
  private String lugar;
  private Integer cupoTotal;
  private int inscriptosActuales;
  private int cuposDisponibles;
  private String estado;
  private String urlPronosticoClima;
  private String datosPago;

  // Getters
  public int getIdEvento() { return idEvento; }
  public String getNombre() { return nombre; }
  public String getDescripcion() { return descripcion; }
  public String getFechaHora() { return fechaHora; }
  public String getLugar() { return lugar; }
  public Integer getCupoTotal() { return cupoTotal; }
  public int getInscriptosActuales() { return inscriptosActuales; }
  public String getEstado() { return estado; }
  public String getDatosPago() { return datosPago; }

  //SETTER
  public void setEstado(String estado) {
    this.estado = estado;
  }

}
