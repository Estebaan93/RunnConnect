package com.example.runnconnect.data.request;

import java.math.BigDecimal;

public class CrearCategoriaRequest {
  private String nombre;
  private BigDecimal costoInscripcion;
  private int cupoCategoria;
  private int edadMinima;
  private int edadMaxima;
  private String genero;

  public CrearCategoriaRequest(String nombre, BigDecimal costoInscripcion, int cupoCategoria) {
    this.nombre = nombre;
    this.costoInscripcion = costoInscripcion;
    this.cupoCategoria = cupoCategoria;
    // Valores por defecto logicos para simplificar el form
    this.edadMinima = 18;
    this.edadMaxima = 99;
    this.genero = "X";
  }

  //getters
  public String getNombre() {
    return nombre;
  }

  public BigDecimal getCostoInscripcion() {
    return costoInscripcion;
  }

  // Setters necesarios para el ViewModel
  public void setEdadMinima(Integer edadMinima) {
    this.edadMinima = edadMinima;
  }

  public void setEdadMaxima(Integer edadMaxima) {
    this.edadMaxima = edadMaxima;
  }

  public void setGenero(String genero) {
    this.genero = genero;
  }

}
