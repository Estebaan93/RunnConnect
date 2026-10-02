package com.example.runnconnect.ui.runner.buscarEventos;

import java.util.Collections;
import java.util.List;

public class EventoCardUI {
  private final int idEvento;
  private final String nombre;
  private final String fecha;
  private final String lugar;
  private final String organizador;
  private final List<String> nombresCategorias;

  public EventoCardUI(int idEvento,
                      String nombre,
                      String fecha,
                      String lugar,
                      String organizador,
                      List<String> nombresCategorias) {
    this.idEvento = idEvento;
    this.nombre = nombre;
    this.fecha = fecha;
    this.lugar = lugar;
    this.organizador = organizador;
    this.nombresCategorias = nombresCategorias != null ? nombresCategorias : Collections.emptyList();
  }

  public int getIdEvento() {
    return idEvento;
  }

  public String getNombre() {
    return nombre;
  }

  public String getFecha() {
    return fecha;
  }

  public String getLugar() {
    return lugar;
  }

  public String getOrganizador() {
    return organizador;
  }

  public List<String> getNombresCategorias() {
    return nombresCategorias;
  }
}
