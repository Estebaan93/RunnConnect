package com.example.runnconnect.ui.runner.misInscripciones;

public class InscripcionUI {
  private final int idInscripcion;
  private final int idEvento;
  private final String nombreEvento;
  private final String fechaLugar;
  private final String categoriaCosto;
  private final String talleRemera;
  private final String estadoTexto;
  private final int estadoColorTexto;
  private final int estadoColorFondo;
  private final String observacionTexto;
  private final int observacionVisibilidad;
  private final int btnSubirComprobanteVisibilidad;
  private final int btnVerComprobanteVisibilidad;
  private final String comprobanteUrl;

  public InscripcionUI(int idInscripcion,
                       int idEvento,
                       String nombreEvento,
                       String fechaLugar,
                       String categoriaCosto,
                       String talleRemera,
                       String estadoTexto,
                       int estadoColorTexto,
                       int estadoColorFondo,
                       String observacionTexto,
                       int observacionVisibilidad,
                       int btnSubirComprobanteVisibilidad,
                       int btnVerComprobanteVisibilidad,
                       String comprobanteUrl) {
    this.idInscripcion = idInscripcion;
    this.idEvento = idEvento;
    this.nombreEvento = nombreEvento;
    this.fechaLugar = fechaLugar;
    this.categoriaCosto = categoriaCosto;
    this.talleRemera = talleRemera;
    this.estadoTexto = estadoTexto;
    this.estadoColorTexto = estadoColorTexto;
    this.estadoColorFondo = estadoColorFondo;
    this.observacionTexto = observacionTexto;
    this.observacionVisibilidad = observacionVisibilidad;
    this.btnSubirComprobanteVisibilidad = btnSubirComprobanteVisibilidad;
    this.btnVerComprobanteVisibilidad = btnVerComprobanteVisibilidad;
    this.comprobanteUrl = comprobanteUrl;
  }

  public int getIdInscripcion() { return idInscripcion; }
  public int getIdEvento() { return idEvento; }
  public String getNombreEvento() { return nombreEvento; }
  public String getFechaLugar() { return fechaLugar; }
  public String getCategoriaCosto() { return categoriaCosto; }
  public String getTalleRemera() { return talleRemera; }
  public String getEstadoTexto() { return estadoTexto; }
  public int getEstadoColorTexto() { return estadoColorTexto; }
  public int getEstadoColorFondo() { return estadoColorFondo; }
  public String getObservacionTexto() { return observacionTexto; }
  public int getObservacionVisibilidad() { return observacionVisibilidad; }
  public int getBtnSubirComprobanteVisibilidad() { return btnSubirComprobanteVisibilidad; }
  public int getBtnVerComprobanteVisibilidad() { return btnVerComprobanteVisibilidad; }
  public String getComprobanteUrl() { return comprobanteUrl; }
}
