package com.example.runnconnect.ui.runner.buscarEventos;

public class CategoriaCompatibilidadUI {
  private final int idCategoria;
  private final String nombre;
  private final String costoTexto;
  private final String cuposTexto;
  private final String requisitosTexto;
  private final String estadoCompatibilidadTexto;
  private final boolean esCompatible;
  private final int colorEstadoCompatibilidad;

  public CategoriaCompatibilidadUI(int idCategoria,
                                   String nombre,
                                   String costoTexto,
                                   String cuposTexto,
                                   String requisitosTexto,
                                   String estadoCompatibilidadTexto,
                                   boolean esCompatible,
                                   int colorEstadoCompatibilidad) {
    this.idCategoria = idCategoria;
    this.nombre = nombre;
    this.costoTexto = costoTexto;
    this.cuposTexto = cuposTexto;
    this.requisitosTexto = requisitosTexto;
    this.estadoCompatibilidadTexto = estadoCompatibilidadTexto;
    this.esCompatible = esCompatible;
    this.colorEstadoCompatibilidad = colorEstadoCompatibilidad;
  }

  public int getIdCategoria() {
    return idCategoria;
  }

  public String getNombre() {
    return nombre;
  }

  public String getCostoTexto() {
    return costoTexto;
  }

  public String getCuposTexto() {
    return cuposTexto;
  }

  public String getRequisitosTexto() {
    return requisitosTexto;
  }

  public String getEstadoCompatibilidadTexto() {
    return estadoCompatibilidadTexto;
  }

  public boolean isEsCompatible() {
    return esCompatible;
  }

  public int getColorEstadoCompatibilidad() {
    return colorEstadoCompatibilidad;
  }
}
