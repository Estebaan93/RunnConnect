package com.example.runnconnect.data.request;

public class ActualizarEventoRequest {
  private String nombre;
  private String descripcion;
  private String fechaHora; // Formato ISO: "2025-10-20T10:00:00"
  private String lugar;
  private Integer cupoTotal;
  private String urlPronosticoClima;
  private String datosPago;

  // Constructor vacío
  public ActualizarEventoRequest() {}

  // Constructor completo (opcional)
  public ActualizarEventoRequest(String nombre, String descripcion, String fechaHora, String lugar, Integer cupoTotal, String datosPago) {
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.fechaHora = fechaHora;
    this.lugar = lugar;
    this.cupoTotal = cupoTotal;
    this.datosPago = datosPago;
  }

  // Getters y Setters (Necesarios para que GSON convierta a JSON y para el ViewModel)
  public void setNombre(String nombre) { this.nombre = nombre; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
  public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }
  public void setLugar(String lugar) { this.lugar = lugar; }
  public void setCupoTotal(Integer cupoTotal) { this.cupoTotal = cupoTotal; }
  public void setUrlPronosticoClima(String urlPronosticoClima) { this.urlPronosticoClima = urlPronosticoClima; }
  public void setDatosPago(String datosPago) { this.datosPago = datosPago; }
}

