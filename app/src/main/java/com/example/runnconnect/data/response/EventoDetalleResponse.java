package com.example.runnconnect.data.response;

import java.util.List;

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
  private String tipoEvento;
  private OrganizadorEventoResponse organizador;
  private List<CategoriaResponse> categorias;

  // Getters
  public int getIdEvento() { return idEvento; }

  public String getNombre() { return nombre; }
  public String getDescripcion() { return descripcion; }
  public String getFechaHora() { return fechaHora; }

  public String getLugar() { return lugar; }
  public Integer getCupoTotal() { return cupoTotal; }
  public int getInscriptosActuales() { return inscriptosActuales; }
  public int getCuposDisponibles() {return cuposDisponibles; }

  public String getEstado() { return estado; }
  public String getUrlPronosticoClima() { return urlPronosticoClima; }
  public String getDatosPago() { return datosPago; }
  public String getTipoEvento() { return tipoEvento; }

  public OrganizadorEventoResponse getOrganizador() { return organizador; }
  public List<CategoriaResponse> getCategorias() { return categorias; }

  //SETTER
  public void setEstado(String estado) {
    this.estado = estado;
  }

  public static class OrganizadorEventoResponse {
    private int idUsuario;
    private String nombre;
    private String email;
    private String telefono;

    public String getNombre() { return nombre; }

    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
  }


}
