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
    // Valores por defecto lógicos para simplificar el form
    this.edadMinima = 18;
    this.edadMaxima = 99;
    this.genero = "X";
  }

}
