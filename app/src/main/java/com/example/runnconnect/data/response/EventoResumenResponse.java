package com.example.runnconnect.data.response;

import java.util.List;

public class EventoResumenResponse {
  private int idEvento;
  private String nombre;
  private String fechaHora; // Viene como string ISO (2025-12-05T09:00:00)
  private String lugar;
  private String estado; // "publicado", "finalizado", "cancelado" etc.
  private int cupoTotal;
  private int inscriptosActuales;
  private int cantidadCategorias;
  private String tipoEvento; //
  private String datosPago;
  private String nombreOrganizador;
  private List<CategoriaResponse> categorias;


  // Getters
  public int getIdEvento() { return idEvento; }
  public String getNombre() { return nombre; }
  public String getFechaHora() { return fechaHora; }
  public String getLugar() { return lugar; }
  public String getEstado() { return estado; }
  public int getCupoTotal() { return cupoTotal; }
  public int getInscriptosActuales() { return inscriptosActuales; }
  public String getTipoEvento() { return tipoEvento; }
  public String getDatosPago() { return datosPago; }
  public int getCantidadCategorias() { return cantidadCategorias; }
  public String getNombreOrganizador() { return nombreOrganizador; }
  public List<CategoriaResponse> getCategorias() { return categorias;
  }

}
