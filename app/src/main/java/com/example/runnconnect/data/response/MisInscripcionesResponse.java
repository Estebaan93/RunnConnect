package com.example.runnconnect.data.response;

import java.math.BigDecimal;
import java.util.List;

public class MisInscripcionesResponse {
  private int total;
  private List<InscripcionItem> inscripciones;

  public int getTotal() {
    return total;
  }

  public List<InscripcionItem> getInscripciones() {
    return inscripciones;
  }

  public static class InscripcionItem {
    private int idInscripcion;
    private String fechaInscripcion;
    private String estadoPago;
    private String talleRemera;
    private boolean aceptoDeslinde;
    private String comprobantePagoURL;
    private String observacion;
    private EventoInscripcionInfo evento;
    private CategoriaInscripcionInfo categoria;

    public int getIdInscripcion() { return idInscripcion; }
    public String getFechaInscripcion() { return fechaInscripcion; }
    public String getEstadoPago() { return estadoPago; }
    public String getTalleRemera() { return talleRemera; }
    public boolean isAceptoDeslinde() { return aceptoDeslinde; }
    public String getComprobantePagoURL() { return comprobantePagoURL; }
    public String getObservacion() { return observacion; }
    public EventoInscripcionInfo getEvento() { return evento; }
    public CategoriaInscripcionInfo getCategoria() { return categoria; }
  }

  public static class EventoInscripcionInfo {
    private int idEvento;
    private String nombre;
    private String fechaHora;
    private String lugar;
    private String estado;
    private String datosPago;

    public int getIdEvento() { return idEvento; }
    public String getNombre() { return nombre; }
    public String getFechaHora() { return fechaHora; }
    public String getLugar() { return lugar; }
    public String getEstado() { return estado; }
    public String getDatosPago() { return datosPago; }
  }

  public static class CategoriaInscripcionInfo {
    private int idCategoria;
    private String nombre;
    private BigDecimal costoInscripcion;
    private String genero;
    private int edadMinima;
    private int edadMaxima;

    public int getIdCategoria() { return idCategoria; }
    public String getNombre() { return nombre; }
    public BigDecimal getCostoInscripcion() { return costoInscripcion; }
    public String getGenero() { return genero; }
    public int getEdadMinima() { return edadMinima; }
    public int getEdadMaxima() { return edadMaxima; }
  }
}
