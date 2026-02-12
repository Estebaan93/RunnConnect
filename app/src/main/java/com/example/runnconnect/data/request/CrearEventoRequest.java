package com.example.runnconnect.data.request;

import java.util.List;

public class CrearEventoRequest {
  private String nombre;
  private String descripcion;
  private String fechaHora; // Formato ISO "2025-12-05T09:00:00"
  private String lugar;
  private int cupoTotal;
  private String urlPronosticoClima;
  private String datosPago; // el precio/CBU temporalmente
  private String tipoEvento;
  private List<CrearCategoriaRequest> categorias;
  public CrearEventoRequest(String nombre, String descripcion, String fechaHora, String lugar, int cupoTotal, String urlPronosticoClima, String datosPago, String tipoEvento, List<CrearCategoriaRequest> categorias) {
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.fechaHora = fechaHora;
    this.lugar = lugar;
    this.cupoTotal = cupoTotal;
    this.urlPronosticoClima = urlPronosticoClima;
    this.datosPago = datosPago;
    this.tipoEvento= tipoEvento;
    this.categorias= categorias;
  }


}
