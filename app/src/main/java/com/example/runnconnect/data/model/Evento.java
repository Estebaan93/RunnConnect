//data/model/Evento
package com.example.runnconnect.data.model;

public class Evento {
  private int idEvento;
  private String nombre;
  private String descripcion;
  private String fechaHora; // Lo manejamos como String para facilitar el parseo del JSON
  private String lugar;
  private int cupoTotal;
  private String estado; // "publicado", "finalizado", etc.
  private String urlPronosticoClima;
  private String tipoEvento;
  private String datosPago;

  //si la api devuleve el obj Org anidado
  private int idOrganizador;

  //constructor
  public Evento(){}

  //get set
  public int getIdEvento() {
    return idEvento;
  }

  public void setIdEvento(int idEvento) {
    this.idEvento = idEvento;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getFechaHora() {
    return fechaHora;
  }

  public void setFechaHora(String fechaHora) {
    this.fechaHora = fechaHora;
  }

  public String getLugar() {
    return lugar;
  }

  public void setLugar(String lugar) {
    this.lugar = lugar;
  }

  public int getCupoTotal() {
    return cupoTotal;
  }

  public void setCupoTotal(int cupoTotal) {
    this.cupoTotal = cupoTotal;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }
  public String getTipoEvento() { return tipoEvento; }
  public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
  public String getUrlPronosticoClima() {
    return urlPronosticoClima;
  }

  public void setUrlPronosticoClima(String urlPronosticoClima) {
    this.urlPronosticoClima = urlPronosticoClima;
  }

  public String getDatosPago() {
    return datosPago;
  }

  public void setDatosPago(String datosPago) {
    this.datosPago = datosPago;
  }

  public int getIdOrganizador() {
    return idOrganizador;
  }

  public void setIdOrganizador(int idOrganizador) {
    this.idOrganizador = idOrganizador;
  }
}
